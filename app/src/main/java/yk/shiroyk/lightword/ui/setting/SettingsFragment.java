package yk.shiroyk.lightword.ui.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

        SwitchPreferenceCompat navBgPreference = findPreference("navigationBarBg");
        navBgPreference.setOnPreferenceClickListener(preference -> {
            int color = preference.getSharedPreferences()
                    .getBoolean("navigationBarBg", false) ? R.color.colorPrimary : R.color.transparent;
            mActivity.getWindow().setNavigationBarColor(getResources().getColor(color));
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
}
