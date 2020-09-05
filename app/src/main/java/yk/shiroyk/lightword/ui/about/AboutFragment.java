package yk.shiroyk.lightword.ui.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import yk.shiroyk.lightword.BuildConfig;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.utils.ThreadTask;
import yk.shiroyk.lightword.utils.UpdateChecker;


public class AboutFragment extends PreferenceFragmentCompat {
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about, rootKey);
        context = getContext();
        init();
    }

    private void init() {
        Preference about_version = findPreference("about_version");
        about_version.setSummary(BuildConfig.VERSION_NAME);
        about_version.setOnPreferenceClickListener(preference -> {
            setCheckUpdate();
            return false;
        });

        Preference about_source = findPreference("about_source");
        about_source.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(getString(R.string.source_url))));
            return false;
        });
        Preference about_donate = findPreference("about_donate");
        about_donate.setOnPreferenceClickListener(preference -> {
            setDonateDialog();
            return false;
        });
    }

    private void setDonateDialog() {
        ImageView img = new ImageView(context);
        img.setImageResource(R.drawable.donate);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("你的支持就是我写BUG的动力")
                .setView(img)
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void setCheckUpdate() {
        UpdateChecker checker = new UpdateChecker();
        Toast.makeText(context, "检查中...", Toast.LENGTH_SHORT).show();
        ThreadTask.runOnThread(checker::checkUpdate, strings -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (strings.length > 1) {
                builder.setTitle(strings[0])
                        .setMessage(strings[1])
                        .setPositiveButton(R.string.dialog_url,
                                (dialogInterface, i) -> new Intent(Intent.ACTION_VIEW)
                                        .setData(Uri.parse(strings[2])))
                        .setNegativeButton(R.string.dialog_cancel, null).create().show();
            } else {
                builder.setTitle(strings[0])
                        .setNegativeButton(R.string.dialog_ensure, null).create().show();
            }
        });
    }

}