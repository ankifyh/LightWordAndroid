package yk.shiroyk.lightword.ui.managedata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.VocabDataRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.FileUtils;


public class ImportVdataFragment extends Fragment {

    private static int REQUEST_VOCABDATA = 10002;
    private boolean firstLoad = true;

    private SharedViewModel sharedViewModel;
    private ImportVdataViewModel vdataViewModel;

    private Context context;
    private TextView tv_vdata_msg;
    private ListView vdata_list;

    private VocabTypeRepository vocabTypeRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabDataRepository vocabDataRepository;

    private VocabType defualtVocabType;
    private TitleString title = (VocabType v) -> v.getVocabtype() + " (" + v.getAmount() + ")";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vdataViewModel = ViewModelProviders.of(this).get(ImportVdataViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        vocabDataRepository = new VocabDataRepository(getActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_vdata, container, false);

        context = root.getContext();

        setHasOptionsMenu(true);
        init(root);

        setVdataList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstLoad) {
            vdataViewModel.setWordMap(vocabularyRepository.getIdToWordMap());
            setDefaultTitle();
            firstLoad = false;
        }
        sharedViewModel.setSubTitle(title.get(defualtVocabType));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        firstLoad = true;
        sharedViewModel.setSubTitle("");
    }

    private void setDefaultTitle() {
        List<VocabType> v = vocabTypeRepository.getAllVocabType();
        if (v.size() > 0) {
            defualtVocabType = v.get(0);
            sharedViewModel.setSubTitle(title.get(defualtVocabType));
            vdataViewModel.setWordList(vocabDataRepository.getAllWordId(defualtVocabType.getId()));
        } else {
            defualtVocabType = new VocabType();
            defualtVocabType.setVocabtype("");
            defualtVocabType.setAmount(0);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        List<VocabType> vocabTypes = vocabTypeRepository.getAllVocabType();
        SubMenu vType = menu.addSubMenu("词汇数据");

        for (final VocabType v : vocabTypes) {
            final String s = title.get(v);
            final MenuItem item = vType.add(s);
            if (v.getId() == 1L) {
                item.setChecked(true);
            }
            item.setOnMenuItemClickListener(menuItem -> {
                item.setChecked(true);
                sharedViewModel.setSubTitle(s);
                vdataViewModel.setWordList(vocabDataRepository.getAllWordId(v.getId()));
                return false;
            });
        }

        vType.setGroupCheckable(0, true, true);
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

//    @Override
//    public void onPrepareOptionsMenu(@NonNull Menu menu) {
//        MenuItem item = menu.findItem(R.id.action_search);
//        if(item != null)
//            item.setVisible(true);
//    }

    private void setVdataList() {
        vdataViewModel.getWordList().observe(getViewLifecycleOwner(), strings -> {
            if (strings.size() > 0) {
                vdata_list.setVisibility(View.VISIBLE);
                tv_vdata_msg.setVisibility(View.GONE);
                ArrayAdapter adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, strings);
                vdata_list.setAdapter(adapter);
                vdata_list.setFastScrollEnabled(true);
            } else {
                vdata_list.setVisibility(View.GONE);
                tv_vdata_msg.setVisibility(View.VISIBLE);
            }
        });
    }

    private void init(View root) {
        tv_vdata_msg = root.findViewById(R.id.tv_vdata_msg);
        vdata_list = root.findViewById(R.id.vdata_list);

        root.findViewById(R.id.fab_import_vdata).setOnClickListener(view -> {
            pickVocabData();
        });
    }

    public void pickVocabData() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        this.startActivityForResult(intent, REQUEST_VOCABDATA);
    }

    public VocabType insertVocabType(String filename) throws ExecutionException, InterruptedException {
        VocabType vocabType = new VocabType();
        vocabType.setVocabtype(filename);
        vocabType.setAmount(0);
        vocabType.setId(vocabTypeRepository.vtypeInsert(vocabType));
        return vocabType;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        if (requestCode == REQUEST_VOCABDATA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uri = data.getData();
                FileUtils fileUtils = new FileUtils(context);
                Integer lines = fileUtils.countLines(uri);
                Map<String, Long[]> wordMap = vocabularyRepository.getWordToFrequencyMap();
                String fname = fileUtils.getFileName(uri);
                try {
                    VocabType vocabType = insertVocabType(fname);
                    vocabType.setAmount(lines);
                    new ImportVdataFragment.ImportVocabData(vocabType, wordMap).execute(uri);
                    Log.d("vtypeId", vocabType.getId() + " Count:" + vocabType.getAmount());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private interface TitleString {
        String get(VocabType v);
    }

    public class ImportVocabData extends AsyncTask<Uri, Void, VocabData[]> {
        int count = 0;
        int lines = 0;
        VocabType vocabType;
        Map<String, Long[]> wordMap;
        private AlertDialog loadingDialog;

        public ImportVocabData(VocabType vocabType, Map<String, Long[]> wordMap) {
            this.vocabType = vocabType;
            this.lines = vocabType.getAmount();
            this.wordMap = wordMap;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            loadingDialog = builder.setCancelable(false)
                    .setView(R.layout.layout_loading).create();
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected VocabData[] doInBackground(Uri... uri) {
            List<VocabData> vocabDataList = new ArrayList<>();
            VocabData[] vocabData = new VocabData[lines];
            try {
                InputStream is = context.getContentResolver().openInputStream(uri[0]);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    String line = str.trim();
                    if (!line.isEmpty()) {
                        VocabData v = new VocabData();
                        Long[] data = wordMap.get(line);
                        if (data != null) {
                            v.setWordId(data[0]);
                            v.setFrequency(data[1]);
                            v.setVtypeId(vocabType.getId());
                            vocabDataList.add(v);
                        }
                    }
                }
                count = vocabDataList.size();
                Log.d("词汇数据初始化数量", count + "/" + lines);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return vocabDataList.toArray(new VocabData[count]);
        }

        @Override
        protected void onPostExecute(VocabData[] result) {
            String msg;
            if (count > 0) {
                vocabDataRepository.insert(result);
                vocabType.setAmount(count);
                vocabTypeRepository.update(vocabType);
                if (vdataViewModel.getWordMapSize() == 0) {
                    vdataViewModel.setWordMap(vocabularyRepository.getIdToWordMap());
                }
                vdataViewModel.setVocabData(result);
                defualtVocabType = vocabType;
                sharedViewModel.setSubTitle(title.get(defualtVocabType));
                msg = "词汇数据导入成功，共导入" + count + "条数据";
            } else {
                msg = "解析失败，未导入数据";
            }
            loadingDialog.dismiss();
            Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}