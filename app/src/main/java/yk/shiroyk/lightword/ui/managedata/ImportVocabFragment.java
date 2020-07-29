package yk.shiroyk.lightword.ui.managedata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;
import yk.shiroyk.lightword.utils.VocabularyDataManage;


public class ImportVocabFragment extends Fragment {

    private static final String TAG = "ImportVocabFragment";
    private static int REQUEST_VOCABULARY = 10001;

    private SharedViewModel sharedViewModel;

    private Context context;
    private TextView tv_vocab_msg;
    private ListView vocab_list;

    private VocabularyRepository vocabularyRepository;
    private VocabularyDataManage vocabularyDataManage;

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
        vocabularyRepository.getCount().observe(getViewLifecycleOwner(),
                integer -> sharedViewModel.setSubTitle("共" + integer + "条"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sharedViewModel.setSubTitle("");
    }

    private void init(View root) {
        tv_vocab_msg = root.findViewById(R.id.tv_vocab_msg);
        vocab_list = root.findViewById(R.id.vocab_list);

        root.findViewById(R.id.fab_import_vocab).setOnClickListener(view -> pickVocabulary());
    }

    public void pickVocabulary() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        this.startActivityForResult(intent, REQUEST_VOCABULARY);
    }

    private void setVocabList() {
        vocabularyRepository.getWordString().observe(getViewLifecycleOwner(), wordList -> {
            if (wordList.size() > 0) {
                vocab_list.setVisibility(View.VISIBLE);
                tv_vocab_msg.setVisibility(View.GONE);
                ArrayAdapter adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, wordList);
                vocab_list.setAdapter(adapter);
                vocab_list.setFastScrollEnabled(true);
            } else {
                vocab_list.setVisibility(View.GONE);
                tv_vocab_msg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (requestCode == REQUEST_VOCABULARY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                FileUtils fileUtils = new FileUtils(context);
                uri = data.getData();
                List<String> wordString = vocabularyRepository.getWordStringIM();
                Integer lines = fileUtils.countLines(uri);
                new ImportVocabFragment.ImportVocabulary(lines, wordString).execute(uri);

            }
        }
    }

    public class ImportVocabulary extends AsyncTask<Uri, Void, List<Vocabulary>> {
        int count = 0;
        int lines;
        int overWrite = 0;
        private AlertDialog loadingDialog;
        private List<String> wordString;

        public ImportVocabulary(int lines, List<String> wordString) {
            this.lines = lines;
            this.wordString = wordString;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            loadingDialog = builder.setCancelable(false)
                    .setView(R.layout.layout_loading).create();
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected List<Vocabulary> doInBackground(Uri... uri) {
            List<Vocabulary> vocabularyList = new ArrayList<>();
            try {
                InputStream is = context.getContentResolver().openInputStream(uri[0]);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    String[] line = str.split(",", 3);
                    if (line.length > 1) {
                        String word = line[0];
                        if (!wordString.contains(word)) {
                            Vocabulary vocabulary = new Vocabulary();
                            long frequency = Long.parseLong(line[1]);
                            vocabulary.setWord(word);
                            vocabulary.setFrequency(frequency);
                            vocabularyList.add(vocabulary);
                            vocabularyDataManage.writeFile(line[2], word);
                        } else {
                            overWrite += 1;
                            vocabularyDataManage.overWriteFile(line[2], word);
                        }
                    }
                }
                count = vocabularyList.size();
                Log.d(TAG, "词汇例句初始化数量: " + vocabularyList.size() + "/" + lines);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return vocabularyList;
        }

        @Override
        protected void onPostExecute(List<Vocabulary> result) {
            String msg;
            if (result.size() > 0) {
                msg = "词库数据导入成功，共导入" + result.size() + "条数据";
                msg += overWrite > 0 ? ", 覆盖" + overWrite + "条数据" : "";
                Vocabulary[] vocabularies = result.toArray(new Vocabulary[count]);
                vocabularyRepository.insert(vocabularies);
            } else {
                if (overWrite > 0) {
                    msg = "词库数据导入成功，共覆盖" + overWrite + "条数据";
                } else {
                    msg = "解析失败，未导入数据";
                }
            }
            loadingDialog.dismiss();
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}