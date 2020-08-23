package yk.shiroyk.lightword.ui.managedata;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.ui.adapter.VocabularyAdapter;


public class MasterWordFragment extends Fragment {

    private Context context;
    private ExerciseRepository exerciseRepository;
    private VocabTypeRepository vocabTypeRepository;
    private MasterWordViewModel idViewModel;
    private SharedPreferences sp;

    private CompositeDisposable disposable;

    private VocabularyAdapter adapter;
    private ProgressBar master_card_loading;
    private TextView tv_master_card_msg;
    private FastScrollRecyclerView master_card_list;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        idViewModel = ViewModelProviders.of(this).get(MasterWordViewModel.class);
        disposable = new CompositeDisposable();
        context = getContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_master_word, container, false);
        setHasOptionsMenu(true);
        init(root);
        setWordList();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        SubMenu vType = menu.addSubMenu(R.string.import_vdata_submenu);
        vocabTypeRepository.getAllVocabType().observe(getViewLifecycleOwner(),
                vocabTypes -> {
                    Long prfType = Long.valueOf(sp.getString("vtypeId", "1"));
                    idViewModel.setVTypeId(prfType);
                    MenuItem searchMenuItem = menu.findItem(R.id.action_search);
                    SearchView searchView = (SearchView) searchMenuItem.getActionView();
                    if (vocabTypes.size() > 0) {
                        if (vocabTypes.size() > vType.size()) {
                            vType.clear();
                            for (VocabType v : vocabTypes) {
                                MenuItem item = vType.add(v.getVocabtype());
                                if (v.getId().equals(prfType)) {
                                    item.setChecked(true);
                                }
                                item.setOnMenuItemClickListener(menuItem -> {
                                    item.setChecked(true);
                                    idViewModel.setVTypeId(v.getId());
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
        idViewModel.getVTypeId().observe(getViewLifecycleOwner(), id -> {
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
                    exerciseRepository.searchMasterWord(id,
                            "%" + newText + "%").observe(
                            getViewLifecycleOwner(), words -> {
                                if (words == null) return;
                                adapter.setWords(words);
                            });
                }
            });
        });
    }

    private void init(View root) {
        master_card_loading = root.findViewById(R.id.master_card_loading);
        tv_master_card_msg = root.findViewById(R.id.tv_master_card_msg);
        master_card_list = root.findViewById(R.id.master_card_list);
    }

    private void setWordList() {
        idViewModel.getVTypeId().observe(getViewLifecycleOwner(), id ->
                exerciseRepository.getMasterWord(id)
                        .observe(getViewLifecycleOwner(), vList -> {
                            if (vList.size() > 0) {
                                master_card_list.setVisibility(View.VISIBLE);
                                tv_master_card_msg.setVisibility(View.GONE);
                                adapter = new VocabularyAdapter(
                                        context, vList, vocabulary ->
                                        disposable.add(Observable.create((ObservableOnSubscribe<ExerciseData>) emitter -> {
                                            emitter.onNext(exerciseRepository.getWordDetail(vocabulary.getId(), id));
                                            emitter.onComplete();
                                        })
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(data -> {
                                                    delMasterWord(vocabulary.getWord(), data);
                                                })));
                                master_card_list.setLayoutManager(new LinearLayoutManager(context));
                                master_card_list.setAdapter(adapter);
                            } else {
                                master_card_list.setVisibility(View.GONE);
                                tv_master_card_msg.setVisibility(View.VISIBLE);
                            }
                            master_card_loading.setVisibility(View.GONE);
                        })
        );
    }

    private void delMasterWord(String title, ExerciseData data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(R.string.delete_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateWord(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void updateWord(ExerciseData data) {
        data.setTimestamp(new Date());
        data.setStage(1);
        exerciseRepository.update(data);
        Toast.makeText(context, "删除成功！", Toast.LENGTH_SHORT).show();
    }
}