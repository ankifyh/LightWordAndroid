/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.ui.managedata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import yk.shiroyk.lightword.MainActivity;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.constant.OrderEnum;
import yk.shiroyk.lightword.db.constant.VocabFilterEnum;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabExercise;
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
import yk.shiroyk.lightword.utils.VocabFileManage;


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
    private VocabFileManage vocabFileManage;
    private ExerciseRepository exerciseRepository;
    private SharedPreferences sp;

    private TextInputEditText et_vocab;
    private TextInputEditText et_frequency;
    private TextInputEditText et_pronounce;
    private VocabDetailAdapter vocabDetailAdapter;
    private ExampleDetailAdapter exampleDetailAdapter;
    private ExerciseList exerciseList;

    private VocabType defaultVType;
    private OrderEnum orderBy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        vocabViewModel = new VocabViewModel(getActivity().getApplication());
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        vocabFileManage = new VocabFileManage(getActivity().getBaseContext());
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage_vocab, container, false);
        context = root.getContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        setHasOptionsMenu(true);
        init(root);
        setWordList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        vocabViewModel.getVocabType().observe(getViewLifecycleOwner(),
                vocabType -> defaultVType = vocabType);
        vocabViewModel.getOrderBy().observe(getViewLifecycleOwner(),
                orderEnum -> orderBy = orderEnum);
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
            initialDefaultVType(vocabTypes);
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            doneMenuItem = menu.findItem(R.id.action_done);
            newVocab = menu.findItem(R.id.action_create_vocab);
            MenuItem vocabOrder = menu.findItem(R.id.action_vocab_order);
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
                setSearchWordView(searchView, this::searchWord);
                masterSelectItem.setTitle(R.string.mastered_word);
                vocabViewModel.setVocabFilter(VocabFilterEnum.All);
                return false;
            });

            reviewVocabItem.setOnMenuItemClickListener(menuItem -> {
                vocabViewModel.setVocabFilter(VocabFilterEnum.Review);
                setSearchWordView(searchView, this::searchReviewWord);
                masterSelectItem.setTitle(R.string.mastered_word);
                return false;
            });

            masterItem.setOnMenuItemClickListener(menuItem -> {
                vocabViewModel.setVocabFilter(VocabFilterEnum.Master);
                setSearchWordView(searchView, this::searchMasterWord);
                masterSelectItem.setTitle(R.string.demaster_word);
                return false;
            });

            vocabViewModel.getVocabFilter().observe(getViewLifecycleOwner(), filter -> {
                switch (filter) {
                    case All:
                        allVocabItem.setChecked(true);
                        break;
                    case Review:
                        reviewVocabItem.setChecked(true);
                        break;
                    case Master:
                        masterItem.setChecked(true);
                        break;
                }
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
                            masterSelectItem.setTitle(R.string.mastered_word);
                            vocabViewModel.setVocabType(v);
                            return false;
                        });
                    }
                    vType.setGroupCheckable(0, true, true);
                }
                allVocabItem.setChecked(true);
                searchMenuItem.setVisible(true);
                setSearchWordView(searchView, this::searchWord);
                menu.findItem(R.id.action_master_select_word).setVisible(true);
                vocabOrder.setVisible(true);
                menu.findItem(R.id.action_vocab_order_word).setChecked(true);
                menu.setGroupVisible(R.id.vocab_data_menu_group, true);
                exitSelectModeRefresh();
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
                exitSelectModeRefresh();
                return true;
            case R.id.action_master_select_word:
                if (getString(R.string.mastered_word).contentEquals(item.getTitle())) {
                    masterWordDialog(adapter.getSelectedItem());
                } else {
                    deMasterWord(adapter.getSelectedItem());
                }
                return true;
            case R.id.action_collect_select_word:
                collectVocabDialog();
                return true;
            case R.id.action_delete_select_word:
                deleteSelectVocabDialog();
                return true;
            case R.id.action_vocab_order_word:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.Word);
                return true;
            case R.id.action_vocab_order_frequency:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.Frequency);
                return true;
            case R.id.action_vocab_order_correct:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.Correct);
                return true;
            case R.id.action_vocab_order_wrong:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.Wrong);
                return true;
            case R.id.action_vocab_order_last_practice:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.LastPractice);
                return true;
            case R.id.action_vocab_order_timestamp:
                item.setChecked(true);
                vocabViewModel.setOrderBy(OrderEnum.Timestamp);
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

    private void initialDefaultVType(List<VocabType> vocabTypes) {
        if (vocabTypes.size() > 0) {
            if (defaultVType == null) {
                long vtypePref = sp.getLong("vtypeId", 1L);
                for (VocabType v : vocabTypes) {
                    if (v.getId().equals(vtypePref)) {
                        defaultVType = v;
                        vocabViewModel.setVocabType(v);
                        vocabViewModel.setVocabFilter(VocabFilterEnum.All);
                        vocabViewModel.setOrderBy(OrderEnum.Word);
                        observeLoading();
                    }
                }
            }
        } else {
            tv_vocab_msg.setVisibility(View.VISIBLE);
            setVocabLoading(false);
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

    private void setSearchWordView(SearchView searchView, Consumer<String> searchWord) {
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
                searchWord.accept(newText);
            }
        });
    }

    private void searchWord(String word) {
        vocabularyRepository.searchWord(
                "%" + word + "%", defaultVType.getId()).observe(
                getViewLifecycleOwner(), words -> {
                    if (words == null) return;
                    adapter.setWords(words);
                });
    }

    private void searchReviewWord(String word) {
        vocabularyRepository.searchReviewWord(
                "%" + word + "%", defaultVType.getId()).observe(
                getViewLifecycleOwner(), words -> {
                    if (words == null) return;
                    adapter.setWords(words);
                });
    }

    private void searchMasterWord(String word) {
        vocabularyRepository.searchMasterWord(
                "%" + word + "%", defaultVType.getId()).observe(
                getViewLifecycleOwner(), words -> {
                    if (words == null) return;
                    adapter.setWords(words);
                });
    }


    private void enterSelectMode() {
        doneMenuItem.setVisible(true);
        newVocab.setVisible(false);
    }

    private void exitSelectMode() {
        doneMenuItem.setVisible(false);
        newVocab.setVisible(true);
        if (adapter != null)
            adapter.exitMultiSelectMode();
    }

    private void exitSelectModeRefresh() {
        doneMenuItem.setVisible(false);
        vocabViewModel.setOrderBy(orderBy);
        newVocab.setVisible(true);
        if (adapter != null)
            adapter.exitMultiSelectMode();
    }

    /**
     * Modify Vocabulary Data
     * ⬇️
     */

    private void editVocabDialog(String title, VocabExercise vocab) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

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

        if (vocab != null) {
            et_vocab.setText(vocab.word);
            et_frequency.setText(String.valueOf(vocab.frequency));
            try {
                String ex = vocabFileManage
                        .readFile(vocab.vtypeId, vocab.word);
                exerciseList = new Gson().fromJson(ex, ExerciseList.class);
                et_pronounce.setText(exerciseList.getPronounceString());
                if (exerciseList.getCollocation() != null)
                    vocabDetailAdapter.setCollocations(exerciseList.getCollocation());
            } catch (Exception ignored) {

            }

            builder.setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                delVocabDialog(vocab);
            });
        } else {
            exerciseList = new ExerciseList();
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
                    exerciseList.setSource(getString(R.string.app_name));
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
        new MaterialAlertDialogBuilder(context)
                .setTitle(String.format(getString(R.string.vocab_exist), w))
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

        long fre;
        try {
            fre = Long.parseLong(et_frequency.getText().toString().trim());
        } catch (Exception ignored) {
            fre = 0;
        }
        Vocabulary vocab = new Vocabulary(w, defaultVType.getId(), fre);
        if (newVocab) {
            vocabularyRepository.insert(vocab, l -> {
                if (l > 0) {
                    vocabFileManage.overWriteFile(json, defaultVType.getId(), w);
                    // update vocab type amount
                    defaultVType.plusAmount(1);
                    vocabTypeRepository.updateOnThread(defaultVType);

                    // refresh option menu
                    getActivity().invalidateOptionsMenu();

                    Toast.makeText(context, String.format(
                            getString(R.string.vocab_save_success), w), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            vocabularyRepository.update(vocab);
            vocabFileManage.overWriteFile(json, vocab.getVtypeId(), w);
            Toast.makeText(context, String.format(
                    getString(R.string.vocab_save_success), w), Toast.LENGTH_SHORT).show();
        }

    }

    private void editCollocationDialog(Collocation collocation) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

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

    private void delVocabDialog(VocabExercise vocab) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_single_vocab)
                .setMessage(String.format(getString(R.string.delete_single_vocab_msg), vocab.word))
                .setPositiveButton(R.string.dialog_delete, (dialogInterface, i) -> {
                    ThreadTask.runOnThread(() -> {
                        int size = vocabularyRepository.delete(new Vocabulary(vocab));
                        if (size > 0) {
                            vocabFileManage.deleteFile(defaultVType.getId(), vocab.word);
                            defaultVType.minusAmount(size);
                            vocabTypeRepository.update(defaultVType);
                            getActivity().invalidateOptionsMenu();
                        }
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void setCreateVTypeDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_create_vocab_type, null);
        TextInputEditText et_type = view.findViewById(R.id.et_type);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.create_vocab_type)
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
                                vocabTypeRepository.insert(v);
                                getActivity().invalidateOptionsMenu();
                                v = null;
                            }
                            return v;
                        }, v -> {
                            if (v == null) {
                                Snackbar.make(getView(), "新建分类成功！", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(getView(), "分类已存在！", Snackbar.LENGTH_SHORT).show();
                                vocabViewModel.setVocabType(v);
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void collectVocabDialog() {
        View view = getLayoutInflater().inflate(R.layout.layout_pick_vocab_type, null);
        AppCompatSpinner sp_type = view.findViewById(R.id.spinner_vType);
        ArrayAdapter<VocabType> arrayAdapter = new ArrayAdapter<>
                (context, android.R.layout.simple_spinner_dropdown_item);
        vocabTypeRepository.getAllVocabType().observe(getViewLifecycleOwner(), vTypes -> {
            arrayAdapter.addAll(vTypes);
            sp_type.setAdapter(arrayAdapter);
        });
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.select_vocab_type)
                .setView(view)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    VocabType vType = (VocabType) sp_type.getSelectedItem();
                    ThreadTask.runOnThread(() -> {
                        List<Vocabulary> vList = vocabularyRepository
                                .getWordListById(adapter.getSelectedItem(), defaultVType.getId());

                        for (Vocabulary vocab : vList) {
                            vocabFileManage.copyFile(
                                    defaultVType.getId(),
                                    vType.getId(),
                                    vocab.getWord());
                        }

                        int[] result = new int[2];
                        result[0] = vList.size();

                        Vocabulary[] vocabs = vocabularyRepository.collectNewVocab(vType.getId(), vList);

                        if (vocabs.length > 0) {
                            vocabularyRepository.insert(vocabs);

                            vType.setAmount(vocabularyRepository.countWord(vType.getId()));
                            vocabTypeRepository.update(vType);
                            getActivity().invalidateOptionsMenu();
                        }
                        result[1] = vocabs.length;

                        return result;
                    }, result -> {
                        String msg = String.format(getString(R.string.success_collect_vocab),
                                result[1],
                                vType.getVocabtype());
                        int overwrite = result[0] - result[1];
                        if (overwrite > 0) {
                            msg += String.format(
                                    getString(R.string.success_collect_vocab_overwrite),
                                    overwrite);
                        }
                        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void deleteSelectVocabDialog() {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_select_vocab)
                .setMessage(R.string.delete_select_vocab_msg)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    ThreadTask.runOnThread(() -> {
                        List<Vocabulary> vList = vocabularyRepository
                                .getWordListById(adapter.getSelectedItem(), defaultVType.getId());
                        int deleteSize = vList.size();
                        int size = vocabularyRepository.delete(vList.toArray(new Vocabulary[deleteSize]));
                        if (size > 0) {
                            for (Vocabulary vocab : vList) {
                                vocabFileManage.deleteFile(defaultVType.getId(), vocab.getWord());
                            }
                            defaultVType.setAmount(vocabularyRepository.countWord(defaultVType.getId()));
                            vocabTypeRepository.update(defaultVType);
                            getActivity().invalidateOptionsMenu();
                        }
                        return size;
                    }, size -> {
                        Snackbar.make(getView(), String.format(getString(R.string.success_delete_vocab),
                                defaultVType.getVocabtype(), size), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    /**
     * Modify mastered vocabulary
     * ⬇️
     */

    private void deMasterWord(String title, ExerciseData data) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(R.string.delete_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateExerciseData(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void deMasterWord(List<Long> data) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_select_master_word_title)
                .setMessage(R.string.delete_select_master_word_msg)
                .setNeutralButton(R.string.dialog_delete,
                        (dialogInterface, i) -> updateExerciseData(data))
                .setPositiveButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void masterWordDialog(List<Long> data) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.add_select_master_word_title)
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
        Snackbar.make(getView(), "成功添加" + idList.size() + "个!", Snackbar.LENGTH_SHORT).show();
    }

    private void updateExerciseData(ExerciseData data) {
        //reset exercise data
        data.setTimestamp(new Date());
        data.setStage(1);
        exerciseRepository.update(data);
        Snackbar.make(getView(), "删除成功!", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Set Vocabulary list
     * ⬇️
     */

    private void setVocabLoading(boolean isLoading) {
        if (isLoading) {
            vocab_loading.setVisibility(View.VISIBLE);
        } else {
            vocab_loading.setVisibility(View.GONE);
        }
        vocab_list.suppressLayout(isLoading);
    }

    private void setVocabList(List<VocabExercise> vList, OrderEnum orderEnum) {
        if (vList.size() > 0) {
            vocab_list.setVisibility(View.VISIBLE);
            tv_vocab_msg.setVisibility(View.GONE);
            adapter = new VocabularyAdapter(context, vList, orderEnum);
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
                    exitSelectModeRefresh();
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
        vocabViewModel.setIsLoading(false);
    }

    private void setMasterWordList(List<VocabExercise> vList, OrderEnum orderBy) {
        if (vList.size() > 0) {
            vocab_list.setVisibility(View.VISIBLE);
            tv_vocab_msg.setVisibility(View.GONE);
            adapter.setWords(vList);
            adapter.setOrder(orderBy);
            adapter.setOnInfoClickListener(vocabulary ->
                    ThreadTask.runOnThread(() -> exerciseRepository
                                    .getWordDetail(vocabulary.id, defaultVType.getId()),
                            data -> deMasterWord(vocabulary.word, data)));
            adapter.setOnLongClickListener(multiSelectMode -> {
                if (multiSelectMode) {
                    enterSelectMode();
                }
            });
            adapter.setOnSelectedChanged(size -> {
                //change done menu visible by selected size
                if (size == 0) {
                    exitSelectModeRefresh();
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
        vocabViewModel.setIsLoading(false);
    }

    private void setWordList() {
        vocabViewModel.getVocabExercise().observe(getViewLifecycleOwner(), vList ->
                vocabViewModel.getVocabFilter().observe(getViewLifecycleOwner(), filter ->
                        vocabViewModel.getOrderBy().observe(getViewLifecycleOwner(), order -> {
                            exitSelectMode();
                            switch (filter) {
                                case All:
                                    setVocabList(vList, order);
                                    sharedViewModel.setSubTitle(defaultVType.toString());
                                    break;
                                case Review:
                                    setVocabList(vList, order);
                                    sharedViewModel.setSubTitle(
                                            String.format(getString(R.string.review_vocab_size), vList.size()));
                                    break;
                                case Master:
                                    setMasterWordList(vList, order);
                                    sharedViewModel.setSubTitle(
                                            String.format(getString(R.string.master_word), vList.size()));
                                    break;
                            }
                        })));
    }

    private void observeLoading() {
        vocabViewModel.getIsLoading().observe(getViewLifecycleOwner(), this::setVocabLoading);
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
                });
        Toast.makeText(context, "删除成功!", Toast.LENGTH_SHORT).show();
    }

    private void setEnsureDialog(VocabType v, Uri uri) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.import_vocab_type_exist_title)
                .setMessage(R.string.import_vocab_type_exist_message)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) ->
                        importVocab(v, uri, true))
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void importVocab(VocabType vocabType, Uri uri, boolean overWrite) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        AlertDialog loadingDialog = builder.setCancelable(false)
                .setView(R.layout.layout_loading).create();

        loadingDialog.show();

        ThreadTask.runOnThread(() -> {
            Set<String> wordList = new HashSet<>();

            if (overWrite) {
                wordList = vocabularyRepository.getWordStringSet(vocabType.getId());
            }

            List<Vocabulary> vocabList = new ArrayList<>();

            int[] result = new int[2];
            //result[0] overwrite size
            //result[1] new insert size

            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

                String header = bufferedReader.readLine();
                if (header.contains("Backup generated by LightWord")) {
                    result[0] = -1;
                    return result;
                }

                String str;
                while ((str = bufferedReader.readLine()) != null) {
                    String[] line = str.split(",", 3);
                    String word = line[0];
                    try {
                        if (!TextUtils.isEmpty(word)) {
                            if (!wordList.contains(word)) {
                                long frequency = Long.parseLong(line[1]);
                                vocabList.add(new Vocabulary(word, vocabType.getId(), frequency));
                            } else {
                                result[0]++;
                            }
                            String data = line[2];
                            if (!TextUtils.isEmpty(data))
                                vocabFileManage.overWriteFile(line[2], vocabType.getId(), word);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                result[0] = -2;
                ex.printStackTrace();
            }

            result[1] = vocabList.size();

            if (result[1] > 0) {
                vocabularyRepository.insert(vocabList.toArray(new Vocabulary[result[1]]));
                vocabType.setAmount(vocabularyRepository.countWord(vocabType.getId()));
                int i = vocabTypeRepository.update(vocabType);
                if (i == 1) getActivity().invalidateOptionsMenu();
            }
            return result;
        }, result -> {

            //result[0] overwrite size
            //result[1] new insert size

            String msg = "共解析到0条数据！";
            if (result[0] == -1) msg = "导入失败，该文件是备份文件！";
            else if (result[0] == -2) msg = "解析失败，未导入数据！";
            else if (overWrite) {
                if (result[0] > 0) {
                    if (result[1] > 0)
                        msg = String.format("导入成功，共覆盖%d条、新增%d条数据", result[0], result[1]);
                    else msg = String.format("导入成功，共覆盖%d条数据", result[0]);
                } else {
                    if (result[1] > 0) msg = String.format("导入成功，共新增%d条", result[1]);
                }
            } else {
                if (result[1] > 0) msg = String.format("导入成功，共导入%d条数据", result[1]);
            }

            //delete invalid vocabType
            if (result[0] == -1 || result[0] == -2 || (result[0] == 0 && result[1] == 0)) {
                vocabTypeRepository.delete(vocabType, i -> {
                    if (i == 1) getActivity().invalidateOptionsMenu();
                });
            }
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
                String fname = FileUtils.getFileName(context.getContentResolver(), Objects.requireNonNull(uri));

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