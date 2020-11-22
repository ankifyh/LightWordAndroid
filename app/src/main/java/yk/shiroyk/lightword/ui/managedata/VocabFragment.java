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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import yk.shiroyk.lightword.MainActivity;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.db.entity.exercise.Collocation;
import yk.shiroyk.lightword.db.entity.exercise.Example;
import yk.shiroyk.lightword.db.entity.exercise.ExerciseList;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.ExampleDetailAdapter;
import yk.shiroyk.lightword.ui.adapter.VocabDetailAdapter;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;
import yk.shiroyk.lightword.utils.ThreadTask;
import yk.shiroyk.lightword.utils.VocabularyDataManage;


public class VocabFragment extends Fragment {

    private static final String TAG = "ImportVocabFragment";
    private static final int REQUEST_VOCABULARY = 10001;

    private VocabViewModel vocabViewModel;
    private SharedViewModel sharedViewModel;

    private Context context;
    private ProgressBar vocab_loading;
    private TextView tv_vocab_msg;
    private FastScrollRecyclerView vocab_list;
    private VocabularyAdapter adapter;
    private MenuItem doneMenuItem;
    private MenuItem newVocab;

    private VocabularyRepository vocabularyRepository;
    private VocabTypeRepository vocabTypeRepository;
    private VocabularyDataManage vocabularyDataManage;
    private ExerciseRepository exerciseRepository;

    private TextInputEditText et_vocab;
    private TextInputEditText et_frequency;
    private TextInputEditText et_pronounce;
    private VocabDetailAdapter vocabDetailAdapter;
    private ExampleDetailAdapter exampleDetailAdapter;
    private ExerciseList exerciseList = new ExerciseList();

    private VocabType defaultVType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        vocabViewModel = new ViewModelProvider(requireActivity()).get(VocabViewModel.class);
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        vocabularyDataManage = new VocabularyDataManage(getActivity().getBaseContext());
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_vocab, container, false);
        context = root.getContext();
        setHasOptionsMenu(true);
        init(root);
        setWordList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        vocabViewModel.getVocabType().observe(getViewLifecycleOwner(),
                v -> {
                    defaultVType = v;
                    sharedViewModel.setSubTitle(v.toString());
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        SubMenu vType = menu.addSubMenu(R.string.import_vocab_data_submenu);
        ThreadTask.runOnThread(() -> vocabTypeRepository.getAllVocabTypes(), vocabTypes -> {
            setDefaultTitle(vocabTypes);
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            doneMenuItem = menu.findItem(R.id.action_done);
            newVocab = menu.findItem(R.id.action_create_vocab);
            MenuItem allVocabItem = menu.findItem(R.id.action_all_vocab);
            MenuItem reviewVocabItem = menu.findItem(R.id.action_review_vocab);
            MenuItem masterItem = menu.findItem(R.id.action_master_word);
            MenuItem masterSelectItem = menu.findItem(R.id.action_master_select_word);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();

            setSearchViewHide(searchView);
            newVocab.setVisible(true);
            newVocab.setOnMenuItemClickListener(menuItem -> {
                editVocabDialog(getString(R.string.new_vocab_title), null);
                return false;
            });

            allVocabItem.setOnMenuItemClickListener(menuItem -> {
                allVocabItem.setChecked(true);
                exitSelectMode();
                setWordList();
                setSearchWordView(searchView);
                masterSelectItem.setTitle(R.string.mastered_word);
                sharedViewModel.setSubTitle(defaultVType.toString());
                return false;
            });

            reviewVocabItem.setOnMenuItemClickListener(menuItem -> {
                reviewVocabItem.setChecked(true);
                exitSelectMode();
                setReviewWordList(defaultVType);
                setSearchWordView(searchView);
                masterSelectItem.setTitle(R.string.mastered_word);
                return false;
            });

            masterItem.setOnMenuItemClickListener(menuItem -> {
                masterItem.setChecked(true);
                exitSelectMode();
                setMasterWordList(defaultVType);
                setSearchMasterWordView(searchView);
                masterSelectItem.setTitle(R.string.demaster_word);
                return false;
            });

            if (vocabTypes.size() > 0) {
                if (vocabTypes.size() > vType.size()) {
                    vType.clear();
                    for (VocabType v : vocabTypes) {
                        String s = v.toString();
                        MenuItem item = vType.add(s);
                        if (v.getId().equals(defaultVType.getId())) {
                            item.setChecked(true);
                        }
                        item.setOnMenuItemClickListener(menuItem -> {
                            item.setChecked(true);
                            allVocabItem.setChecked(true);
                            masterSelectItem.setTitle(R.string.mastered_word);
                            vocabViewModel.setVocabType(v);
                            sharedViewModel.setSubTitle(s);
                            return false;
                        });
                    }
                    vType.setGroupCheckable(0, true, true);
                }
                allVocabItem.setChecked(true);
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
                    deMasterWord(adapter.getSelectedItem());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void init(View root) {
        vocab_loading = root.findViewById(R.id.vocab_loading);
        tv_vocab_msg = root.findViewById(R.id.tv_vocab_msg);
        vocab_list = root.findViewById(R.id.vocab_list);

        root.findViewById(R.id.fab_import_vocab).setOnClickListener(view -> pickVocabulary());
    }

    private void setDefaultTitle(List<VocabType> vocabTypes) {
        if (vocabTypes.size() > 0) {
            defaultVType = vocabTypes.get(0);
            vocabViewModel.setVocabType(defaultVType);
            sharedViewModel.setSubTitle(defaultVType.toString());
        } else {
            tv_vocab_msg.setVisibility(View.VISIBLE);
            vocab_loading.setVisibility(View.GONE);
        }
    }

    private void setSearchViewHide(SearchView searchView) {
        searchView.setOnSearchClickListener(view -> {
            newVocab.setVisible(false);
        });
        searchView.setOnCloseListener(() -> {
            newVocab.setVisible(true);
            return false;
        });
    }

    private void setSearchWordView(SearchView searchView) {
        vocabViewModel.getVocabType().observe(getViewLifecycleOwner(), v -> {
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
                    vocabularyRepository.searchWord(
                            "%" + newText + "%", v.getId()).observe(
                            getViewLifecycleOwner(), words -> {
                                if (words == null) return;
                                adapter.setWords(words);
                            });
                }
            });
        });
    }

    private void setSearchMasterWordView(SearchView searchView) {
        vocabViewModel.getVocabType().observe(getViewLifecycleOwner(), vocabType -> {
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

    private void enterSelectMode() {
        doneMenuItem.setVisible(true);
        newVocab.setVisible(false);
    }

    private void exitSelectMode() {
        doneMenuItem.setVisible(false);
        newVocab.setVisible(true);
        sharedViewModel.setSubTitle(defaultVType.toString());
        if (adapter != null)
            adapter.exitMultiSelectMode();
    }

    /**
     * Modify Vocabulary Data
     * ⬇️
     */

    private void editVocabDialog(String title, Vocabulary vocabulary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = getLayoutInflater().inflate(R.layout.layout_edit_vocab, null);

        et_vocab = view.findViewById(R.id.et_vocab);
        et_frequency = view.findViewById(R.id.et_frequency);
        et_pronounce = view.findViewById(R.id.et_pronounce);
        TextView tv_new_collocation = view.findViewById(R.id.tv_new_collocation);
        RecyclerView recycler_collocation = view.findViewById(R.id.recycler_collocation);

        tv_new_collocation.setText(R.string.create_collocation);
        tv_new_collocation.setOnClickListener(v -> editCollocationDialog(null));
        vocabDetailAdapter = new VocabDetailAdapter(context,
                this::editCollocationDialog,
                false);
        recycler_collocation.setLayoutManager(new LinearLayoutManager(context));
        recycler_collocation.setAdapter(vocabDetailAdapter);

        if (vocabulary != null) {
            et_vocab.setText(vocabulary.getWord());
            et_frequency.setText(vocabulary.getFrequency().toString());
            try {
                String ex = vocabularyDataManage
                        .readFile(vocabulary.getVtypeId(), vocabulary.getWord());
                exerciseList = new Gson().fromJson(ex, ExerciseList.class);
                et_pronounce.setText(exerciseList.getPronounceString());
                vocabDetailAdapter.setCollocations(exerciseList.getCollocation());
            } catch (Exception ignored) {

            }

            builder.setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                delVocabDialog(vocabulary);
            });
        } else {
            builder.setNeutralButton(R.string.create_vocab_type, (dialogInterface, i) -> {
                setCreateVTypeDialog();
            });
        }

        builder.setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                    if (defaultVType == null) {
                        Toast.makeText(context,
                                R.string.not_fount_vocab_type, Toast.LENGTH_SHORT).show();
                        setCreateVTypeDialog();
                    } else {
                        checkExists();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void checkExists() {
        String w = et_vocab.getText().toString().trim();
        if (w.length() > 0) {
            ThreadTask.runOnThread(() ->
                    vocabularyRepository.queryWord(w, defaultVType.getId()), v -> {
                if (v == null) {
                    exerciseList.setInflection(null);
                    exerciseList.setSource(null);
                    writeToFile(w, true);
                } else {
                    overWriteDialog(w);
                }
            });
        } else {
            Toast.makeText(context, R.string.save_vocab_error, Toast.LENGTH_SHORT).show();
        }

    }

    private void overWriteDialog(String w) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format(getString(R.string.vocab_exist), w))
                .setMessage(R.string.vocab_exist_msg)
                .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                    writeToFile(w, false);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void writeToFile(String w, boolean newVocab) {
        exerciseList.setPronounce(Arrays.asList(et_pronounce.getText().toString().split(",")));

        String json = new Gson().toJson(exerciseList, ExerciseList.class);
        Vocabulary v = new Vocabulary();
        v.setWord(w);
        long fre;
        try {
            fre = Long.parseLong(et_frequency.getText().toString().trim());
        } catch (Exception ignored) {
            fre = 0;
        }
        v.setFrequency(fre);
        v.setVtypeId(defaultVType.getId());
        if (newVocab) {
            vocabularyRepository.insert(v, l -> {
                if (l > 0) {
                    vocabularyDataManage.overWriteFile(json, defaultVType.getId(), w);
                    // update vocab type amount
                    defaultVType.inAmount(1);
                    vocabTypeRepository.updateAmount(defaultVType);

                    // refresh option menu
                    getActivity().invalidateOptionsMenu();

                    Toast.makeText(context, String.format(
                            getString(R.string.vocab_save_success), w), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            vocabularyRepository.update(v);
            vocabularyDataManage.overWriteFile(json, v.getVtypeId(), w);
            Toast.makeText(context, String.format(
                    getString(R.string.vocab_save_success), w), Toast.LENGTH_SHORT).show();
        }

    }

    private void editCollocationDialog(Collocation collocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = getLayoutInflater().inflate(R.layout.layout_edit_collocation, null);
        TextInputEditText et_pos = view.findViewById(R.id.et_pos);
        TextInputEditText et_mean = view.findViewById(R.id.et_mean);
        RecyclerView example_list = view.findViewById(R.id.example_list);
        TextView tv_new_example = view.findViewById(R.id.tv_new_example);

        exampleDetailAdapter = new ExampleDetailAdapter(context,
                this::editExampleDialog, false);
        example_list.setLayoutManager(new LinearLayoutManager(context));
        example_list.setAdapter(exampleDetailAdapter);
        tv_new_example.setText(R.string.create_example);
        tv_new_example.setOnClickListener(v -> editExampleDialog(null));

        if (collocation != null) {
            et_pos.setText(collocation.getPartOfSpeech());
            et_mean.setText(collocation.getMeaning());
            exampleDetailAdapter.setExampleList(collocation.getExample());
            builder.setTitle(collocation.getMeaning())
                    .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                        collocation.setPartOfSpeech(et_pos.getText().toString().trim());
                        collocation.setMeaning(et_mean.getText().toString().trim());
                        vocabDetailAdapter.notifyDataSetChanged();
                    });
            builder.setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                vocabDetailAdapter.getCollocations().remove(collocation);
                vocabDetailAdapter.notifyDataSetChanged();
            });
        } else {
            builder.setTitle("搭配")
                    .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                        Collocation coll = new Collocation();
                        coll.setPartOfSpeech(et_pos.getText().toString().trim());
                        coll.setMeaning(et_mean.getText().toString().trim());
                        coll.setExample(exampleDetailAdapter.getExampleList());
                        vocabDetailAdapter.addCollocation(coll);
                        exerciseList.setCollocation(vocabDetailAdapter.getCollocations());
                    });
        }

        builder.setView(view)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();

    }

    private void editExampleDialog(Example example) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = getLayoutInflater().inflate(R.layout.layout_edit_example, null);
        TextInputEditText et_example = view.findViewById(R.id.et_example);
        TextInputEditText et_translation = view.findViewById(R.id.et_translation);
        TextInputEditText et_answer = view.findViewById(R.id.et_answer);

        if (example != null) {
            et_example.setText(example.getExample());
            et_translation.setText(example.getTranslation());
            if (example.hasAnswer()) {
                et_answer.setText(example.getAnswer());
            }
            builder.setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                example.setExample(et_example.getText().toString().trim());
                example.setTranslation(et_translation.getText().toString().trim());
                example.setAnswer(et_answer.getText().toString().trim());
                exampleDetailAdapter.notifyDataSetChanged();
            });
            builder.setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                exampleDetailAdapter.getExampleList().remove(example);
                exampleDetailAdapter.notifyDataSetChanged();
            });
        } else {
            builder.setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                Example ex = new Example();
                ex.setExample(et_example.getText().toString().trim());
                ex.setTranslation(et_translation.getText().toString().trim());
                ex.setAnswer(et_answer.getText().toString().trim());
                exampleDetailAdapter.addExample(ex);
            });
        }

        builder.setTitle("例句")
                .setView(view)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void delVocabDialog(Vocabulary vocabulary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_vocab)
                .setMessage(String.format(getString(R.string.delete_vocab_msg), vocabulary.getWord()))
                .setPositiveButton(R.string.dialog_delete, (dialogInterface, i) ->
                        vocabularyRepository.delete(vocabulary, l -> {
                            if (l > 0) {
                                vocabularyDataManage.deleteFile(vocabulary.getVtypeId(), vocabulary.getWord());
                                Toast.makeText(context, String.format(
                                        getString(R.string.vocab_delete_success),
                                        vocabulary.getWord()), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void setCreateVTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.layout_create_vocab_type, null);
        TextInputEditText et_type = view.findViewById(R.id.et_type);

        builder.setTitle(R.string.create_vocab_type)
                .setView(view)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    String vName = et_type.getText().toString().trim();
                    if (vName.length() > 0) {
                        ThreadTask.runOnThread(() -> {
                            VocabType v = vocabTypeRepository.getVocabType(vName);
                            if (v == null) {
                                // if not exist, create
                                v = new VocabType();
                                v.setVocabtype(vName);
                                v.setAmount(0);
                                Long id = vocabTypeRepository.insert(v);
                                v.setId(id);
                            }
                            return v;
                        }, v -> vocabViewModel.setVocabType(v));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    /**
     * Modify mastered vocabulary
     * ⬇️
     */

    private void deMasterWord(String title, ExerciseData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(R.string.delete_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateExerciseData(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void deMasterWord(List<Long> data) {
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
            exerciseRepository.mastered(id, defaultVType.getId());
        }
        Toast.makeText(context, "成功添加" + idList.size() + "个!", Toast.LENGTH_SHORT).show();
    }

    private void updateExerciseData(ExerciseData data) {
        //reset exercise data
        data.setTimestamp(new Date());
        data.setStage(1);
        exerciseRepository.update(data);
        setMasterWordList(defaultVType);
        Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Set Vocabulary list
     * ⬇️
     */

    private void setVocabList(List<Vocabulary> vList) {
        if (vList.size() > 0) {
            vocab_list.setVisibility(View.VISIBLE);
            tv_vocab_msg.setVisibility(View.GONE);
            adapter = new VocabularyAdapter(context, vList);
            adapter.setOnInfoClickListener(vocabulary ->
                    editVocabDialog(getString(R.string.edit_vocab_title), vocabulary));
            adapter.setOnLongClickListener(multiSelectMode -> {
                if (multiSelectMode) {
                    enterSelectMode();
                }
            });
            adapter.setOnSelectedChanged(size -> {
                //change done menu visible by selected size
                if (size == 0) {
                    exitSelectMode();
                } else {
                    enterSelectMode();
                    sharedViewModel.setSubTitle(String.format(
                            getString(R.string.mulit_select_item_title),
                            size,
                            vList.size()));
                }
            });
            vocab_list.setLayoutManager(new LinearLayoutManager(context));
            vocab_list.setAdapter(adapter);
        } else {
            vocab_list.setVisibility(View.GONE);
            tv_vocab_msg.setVisibility(View.VISIBLE);
        }
        vocab_loading.setVisibility(View.GONE);
    }

    private void setWordList() {
        vocabViewModel.getVocabType().observe(getViewLifecycleOwner(), v -> {
            vocabularyRepository.getAllWordList(v.getId()).observe(
                    getViewLifecycleOwner(), this::setVocabList);
        });
    }

    private void setReviewWordList(VocabType vType) {
        vocabularyRepository.getAllReviewWord(vType.getId()).observe(
                getViewLifecycleOwner(), vList -> {
                    setVocabList(vList);
                    sharedViewModel.setSubTitle(
                            String.format(getString(R.string.review_vocab_size), vList.size()));
                });
    }

    private void setMasterWordList(VocabType vType) {
        //query word list by stage > 10
        ThreadTask.runOnThread(() -> exerciseRepository.getMasterWord(vType.getId()), vList -> {
            if (vList.size() > 0) {
                vocab_list.setVisibility(View.VISIBLE);
                tv_vocab_msg.setVisibility(View.GONE);
                adapter.setWords(vList);
                adapter.setOnInfoClickListener(vocabulary ->
                        ThreadTask.runOnThread(() -> exerciseRepository
                                        .getWordDetail(vocabulary.getId(), vType.getId()),
                                data -> deMasterWord(vocabulary.getWord(), data)));
                adapter.setOnLongClickListener(multiSelectMode -> {
                    if (multiSelectMode) {
                        enterSelectMode();
                    }
                });
                adapter.setOnSelectedChanged(size -> {
                    //change done menu visible by selected size
                    if (size == 0) {
                        exitSelectMode();
                    } else {
                        enterSelectMode();
                        sharedViewModel.setSubTitle(String.format(
                                getString(R.string.mulit_select_item_title),
                                size,
                                vList.size()));
                    }
                });
                vocab_list.setLayoutManager(new LinearLayoutManager(context));
                vocab_list.setAdapter(adapter);
            } else {
                vocab_list.setVisibility(View.GONE);
                tv_vocab_msg.setVisibility(View.VISIBLE);
            }
            sharedViewModel.setSubTitle(
                    String.format(getString(R.string.master_word), vList.size()));
            vocab_loading.setVisibility(View.GONE);
        });
    }

    /**
     * Import Vocabulary
     * ⬇️
     */

    private void pickVocabulary() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        this.startActivityForResult(intent, REQUEST_VOCABULARY);
    }

    private void updateExerciseData(List<Long> idList) {
        //reset exercise data
        ThreadTask.runOnThread(() -> exerciseRepository
                        .getWordListById(idList, defaultVType.getId()),
                dataArray -> {
                    Date date = new Date();
                    for (ExerciseData data : dataArray) {
                        data.setTimestamp(date);
                        data.setStage(1);
                    }
                    exerciseRepository.update(dataArray);
                    // refresh mastered word list
                    setMasterWordList(defaultVType);
                });
        Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    private void setEnsureDialog(VocabType v, Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.import_vocab_type_exist_title)
                .setMessage(R.string.import_vocab_type_exist_message)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) ->
                        importVocab(v, uri, true))
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void importVocab(VocabType vocabType, Uri uri, boolean overWrite) {
        FileUtils fileUtils = new FileUtils(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();

        ThreadTask.runOnThread(() -> {
            List<String> wordList = new ArrayList<>();
            Integer lines = fileUtils.countLines(uri);

            if (overWrite) {
                wordList = vocabularyRepository
                        .getWordString(vocabType.getId());
            }
            vocabType.setAmount(lines);

            int overWriteNum = 0;
            List<Vocabulary> vocabList = new ArrayList<>();

            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    String[] line = str.split(",", 3);
                    if (line.length == 3) {
                        String word = line[0];
                        if (!wordList.contains(word)) {
                            Vocabulary vocabulary = new Vocabulary();
                            try {
                                long frequency = Long.parseLong(line[1]);
                                vocabulary.setWord(word);
                                vocabulary.setFrequency(frequency);
                                vocabulary.setVtypeId(vocabType.getId());
                                vocabList.add(vocabulary);
                                vocabularyDataManage
                                        .writeFile(line[2], vocabType.getId(), word);
                            } catch (NumberFormatException ignored) {

                            }
                        } else {
                            overWriteNum++;
                            vocabularyDataManage
                                    .overWriteFile(line[2], vocabType.getId(), word);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            int count = vocabList.size();

            String msg;

            if (count > 0) {
                Vocabulary[] vocab =
                        vocabList.toArray(new Vocabulary[count]);
                vocabularyRepository.insert(vocab);

                msg = "词汇数据导入成功,";
                if (overWriteNum > 0) {
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
                if (overWriteNum > 0) {
                    msg = "导入成功，共覆盖" + overWriteNum + "条数据";
                } else {
                    msg = "解析失败，未导入数据！";
                }
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
        if (requestCode == REQUEST_VOCABULARY && resultCode == Activity.RESULT_OK) {
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
                                () -> importVocab(vType, uri, false));
                    }
                });
            }
        }

    }

}