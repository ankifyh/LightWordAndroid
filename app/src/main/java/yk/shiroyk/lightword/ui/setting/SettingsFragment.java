package yk.shiroyk.lightword.ui.setting;

import android.app.Activity;
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

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.utils.ThemeHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Activity mActivity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        mActivity = getActivity();
        init();
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
            int color = preference.getSharedPreferences().getBoolean("navigationBarBg", false) ? R.color.colorPrimary : R.color.transparent;
            mActivity.getWindow().setNavigationBarColor(getResources().getColor(color));
            return false;
        });

        VocabTypeRepository vocabTypeRepository = new VocabTypeRepository(this.getActivity().getApplication());
        List<VocabType> vocabTypes = vocabTypeRepository.getAllVocabType();
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        for (VocabType v : vocabTypes) {
            entries.add(v.getVocabtype() + " (" + v.getAmount() + ")");
            entryValues.add(v.getId() + "");
        }

        ListPreference vtypePreference = findPreference("vtypeId");
        vtypePreference.setEntries(entries.toArray(new CharSequence[entries.size()]));
        vtypePreference.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));

        EditTextPreference targetPreference = findPreference("dailyTarget");
        targetPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));

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
}
