package yk.shiroyk.lightword.ui.managedata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabDataRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;

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

    private VocabTypeRepository vocabTypeRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabDataRepository vocabDataRepository;
    private ExerciseRepository exerciseRepository;

    private CompositeDisposable compositeDisposable;

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

        compositeDisposable = new CompositeDisposable();
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
                v -> sharedViewModel.setSubTitle(title.get(v)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sharedViewModel.setSubTitle("");
        compositeDisposable.dispose();
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
        SubMenu vType = menu.addSubMenu(R.string.import_vdata_submenu);
        vocabTypeRepository.getAllVocabType().observe(getViewLifecycleOwner(),
                vocabTypes -> {
                    setDefaultTitle(vocabTypes);
                    MenuItem searchMenuItem = menu.findItem(R.id.action_search);
                    SearchView searchView = (SearchView) searchMenuItem.getActionView();
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
                                    vdataViewModel.setVocabType(v);
                                    sharedViewModel.setSubTitle(s);
                                    return false;
                                });
                            }
                            vType.setGroupCheckable(0, true, true);
                        }
                        searchMenuItem.setVisible(true);
                        setSearchView(searchView);
                    } else {
                        searchMenuItem.setVisible(false);
                    }
                });
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setSearchView(SearchView searchView) {
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

    private void setWordList() {
        vdataViewModel.getVocabType().observe(getViewLifecycleOwner(), v -> {
            vocabDataRepository.getAllWord(v.getId()).observe(getViewLifecycleOwner(), vList -> {
                if (vList.size() > 0) {
                    vdata_list.setVisibility(View.VISIBLE);
                    tv_vdata_msg.setVisibility(View.GONE);
                    adapter = new VocabularyAdapter(
                            context, vList, vocabulary ->
                            exerciseRepository.getWordDetail(vocabulary.getId(), v.getId()).observe(
                                    getViewLifecycleOwner(),
                                    data -> showDetail(vocabulary.getWord(), data)
                            ));
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

    private void showDetail(String title, ExerciseData data) {
        String detail = "暂无练习数据";
        if (data != null) {
            detail = data.toString();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(detail)
                .setPositiveButton(R.string.dialog_cancel, null)
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
                        compositeDisposable.add(insertVData(v, uri, true)))
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private Disposable insertVData(VocabType v, Uri uri, boolean overWrite) {
        FileUtils fileUtils = new FileUtils(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();

        return Observable.create(
                (ObservableOnSubscribe<Map<String, Long[]>>) emitter -> {
                    emitter.onNext(vocabularyRepository.getWordToFrequencyMap());
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(wordMap -> {
                    List<Long> wordIdList = new ArrayList<>();
                    Integer lines = fileUtils.countLines(uri);

                    if (overWrite) {
                        wordIdList = vocabDataRepository
                                .getAllWordId(v.getId());
                    }
                    v.setAmount(lines);
                    return parseFile(uri, v, wordIdList, wordMap, lines);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                    String msg;
                    if (r.count > 0) {
                        VocabData[] vocabData =
                                r.vocabDataList.toArray(new VocabData[r.count]);
                        vocabDataRepository.insert(vocabData);
                        msg = "词汇数据导入成功,";
                        if (r.ignoreWord > 0) {
                            msg += "新增" + r.count + "条";
                            r.count += r.ignoreWord;
                        } else {
                            msg += "共导入" + r.count + "条数据";
                            r.vocabType.setAmount(r.count);
                            Disposable update = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
                                emitter.onNext(vocabTypeRepository.update(r.vocabType));
                                emitter.onComplete();
                            })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(i -> {
                                        if (i.equals(1)) {
                                            getActivity().invalidateOptionsMenu();
                                        }
                                    });
                            compositeDisposable.add(update);
                        }
                    } else {
                        msg = r.ignoreWord > 0 ? "没有可新增的数据" : "解析失败，未导入数据！";
                    }
                    loadingDialog.dismiss();
                    Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                });
    }

    private FileResult parseFile(Uri uri,
                                 VocabType vocabType,
                                 List<Long> wordIdList,
                                 Map<String, Long[]> wordMap,
                                 int lines) {

        FileResult result = new FileResult();
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
                            result.ignoreWord += 1;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.count = vocabDataList.size();
        result.vocabType = vocabType;
        result.vocabDataList = vocabDataList;
        Log.d(TAG, "词汇数据初始化数量: " + result.count + "/" + lines);
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOCABDATA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String fname = new FileUtils(context).getFileName(uri);
                Disposable disposable = Observable.create(
                        (ObservableOnSubscribe<QueryResult>) emitter -> {
                            QueryResult r = new QueryResult();
                            r.v = vocabTypeRepository.getVocabType(fname);
                            if (r.v == null) {
                                r.isExist = false;
                                r.v = new VocabType();
                                r.v.setAmount(0);
                            }
                            emitter.onNext(r);
                            emitter.onComplete();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(r -> {
                            if (r.isExist) {
                                setEnsureDialog(r.v, uri);
                            } else {
                                r.v.setVocabtype(fname);
                                Disposable create = Observable.create(
                                        (ObservableOnSubscribe<Long>)
                                                emitter -> emitter.onNext(vocabTypeRepository.insert(r.v)))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(id -> {
                                            r.v.setId(id);
                                            insertVData(r.v, uri, false);
                                        });
                                compositeDisposable.add(create);
                            }
                        });
                compositeDisposable.add(disposable);
            }
        }

    }

    private interface TitleString {
        String get(VocabType v);
    }

    private static class QueryResult {
        VocabType v;
        boolean isExist = true;
    }

    private static class FileResult {
        int count = 0;
        int ignoreWord = 0;
        VocabType vocabType;
        List<VocabData> vocabDataList;
    }
}