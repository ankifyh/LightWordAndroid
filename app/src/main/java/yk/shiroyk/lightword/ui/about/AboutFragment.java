package yk.shiroyk.lightword.ui.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import yk.shiroyk.lightword.R;


public class AboutFragment extends PreferenceFragmentCompat {
    private Context context;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.about, rootKey);
        context = getContext();
        init();
    }

    private void init() {
        Preference about_source = findPreference("about_source");
        about_source.setOnPreferenceClickListener(preference -> {
            Log.d("source", about_source.getSummary().toString());
            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(about_source.getSummary().toString())));
            return false;
        });
        Preference about_donate = findPreference("about_donate");
        about_donate.setOnPreferenceClickListener(preference -> {
            setDialog();
            return false;
        });
    }

    private void setDialog() {
        ImageView img = new ImageView(context);
        img.setImageResource(R.drawable.donate);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("你的支持就是我写BUG的动力")
                .setView(img)
                .setNegativeButton("确认", null).create().show();
    }

}