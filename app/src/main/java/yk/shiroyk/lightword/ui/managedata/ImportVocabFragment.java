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
import java.util.List;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.db.entity.exercise.Collocation;
import yk.shiroyk.lightword.db.entity.exercise.Example;
import yk.shiroyk.lightword.db.entity.exercise.ExerciseList;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.ExampleDetailAdapter;
import yk.shiroyk.lightword.ui.adapter.VocabDetailAdapter;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.ThreadTask;
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

    private TextInputEditText et_vocab;
    private TextInputEditText et_frequency;
    private VocabDetailAdapter vocabDetailAdapter;
    private ExampleDetailAdapter exampleDetailAdapter;
    private ExerciseList exerciseList = new ExerciseList();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabularyDataManage = new VocabularyDataManage(getActivity().getBaseContext());

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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem newItem = menu.add(R.string.new_item);
        newItem.setOnMenuItemClickListener(menuItem -> {
            editVocabDialog(getString(R.string.new_vocab_title), null);
            return false;
        });
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

    private void editVocabDialog(String title, Vocabulary vocabulary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = getLayoutInflater().inflate(R.layout.layout_edit_vocab, null);

        et_vocab = view.findViewById(R.id.et_vocab);
        et_frequency = view.findViewById(R.id.et_frequency);
        TextView tv_new_collocation = view.findViewById(R.id.tv_new_collocation);
        RecyclerView recycler_collocation = view.findViewById(R.id.recycler_collocation);

        tv_new_collocation.setText(R.string.create_collocation);
        tv_new_collocation.setOnClickListener(v -> editCollocationDialog(null));
        vocabDetailAdapter = new VocabDetailAdapter(context,
                this::editCollocationDialog);
        recycler_collocation.setLayoutManager(new LinearLayoutManager(context));
        recycler_collocation.setAdapter(vocabDetailAdapter);

        if (vocabulary != null) {
            et_vocab.setText(vocabulary.getWord());
            et_frequency.setText(vocabulary.getFrequency().toString());
            try {
                String ex = vocabularyDataManage.readFile(vocabulary.getWord());
                exerciseList = new Gson().fromJson(ex, ExerciseList.class);
                vocabDetailAdapter.setCollocations(exerciseList.getCollocation());
            } catch (Exception ignored) {

            }

            builder.setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                delVocabDialog(vocabulary);
            });
        }

        builder.setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.dialog_save, (dialogInterface, i) -> {
                    checkExists();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void checkExists() {
        String w = et_vocab.getText().toString().trim();
        if (w.length() > 0) {
            ThreadTask.runOnThread(() -> vocabularyRepository.queryWord(w), v -> {
                if (v == null) {
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
        if (newVocab) {
            vocabularyRepository.insert(v, l -> {
                Log.d("n", l + "");
                if (l > 0) {

                    vocabularyDataManage.overWriteFile(json, w);
                    Toast.makeText(context, String.format(
                            getString(R.string.vocab_save_success), w), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            vocabularyRepository.update(v);
            vocabularyDataManage.overWriteFile(json, w);
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

        exampleDetailAdapter = new ExampleDetailAdapter(context, this::editExampleDialog);
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
                                vocabularyDataManage.deleteFile(vocabulary.getWord());
                                Toast.makeText(context, String.format(
                                        getString(R.string.vocab_delete_success), vocabulary.getWord()), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
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
                    editVocabDialog(getString(R.string.edit_vocab_title), vocabulary);
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
                importVocab(uri);
            }
        }
    }

    private void importVocab(Uri uri) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();
        Log.d("Uri", uri.toString());

        ThreadTask.runOnThread(() -> {
            List<String> stringList = vocabularyRepository.getWordString();

            int count;
            int overWrite = 0;
            List<Vocabulary> vList = new ArrayList<>();

            try {
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
                            vList.add(vocabulary);
                            vocabularyDataManage.writeFile(line[2], word);
                        } else {
                            overWrite++;
                            vocabularyDataManage.overWriteFile(line[2], word);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String msg;
            count = vList.size();
            if (count > 0) {
                msg = "词库数据导入成功，共导入" + count + "条数据";
                msg += overWrite > 0 ? ", 覆盖" + overWrite + "条数据" : "";
                Vocabulary[] vocabularies = vList.toArray(new Vocabulary[count]);
                vocabularyRepository.insert(vocabularies);
            } else {
                if (overWrite > 0) {
                    msg = "词库数据导入成功，共覆盖" + overWrite + "条数据";
                } else {
                    msg = "解析失败，未导入数据";
                }
            }
            return msg;
        }, msg -> {
            loadingDialog.dismiss();
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });
    }

}