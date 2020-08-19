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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;
import yk.shiroyk.lightword.utils.VocabularyDataManage;


public class ImportVocabFragment extends Fragment {

    private static final String TAG = "ImportVocabFragment";
    private static int REQUEST_VOCABULARY = 10001;

    private SharedViewModel sharedViewModel;

    private Context context;
    private ProgressBar vocab_loading;
    private TextView tv_vocab_msg;
    private FastScrollRecyclerView vocab_list;
    private VocabularyAdapter adapter;

    private VocabularyRepository vocabularyRepository;
    private VocabularyDataManage vocabularyDataManage;

    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabularyDataManage = new VocabularyDataManage(getActivity().getBaseContext());

        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_vocab, container, false);
        context = root.getContext();
        setHasOptionsMenu(true);
        init(root);
        setVocabList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sharedViewModel.setSubTitle("");
        compositeDisposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        vocabularyRepository.getCount().observe(getViewLifecycleOwner(), i -> {
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            if (i > 0) {
                searchMenuItem.setVisible(true);
                setSearchView(searchView);
                inflater.inflate(R.menu.main, menu);
            } else {
                searchMenuItem.setVisible(false);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setSearchView(SearchView searchView) {
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
                        "%" + newText + "%").observe(
                        getViewLifecycleOwner(), words -> {
                            if (words == null) return;
                            adapter.setWords(words);
                        });
            }
        });
    }

    private void init(View root) {
        vocab_loading = root.findViewById(R.id.vocab_loading);
        tv_vocab_msg = root.findViewById(R.id.tv_vocab_msg);
        vocab_list = root.findViewById(R.id.vocab_list);

        root.findViewById(R.id.fab_import_vocab).setOnClickListener(view -> pickVocabulary());
    }

    private void pickVocabulary() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        this.startActivityForResult(intent, REQUEST_VOCABULARY);
    }

    private void setTitle() {
        vocabularyRepository.getCount().observe(this,
                integer -> sharedViewModel.setSubTitle("共" + integer + "条"));
    }

    private void setVocabList() {
        vocabularyRepository.getAllWordList().observe(getViewLifecycleOwner(), v -> {
            if (v.size() > 0) {
                vocab_list.setVisibility(View.VISIBLE);
                tv_vocab_msg.setVisibility(View.GONE);
                adapter = new VocabularyAdapter(
                        context, v, vocabulary -> {
                    showDetail(vocabulary.getWord(),
                            vocabularyDataManage.readFile(vocabulary.getWord()));
                });

                vocab_list.setLayoutManager(new LinearLayoutManager(context));
                vocab_list.setAdapter(adapter);
            } else {
                vocab_list.setVisibility(View.GONE);
                tv_vocab_msg.setVisibility(View.VISIBLE);
            }
            vocab_loading.setVisibility(View.GONE);
        });
    }

    private void showDetail(String title, String detail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(detail)
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOCABULARY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                compositeDisposable.add(importVocab(uri));
            }
        }
    }

    private Disposable importVocab(Uri uri) {
        FileUtils fileUtils = new FileUtils(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();
        Log.d("Uri", uri.toString());
        return Observable.create(
                (ObservableOnSubscribe<List<String>>) emitter -> {
                    emitter.onNext(vocabularyRepository.getWordString());
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(stringList -> {
                    FileResult result = new FileResult();
                    List<Vocabulary> vocabularyList = new ArrayList<>();
                    Integer lines = fileUtils.countLines(uri);

                    InputStream is = context.getContentResolver().openInputStream(uri);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                    String str;

                    while ((str = bufferedReader.readLine()) != null) {
                        String[] line = str.split(",", 3);
                        if (line.length > 1) {
                            String word = line[0];
                            if (!stringList.contains(word)) {
                                Vocabulary vocabulary = new Vocabulary();
                                long frequency = Long.parseLong(line[1]);
                                vocabulary.setWord(word);
                                vocabulary.setFrequency(frequency);
                                vocabularyList.add(vocabulary);
                                vocabularyDataManage.writeFile(line[2], word);
                            } else {
                                result.overWrite++;
                                vocabularyDataManage.overWriteFile(line[2], word);
                            }
                        }
                    }

                    result.count = vocabularyList.size();
                    result.vList = vocabularyList;
                    Log.d(TAG, "词汇例句初始化数量: " + vocabularyList.size() + "/" + lines);
                    return result;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                    String msg;
                    int resultSize = r.vList.size();
                    if (resultSize > 0) {
                        msg = "词库数据导入成功，共导入" + resultSize + "条数据";
                        msg += r.overWrite > 0 ? ", 覆盖" + r.overWrite + "条数据" : "";
                        Vocabulary[] vocabularies = r.vList.toArray(new Vocabulary[r.count]);
                        vocabularyRepository.insert(vocabularies);
                    } else {
                        if (r.overWrite > 0) {
                            msg = "词库数据导入成功，共覆盖" + r.overWrite + "条数据";
                        } else {
                            msg = "解析失败，未导入数据";
                        }
                    }
                    loadingDialog.dismiss();
                    Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                });
    }

    private static class FileResult {
        int count = 0;
        int overWrite = 0;
        List<Vocabulary> vList;
    }
}