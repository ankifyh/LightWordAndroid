package yk.shiroyk.lightword.ui.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.ui.adapter.ColorPickAdapter;
import yk.shiroyk.lightword.utils.ThemeHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Activity mActivity;

    private VocabTypeRepository vocabTypeRepository;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        mActivity = getActivity();
        vocabTypeRepository = new VocabTypeRepository(mActivity.getApplication());
        compositeDisposable = new CompositeDisposable();
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.dispose();
    }

    private void init() {
        ListPreference themePreference = findPreference("themePref");
        themePreference.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    String themeOption = (String) newValue;
                    ThemeHelper.applyTheme(themeOption);
                    return true;
                });

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

        compositeDisposable.add(setVTypeEntry());

        EditTextPreference targetPreference = findPreference("dailyTarget");
        targetPreference.setOnBindEditTextListener(editText ->
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));

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

        Preference import_data = findPreference("import_data");
        import_data.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), "导入", Toast.LENGTH_LONG).show();
            return false;
        });
        Preference export_data = findPreference("export_data");
        export_data.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), "导出", Toast.LENGTH_LONG).show();
            return false;
        });

    }

    private Disposable setVTypeEntry() {
        return Observable.create(
                (ObservableOnSubscribe<List<VocabType>>)
                        emitter -> emitter.onNext(vocabTypeRepository.getAllVocabTypes()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vList -> {
                    if (vList.size() > 0) {
                        List<String> entries = new ArrayList<>();
                        List<String> entryValues = new ArrayList<>();

                        ListPreference vtypePreference = findPreference("vtypeId");
                        String preValue = vtypePreference.getValue();

                        for (VocabType v : vList) {
                            Long id = v.getId();
                            String vType = v.getVocabtype();
                            entries.add(vType + " (" + v.getAmount() + ")");
                            entryValues.add(id + "");
                            if (id.toString().equals(preValue))
                                vtypePreference.setSummaryProvider(p -> vType);
                        }
                        CharSequence[] vtype = entries.toArray(new CharSequence[entries.size()]);
                        CharSequence[] vtypeId = entryValues.toArray(new CharSequence[entryValues.size()]);

                        vtypePreference.setEntries(vtype);
                        vtypePreference.setEntryValues(vtypeId);

                        vtypePreference.setOnPreferenceChangeListener(
                                (preference, newValue) -> {
                                    vtypePreference.setValue(newValue.toString());
                                    if (vtype.length > 0)
                                        vtypePreference.setSummaryProvider(p ->
                                                vtype[Integer.parseInt(newValue.toString()) - 1]);
                                    return false;
                                });
                    }
                });
    }

    private void setColorPickView(SharedPreferences sp) {
        final String[] theme = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_color_picker, null);

        GridView gridView = dialogView.findViewById(R.id.color_grid);
        ColorPickAdapter adapter = new ColorPickAdapter(
                getContext(),
                R.layout.item_color_btn,
                getResources().obtainTypedArray(R.array.primary_colors),
                newTheme -> {
                    theme[0] = newTheme;
                });
        gridView.setAdapter(adapter);
        builder.setTitle("选择主色调")
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.btn_color_default).setOnClickListener(view -> {
            sp.edit().putString("primaryColor", "default").apply();
            dialog.dismiss();
            getActivity().recreate();
        });

        dialogView.findViewById(R.id.btn_color_ensure).setOnClickListener(view -> {
            String newTheme = theme[0];
            if (newTheme != null) {
                sp.edit().putString("primaryColor", newTheme).apply();
            }
            dialog.dismiss();
            getActivity().recreate();
        });
        dialogView.findViewById(R.id.btn_color_cancel)
                .setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}
