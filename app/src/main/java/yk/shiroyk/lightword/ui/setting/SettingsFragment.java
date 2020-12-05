/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.ui.setting;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ListView;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.constant.Constant;
import yk.shiroyk.lightword.db.constant.ThemeEnum;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabExerciseData;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.ColorPickAdapter;
import yk.shiroyk.lightword.utils.ThemeHelper;
import yk.shiroyk.lightword.utils.ThreadTask;
import yk.shiroyk.lightword.utils.VocabFileManage;

public class SettingsFragment extends PreferenceFragmentCompat {

    private VocabTypeRepository vocabTypeRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabFileManage vocabFileManage;
    private ExerciseRepository exerciseRepository;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabFileManage = new VocabFileManage(getContext());
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void init() {
        materialSingleChoiceDialog("themePref", R.array.themeArray, newValue ->
                ThemeHelper.applyTheme(ThemeEnum.values()[newValue]));

        Preference primaryColor = findPreference("primaryColor");
        primaryColor.setOnPreferenceClickListener(preference -> {
            setColorPickView(preference.getSharedPreferences());
            return false;
        });

        SwitchPreferenceCompat navBgPreference = findPreference("navigationBarBg");
        navBgPreference.setOnPreferenceClickListener(preference -> {
            ThemeHelper.setNavigationBarColor(getActivity(), preference.getSharedPreferences());
            return false;
        });

        choiceVTypeDialog();

        materialSingleChoiceDialog("isPronounce", R.array.pronounceArray, null);

        EditTextPreference targetPreference = findPreference("dailyTarget");
        targetPreference.setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_CLASS_NUMBER));

        EditTextPreference cardQuantity = findPreference("cardQuantity");
        cardQuantity.setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_CLASS_NUMBER));

        materialSingleChoiceDialog("ttsSpeech", R.array.ttsArray, null);

        Preference systemTTS = findPreference("systemTTS");
        systemTTS.setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception ignored) {
            }
            return false;
        });

        Preference backupVocabulary = findPreference("backupVocabulary");
        backupVocabulary.setOnPreferenceClickListener(preference -> {
            selectVTypeDialog(vType -> {
                writeBackupFile(
                        vType.getVocabtype(),
                        vType, false);
            });
            return false;
        });

        Preference copyExercise = findPreference("copyExercise");
        copyExercise.setOnPreferenceClickListener(preference -> {
            copyExerciseDialog();
            return false;
        });

        Preference backupExercise = findPreference("backupExercise");
        backupExercise.setOnPreferenceClickListener(preference -> {
            selectVTypeDialog(vType -> {
                SimpleDateFormat format = new SimpleDateFormat("-MM-dd-HH-mm", Locale.CHINA);
                writeBackupFile(
                        vType.getVocabtype() + "-" +
                                getString(R.string.app_name) +
                                "-backup-" +
                                format.format(new Date()),
                        vType, true);
            });
            return false;
        });

        Preference importExercise = findPreference("importExercise");
        importExercise.setOnPreferenceClickListener(preference -> {
            pickVocabExercise();
            return false;
        });

    }

    private void materialSingleChoiceDialog(String key, @ArrayRes int id, Consumer<Integer> consumer) {
        Preference preference = findPreference(key);
        CharSequence[] stringArray = getResources().getStringArray(id);
        AtomicInteger position = new AtomicInteger(getPreferenceScreen().getSharedPreferences().getInt(key, 0));
        preference.setSummary(stringArray[position.get()]);

        preference.setOnPreferenceClickListener(pref -> {
            position.set(pref.getSharedPreferences().getInt(key, 0));
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(preference.getTitle())
                    .setSingleChoiceItems(stringArray, position.get(), (dialogInterface, i) -> {
                        position.set(i);
                        pref.getSharedPreferences().edit().putInt(key, i).apply();
                        if (consumer != null)
                            consumer.accept(i);
                        pref.setSummary(stringArray[i]);
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .create().show();
            return false;
        });
    }

    private void choiceVTypeDialog() {
        Preference vtypePreference = findPreference("vtypeId");
        ThreadTask.runOnThread(() -> vocabTypeRepository.getAllVocabTypes(), vList -> {
            if (vList.size() > 0) {
                Long vtypeId = vtypePreference.getSharedPreferences().getLong("vtypeId", 1L);
                AtomicInteger position = new AtomicInteger(0);
                for (int i = 0; i < vList.size(); i++) {
                    if (vtypeId.equals(vList.get(i).getId())) {
                        vtypePreference.setSummary(vList.get(i).getVocabtype());
                        position.set(i);
                    }
                }
                ArrayAdapter<VocabType> arrayAdapter = new ArrayAdapter<>
                        (getContext(), R.layout.item_dialog_singlechoice);
                arrayAdapter.addAll(vList);
                vtypePreference.setOnPreferenceClickListener(preference -> {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.select_vocab_type)
                            .setSingleChoiceItems(arrayAdapter, position.get(), (dialogInterface, i) -> {
                                VocabType vocabType = arrayAdapter.getItem(i);
                                position.set(i);
                                preference.getSharedPreferences().edit()
                                        .putLong("vtypeId", vocabType.getId()).apply();
                                preference.setSummary(vocabType.getVocabtype());
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .create().show();
                    return false;
                });
            } else {
                vtypePreference.setSummary("未设置");
            }
        });
    }

    private void setColorPickView(SharedPreferences sp) {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_color_picker, null);

        GridView gridView = dialogView.findViewById(R.id.color_grid);
        ColorPickAdapter adapter = new ColorPickAdapter(
                getContext(),
                R.layout.item_color_btn,
                getResources().obtainTypedArray(R.array.primary_colors));
        gridView.setAdapter(adapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext(), R.style.Custom_MaterialAlertDialog).
                setTitle("选择主色调")
                .setView(dialogView)
                .setNeutralButton(R.string.dialog_default, (dialogInterface, i) -> {
                    sp.edit().putString("primaryColor", "default").apply();
                    dialogInterface.dismiss();
                    getActivity().recreate();
                })
                .setPositiveButton(R.string.dialog_cancel, null).create();
        adapter.setOnColorClickedListener(newTheme -> {
            sp.edit().putString("primaryColor", newTheme).apply();
            dialog.dismiss();
            getActivity().recreate();
        });
        dialog.show();
    }

    private void countDown(Runnable runnable, int millis) {
        new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                runnable.run();
            }
        }.start();
    }

    private void insertModeDialog(@StringRes int title, Consumer<Boolean> consumer) {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_insert_mode, null);
        MaterialRadioButton insert = dialogView.findViewById(R.id.radio_only_insert);
        MaterialRadioButton insertUpdate = dialogView.findViewById(R.id.radio_insert_update);

        if (R.string.copy_mode == title) {
            insert.setText(R.string.only_copy);
            insertUpdate.setText(R.string.copy_and_update);
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    consumer.accept(insert.isChecked());
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void copyExercise(Boolean mode, Long oldVType, Long newVType, String vTypeName) {
        Snackbar snackbar = Snackbar.make(getView(), "复制中....", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        ThreadTask.runOnThread(() -> vocabularyRepository.getWordList(newVType), vList -> {
            exerciseRepository.copyExerciseData(mode, oldVType, newVType, vList).observe(
                    getViewLifecycleOwner(), size -> {
                        String msg;
                        if (mode) msg = String.format(
                                getString(R.string.success_copy_exercise),
                                size[0], vTypeName);
                        else msg = String.format(
                                getString(R.string.success_copy_update_exercise),
                                size[0], size[1], vTypeName);
                        snackbar.setText(msg);
                        countDown(snackbar::dismiss, 1000);
                    });
        });

    }

    private void copyExerciseDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.layout_copy_vocab_exercise, null);
        AutoCompleteTextView tv_oldVType = dialogView.findViewById(R.id.old_vType);
        AutoCompleteTextView tv_newVType = dialogView.findViewById(R.id.new_vType);

        ArrayAdapter<VocabType> arrayAdapter = new ArrayAdapter<>
                (getContext(), android.R.layout.simple_list_item_single_choice);
        vocabTypeRepository.getAllVocabType().observe(getViewLifecycleOwner(), vTypes -> {
            arrayAdapter.addAll(vTypes);
            tv_oldVType.setAdapter(arrayAdapter);
            tv_newVType.setAdapter(arrayAdapter);
            AtomicReference<VocabType> oldVType = new AtomicReference<>();
            AtomicReference<VocabType> newVType = new AtomicReference<>();
            tv_oldVType.setOnItemClickListener((adapterView, view, i, l) ->
                    oldVType.set((VocabType) adapterView.getItemAtPosition(i)));
            tv_newVType.setOnItemClickListener((adapterView, view, i, l) ->
                    newVType.set((VocabType) adapterView.getItemAtPosition(i)));

            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.select_vocab_type)
                    .setView(dialogView)
                    .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                        if (oldVType.get() == null || newVType.get() == null) {
                            Snackbar.make(getView(), "未选择分类！", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        insertModeDialog(R.string.copy_mode, mode -> {
                            copyExercise(
                                    mode,
                                    oldVType.get().getId(),
                                    newVType.get().getId(),
                                    newVType.get().getVocabtype());
                        });
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .create().show();
        });
    }

    private void writeBackupFile(String name, VocabType vType, Boolean isExercise) {
        Snackbar snackbar = Snackbar.make(getView(), "备份中...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        ThreadTask.runOnThread(() -> {
            int size;
            try {
                OutputStream outputStream;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = getContext().getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".csv");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
                    Uri backup = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
                    outputStream = resolver.openOutputStream(Objects.requireNonNull(backup));
                } else {
                    File document = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    if (!document.exists()) document.mkdir();
                    File backup = new File(document, name + ".csv");
                    outputStream = new FileOutputStream(backup);
                }

                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                try {
                    if (isExercise) {
                        Set<VocabExerciseData> vList = exerciseRepository.getVocabExerciseDataSet(vType.getId());

                        String header = "word,timestamp,last_practice," +
                                "stage,correct,wrong," + vType.getVocabtype() +
                                ",Backup generated by LightWord \n";
                        bufferedOutputStream.write(header.getBytes());
                        bufferedOutputStream.flush();
                        for (VocabExerciseData vData : vList) {
                            String line = vData.word + ","
                                    + vData.timestamp.getTime() + ","
                                    + vData.last_practice.getTime() + ","
                                    + vData.stage + ","
                                    + vData.correct + ","
                                    + vData.wrong + "\n";
                            bufferedOutputStream.write(line.getBytes());
                            bufferedOutputStream.flush();
                        }
                        size = vList.size();
                    } else {
                        Set<Vocabulary> vList = vocabularyRepository.getWordSet(vType.getId());

                        String header = "word,frequency,json," + vType.getVocabtype() +
                                ",Vocabulary generated by LightWord\n";
                        bufferedOutputStream.write(header.getBytes());
                        bufferedOutputStream.flush();

                        for (Vocabulary vocab : vList) {
                            String line = vocab.getWord() + ","
                                    + vocab.getFrequency() + ","
                                    + vocabFileManage.readFile(vType.getId()
                                    , vocab.getWord()).trim() + "\n";
                            bufferedOutputStream.write(line.getBytes());
                            bufferedOutputStream.flush();
                        }
                        size = vList.size();
                    }
                    outputStream.close();
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return -1;
            }
            return size;
        }, size -> {
            if (size == null) snackbar.setText("备份失败！");
            else if (size == -1) snackbar.setText("备份失败，无文件写入权限！");
            else snackbar.setText("备份到" + Environment.DIRECTORY_DOCUMENTS + "已完成！共备份" + size + "条");
            countDown(snackbar::dismiss, 2000);
        });

    }

    private void selectVTypeDialog(Consumer<VocabType> consumer) {
        ArrayAdapter<VocabType> arrayAdapter = new ArrayAdapter<>
                (getContext(), R.layout.item_dialog_singlechoice);
        vocabTypeRepository.getAllVocabType().observe(getViewLifecycleOwner(), vTypes -> {
            arrayAdapter.addAll(vTypes);
            AtomicReference<VocabType> vTypeAtomic = new AtomicReference<>();
            ListView view = new ListView(getContext());
            view.setAdapter(arrayAdapter);
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.select_vocab_type)
                    .setSingleChoiceItems(arrayAdapter, 0, (dialogInterface, i) -> {
                        vTypeAtomic.set(arrayAdapter.getItem(i));
                    })
                    .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                        if (vTypeAtomic.get() == null)
                            vTypeAtomic.set(arrayAdapter.getItem(0));
                        consumer.accept(vTypeAtomic.get());
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .create().show();
        });
    }

    private void pickVocabExercise() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        this.startActivityForResult(intent, Constant.REQUEST_VOCAB_EXERCISE);
    }

    private void importVocabExercise(Boolean mode, Uri uri) {
        Snackbar snackbar = Snackbar.make(getView(), "导入备份中...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        ThreadTask.runOnThread(() -> {
            try {
                InputStream is = getContext().getContentResolver().openInputStream(uri);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String header = bufferedReader.readLine();
                if (!header.contains("Backup generated by LightWord")) return "解析失败，不是备份文件！";
                VocabType vType = vocabTypeRepository.getVocabType(header.split(",", 8)[6].trim());
                if (vType == null) return "词汇分类不存在，无法导入练习数据！";

                Map<String, Long> wordIdMap = vocabularyRepository.getWordIdMap(vType.getId());
                Set<ExerciseData> dataList = new HashSet<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] data = line.split(",", 6);
                    try {
                        ExerciseData exerciseData = new ExerciseData();
                        exerciseData.setWordId(wordIdMap.get(data[0]));
                        exerciseData.setVtypeId(vType.getId());
                        exerciseData.setTimestamp(new Date(Long.parseLong(data[1])));
                        exerciseData.setLastPractice(new Date(Long.parseLong(data[2])));
                        exerciseData.setStage(Integer.parseInt(data[3]));
                        exerciseData.setCorrect(Integer.parseInt(data[4]));
                        exerciseData.setWrong(Integer.parseInt(data[5]));
                        dataList.add(exerciseData);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Integer[] result = exerciseRepository
                        .insertOrUpdate(mode, vType.getId(), dataList);
                if (result[0] > 0 && result[1] == 0)
                    return String.format("成功导入备份！共导入%d条数据", result[0]);
                else if (result[0] > 0 && result[1] > 0)
                    return String.format("成功导入备份！新增%d条、覆盖%d条", result[0], result[1]);
                else if (result[1] != null && result[1] > 0)
                    return String.format("成功导入备份！共覆盖%d条", result[1]);
                else
                    return "共导入0条数据！";
            } catch (IOException ex) {
                ex.printStackTrace();
                return "导入备份失败！";
            }
        }, msg -> {
            snackbar.setText(msg);
            countDown(snackbar::dismiss, 2000);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_VOCAB_EXERCISE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                insertModeDialog(R.string.insert_mode, mode -> {
                    importVocabExercise(mode, data.getData());
                });
            }
        }
    }
}
