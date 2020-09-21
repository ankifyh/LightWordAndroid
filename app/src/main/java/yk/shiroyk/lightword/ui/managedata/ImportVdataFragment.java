package yk.shiroyk.lightword.ui.managedata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import yk.shiroyk.lightword.MainActivity;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabDataRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ImportVdataFragment extends Fragment {

    private static final String TAG = "ImportVdataFragment";
    private static int REQUEST_VOCABDATA = 10002;

    private SharedViewModel sharedViewModel;
    private ImportVdataViewModel vdataViewModel;

    private Context context;
    private ProgressBar vdata_loading;
    private TextView tv_vdata_msg;
    private FastScrollRecyclerView vdata_list;
    private VocabularyAdapter adapter;
    private MenuItem doneMenuItem;

    private VocabTypeRepository vocabTypeRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabDataRepository vocabDataRepository;
    private ExerciseRepository exerciseRepository;

    private VocabType defaultVocabType;
    private TitleString title = (VocabType v) -> v.getVocabtype() + " (" + v.getAmount() + ")";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vdataViewModel = ViewModelProviders.of(this).get(ImportVdataViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        vocabDataRepository = new VocabDataRepository(getActivity().getApplication());
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_vdata, container, false);

        context = root.getContext();

        setHasOptionsMenu(true);
        init(root);
        setWordList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        vdataViewModel.getVocabType().observe(getViewLifecycleOwner(),
                v -> {
                    defaultVocabType = v;
                    sharedViewModel.setSubTitle(title.get(v));
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sharedViewModel.setSubTitle("");
        if (adapter != null) {
            adapter.exitMultiSelectMode();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.clearSelected();
        }
    }

    private void setDefaultTitle(List<VocabType> vocabTypes) {
        if (vocabTypes.size() > 0) {
            defaultVocabType = vocabTypes.get(0);
            vdataViewModel.setVocabType(defaultVocabType);
            sharedViewModel.setSubTitle(title.get(defaultVocabType));
        } else {
            tv_vdata_msg.setVisibility(View.VISIBLE);
            vdata_loading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        SubMenu vType = menu.addSubMenu(R.string.import_vdata_submenu);
        ThreadTask.runOnThread(() -> vocabTypeRepository.getAllVocabTypes(), vocabTypes -> {
            setDefaultTitle(vocabTypes);
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            doneMenuItem = menu.findItem(R.id.action_done);
            MenuItem allVDataItem = menu.findItem(R.id.action_all_vocabdata);
            MenuItem masterItem = menu.findItem(R.id.action_master_word);
            MenuItem masterSelectItem = menu.findItem(R.id.action_master_select_word);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();

            allVDataItem.setOnMenuItemClickListener(menuItem -> {
                allVDataItem.setChecked(true);
                exitSelectMode();
                setWordList();
                setSearchWordView(searchView);
                masterSelectItem.setTitle(R.string.mastered_word);
                return false;
            });
            masterItem.setOnMenuItemClickListener(menuItem -> {
                masterItem.setChecked(true);
                exitSelectMode();
                setMasterWordList(defaultVocabType);
                setSearchMasterWordView(searchView);
                masterSelectItem.setTitle(R.string.demaster_word);
                return false;
            });

            if (vocabTypes.size() > 0) {
                if (vocabTypes.size() > vType.size()) {
                    vType.clear();
                    for (VocabType v : vocabTypes) {
                        String s = title.get(v);
                        MenuItem item = vType.add(s);
                        if (v.getId().equals(defaultVocabType.getId())) {
                            item.setChecked(true);
                        }
                        item.setOnMenuItemClickListener(menuItem -> {
                            item.setChecked(true);
                            allVDataItem.setChecked(true);
                            masterSelectItem.setTitle(R.string.mastered_word);
                            vdataViewModel.setVocabType(v);
                            sharedViewModel.setSubTitle(s);
                            return false;
                        });
                    }
                    vType.setGroupCheckable(0, true, true);
                }
                allVDataItem.setChecked(true);
                searchMenuItem.setVisible(true);
                setSearchWordView(searchView);
                MenuItem masterWord = menu.findItem(R.id.action_master_select_word);
                masterWord.setVisible(true);
                menu.setGroupVisible(R.id.vocab_data_menu_group, true);
                menu.setGroupCheckable(R.id.vocab_data_menu_group, true, true);
            } else {
                searchMenuItem.setVisible(false);
                menu.setGroupVisible(R.id.vocab_data_menu_group, false);
            }
        });
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_select:
                exitSelectMode();
                return true;
            case R.id.action_master_select_word:
                if (getString(R.string.mastered_word).contentEquals(item.getTitle())) {
                    masterWordDialog(adapter.getSelectedItem());
                } else {
                    delMasterWord(adapter.getSelectedItem());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exitSelectMode() {
        doneMenuItem.setVisible(false);
        adapter.exitMultiSelectMode();
        sharedViewModel.setSubTitle(title.get(defaultVocabType));
    }

    private void setSearchWordView(SearchView searchView) {
        vdataViewModel.getVocabType().observe(getViewLifecycleOwner(), v -> {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    getResults(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    getResults(s);
                    return true;
                }

                private void getResults(String newText) {
                    vocabDataRepository.searchWord(v.getId(),
                            "%" + newText + "%").observe(
                            getViewLifecycleOwner(), words -> {
                                if (words == null) return;
                                adapter.setWords(words);
                            });
                }
            });
        });
    }

    private void setSearchMasterWordView(SearchView searchView) {
        vdataViewModel.getVocabType().observe(getViewLifecycleOwner(), vocabType -> {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    getResults(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    getResults(s);
                    return true;
                }

                private void getResults(String newText) {
                    exerciseRepository.searchMasterWord(vocabType.getId(),
                            "%" + newText + "%").observe(
                            getViewLifecycleOwner(), words -> {
                                if (words == null) return;
                                adapter.setWords(words);
                            });
                }
            });
        });
    }

    private void setWordList() {
        vdataViewModel.getVocabType().observe(getViewLifecycleOwner(), v -> {
            vocabDataRepository.getAllWord(v.getId()).observe(getViewLifecycleOwner(), vList -> {
                if (vList.size() > 0) {
                    vdata_list.setVisibility(View.VISIBLE);
                    tv_vdata_msg.setVisibility(View.GONE);
                    adapter = new VocabularyAdapter(context, vList);
                    adapter.setOnInfoClickListener(vocabulary -> {
                        ThreadTask.runOnThread(() -> exerciseRepository
                                        .getWordDetail(vocabulary.getId(), v.getId()),
                                data -> showDetail(vocabulary, v.getId(), data));
                    });
                    adapter.setOnLongClickListener(multiSelectMode -> {
                        if (multiSelectMode) {
                            doneMenuItem.setVisible(true);
                        }
                    });
                    adapter.setOnSelectedChanged(size -> {
                        //change done menu visible by selected size
                        if (size == 0) {
                            exitSelectMode();
                        } else {
                            doneMenuItem.setVisible(true);
                            sharedViewModel.setSubTitle(
                                    String.format(
                                            getString(R.string.mulit_select_item_title),
                                            size,
                                            vList.size()));
                        }
                    });
                    vdata_list.setLayoutManager(new LinearLayoutManager(context));
                    vdata_list.setAdapter(adapter);
                } else {
                    vdata_list.setVisibility(View.GONE);
                    tv_vdata_msg.setVisibility(View.VISIBLE);
                }
                vdata_loading.setVisibility(View.GONE);
            });
        });
    }

    private void setMasterWordList(VocabType vType) {
        //query word list by stage > 10
        ThreadTask.runOnThread(() -> exerciseRepository.getMasterWord(vType.getId()), vList -> {
            if (vList.size() > 0) {
                vdata_list.setVisibility(View.VISIBLE);
                tv_vdata_msg.setVisibility(View.GONE);
                adapter.setWords(vList);
                adapter.setOnInfoClickListener(vocabulary ->
                        ThreadTask.runOnThread(() -> exerciseRepository
                                        .getWordDetail(vocabulary.getId(), vType.getId()),
                                data -> delMasterWord(vocabulary.getWord(), data)));
                adapter.setOnLongClickListener(multiSelectMode -> {
                    if (multiSelectMode) {
                        doneMenuItem.setVisible(true);
                    }
                });
                adapter.setOnSelectedChanged(size -> {
                    //change done menu visible by selected size
                    if (size == 0) {
                        exitSelectMode();
                    } else {
                        doneMenuItem.setVisible(true);
                        sharedViewModel.setSubTitle(String.format(
                                getString(R.string.mulit_select_item_title),
                                size,
                                vList.size()));
                    }
                });
                vdata_list.setLayoutManager(new LinearLayoutManager(context));
                vdata_list.setAdapter(adapter);
            } else {
                vdata_list.setVisibility(View.GONE);
                tv_vdata_msg.setVisibility(View.VISIBLE);
            }
            vdata_loading.setVisibility(View.GONE);
        });
    }

    private void delMasterWord(String title, ExerciseData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(R.string.delete_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateExerciseData(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void delMasterWord(List<Long> data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_select_master_word_title)
                .setMessage(R.string.delete_select_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateExerciseData(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void masterWordDialog(List<Long> data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_select_master_word_title)
                .setMessage(R.string.add_select_master_word_msg)
                .setPositiveButton(R.string.dialog_ensure,
                        (dialogInterface, i) -> masterWord(data))
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void masterWord(List<Long> idList) {
        for (Long id : idList) {
            exerciseRepository.mastered(id, defaultVocabType.getId());
        }
        Toast.makeText(context, "成功添加" + idList.size() + "个!", Toast.LENGTH_SHORT).show();
    }

    private void updateExerciseData(ExerciseData data) {
        //reset exercise data
        data.setTimestamp(new Date());
        data.setStage(1);
        exerciseRepository.update(data);
        setMasterWordList(defaultVocabType);
        Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    private void updateExerciseData(List<Long> idList) {
        //reset exercise data
        ThreadTask.runOnThread(() -> exerciseRepository
                        .getWordListById(idList, defaultVocabType.getId()),
                dataArray -> {
                    Date date = new Date();
                    for (ExerciseData data : dataArray) {
                        data.setTimestamp(date);
                        data.setStage(1);
                    }
                    exerciseRepository.update(dataArray);
                    // refresh master word list
                    setMasterWordList(defaultVocabType);
                });
        Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    private void showDetail(Vocabulary v, Long vTypeId, ExerciseData data) {
        String detail = getString(R.string.no_exercise_data);
        boolean showNeutral = false;
        if (data != null) {
            if (data.getStage() > (exerciseRepository.getForgetTimeSize())) {
                detail = data.toMasterString();
            } else {
                showNeutral = true;
                detail = data.toString();
            }
        } else {
            showNeutral = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(v.getWord())
                .setMessage(detail);
        if (showNeutral) {
            builder.setNeutralButton(R.string.dialog_btn_mastered, (dialogInterface, i) -> {
                exerciseRepository.mastered(v.getId(), vTypeId);
                Toast.makeText(context,
                        String.format(getString(R.string.add_master_word_succuess_msg),
                                v.getWord()), Toast.LENGTH_SHORT).show();
            });
        }
        builder.setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void init(View root) {
        vdata_loading = root.findViewById(R.id.vdata_loading);
        tv_vdata_msg = root.findViewById(R.id.tv_vdata_msg);
        vdata_list = root.findViewById(R.id.vdata_list);

        root.findViewById(R.id.fab_import_vdata).setOnClickListener(view -> {
            pickVocabData();
        });
    }

    private void pickVocabData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        this.startActivityForResult(intent, REQUEST_VOCABDATA);
    }

    private void setEnsureDialog(VocabType v, Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.import_vdata_type_exist_title)
                .setMessage(R.string.import_vdata_type_exist_message)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) ->
                        insertVData(v, uri, true))
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void insertVData(VocabType vocabType, Uri uri, boolean overWrite) {
        FileUtils fileUtils = new FileUtils(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();

        ThreadTask.runOnThread(() -> {
            Map<String, Long[]> wordMap = vocabularyRepository.getWordToFrequencyMap();
            List<Long> wordIdList = new ArrayList<>();
            Integer lines = fileUtils.countLines(uri);

            if (overWrite) {
                wordIdList = vocabDataRepository
                        .getAllWordId(vocabType.getId());
            }
            vocabType.setAmount(lines);

            int ignoreWord = 0;
            List<VocabData> vocabDataList = new ArrayList<>();

            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    String line = str.trim();
                    if (!line.isEmpty()) {
                        VocabData v = new VocabData();
                        Long[] data = wordMap.get(line);
                        if (data != null) {
                            Long wordId = data[0];
                            if (!wordIdList.contains(wordId)) {
                                v.setWordId(wordId);
                                v.setFrequency(data[1]);
                                v.setVtypeId(vocabType.getId());
                                vocabDataList.add(v);
                            } else {
                                ignoreWord += 1;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            int count = vocabDataList.size();

            String msg;
            if (count > 0) {
                VocabData[] vocabData =
                        vocabDataList.toArray(new VocabData[count]);
                vocabDataRepository.insert(vocabData);
                msg = "词汇数据导入成功,";
                if (ignoreWord > 0) {
                    msg += "新增" + count + "条";
                } else {
                    msg += "共导入" + count + "条数据";
                    vocabType.setAmount(count);
                    Integer i = vocabTypeRepository.update(vocabType);
                    if (i.equals(1)) {
                        getActivity().invalidateOptionsMenu();
                    }
                }
            } else {
                msg = ignoreWord > 0 ? "没有可新增的数据" : "解析失败，未导入数据！";
            }

            return msg;
        }, msg -> {
            loadingDialog.dismiss();
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOCABDATA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String fname = new FileUtils(context).getFileName(uri);

                ThreadTask.runOnThread(() -> {
                    VocabType vocabType = vocabTypeRepository.getVocabType(fname);
                    if (vocabType != null) {
                        ((MainActivity) context).runOnUiThread(
                                () -> setEnsureDialog(vocabType, uri));
                    } else {
                        VocabType vType = new VocabType();
                        vType.setVocabtype(fname);
                        Long id = vocabTypeRepository.insert(vType);
                        vType.setId(id);
                        ((MainActivity) context).runOnUiThread(
                                () -> insertVData(vType, uri, false));
                    }
                });
            }
        }

    }

    private interface TitleString {
        String get(VocabType v);
    }

}