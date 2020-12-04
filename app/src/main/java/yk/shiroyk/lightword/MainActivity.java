/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.Crashes;

import yk.shiroyk.lightword.db.constant.ThemeEnum;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.ThemeHelper;
import yk.shiroyk.lightword.utils.ThreadTask;
import yk.shiroyk.lightword.utils.UpdateChecker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sp;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getBaseContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        setStatusBarTransparent();
        ThemeHelper.setPrimaryColor(this, sp);
        ThemeHelper.setNavigationBarColor(this, sp);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedViewModel.getSubTitle().observe(this, s -> {
            getSupportActionBar().setSubtitle(s);
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawer.setFitsSystemWindows(true);
        drawer.setClipToPadding(false);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_data, R.id.nav_setting, R.id.nav_about, R.id.nav_help)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setUpSwitchTheme(navigationView);

        if (!BuildConfig.APPSECRET.isEmpty() && !BuildConfig.DEBUG) {
            AppCenter.setLogLevel(Log.VERBOSE);
            AppCenter.start(getApplication(),
                    BuildConfig.APPSECRET,
                    Analytics.class,
                    Crashes.class);
            Crashes.setListener(new AbstractCrashesListener() {
                @Override
                public boolean shouldAwaitUserConfirmation() {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle("检测到APP上次发生崩溃")
                            .setMessage("是否发送崩溃日志？")
                            .setPositiveButton("是", (dialog, which) -> Crashes.notifyUserConfirmation(Crashes.SEND))
                            .setNegativeButton("否", (dialog, which) -> Crashes.notifyUserConfirmation(Crashes.DONT_SEND))
                            .create().show();
                    return true;
                }
            });
        }

        requestStoragePermissions();

        onApplicationStart(() -> {
            Log.i(TAG, "App Launch");
            checkLatestVersion();
        });

    }

    private void setUpSwitchTheme(NavigationView nv) {
        ThemeEnum themePref = ThemeEnum.values()[sp.getInt("themePref", ThemeEnum.LightMode.getMode())];
        nv.getHeaderView(0).findViewById(R.id.btn_theme_toggle).setOnClickListener(view -> {
            ThemeEnum newTheme;
            if (ThemeEnum.LightMode == themePref) {
                newTheme = ThemeEnum.DarkMode;
            } else {
                newTheme = ThemeEnum.LightMode;
            }
            sp.edit().putInt("themePref", newTheme.getMode()).apply();
            ThemeHelper.applyTheme(newTheme);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setStatusBarTransparent() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermissions() {
        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_EXTERNAL_STORAGE
        );
    }

    private void onApplicationStart(Runnable runnable) {
        boolean firstTime = sp.getBoolean("firstTime", true);
        if (!firstTime) return;
        sp.edit().putBoolean("firstTime", false).apply();
        runnable.run();
    }

    private void checkLatestVersion() {
        boolean autoCheck = sp.getBoolean("autoCheckUpdate", false);
        if (autoCheck) {
            Log.i(TAG, "Checking Latest Version...");
            UpdateChecker checker = new UpdateChecker();
            ThreadTask.runOnThread(checker::checkUpdate, strings -> {
                if (strings.length > 1) {
                    Snackbar.make(findViewById(android.R.id.content), "有新的更新可用啦！", Snackbar.LENGTH_LONG)
                            .setAction("点击查看", view ->
                                    new MaterialAlertDialogBuilder(this)
                                            .setTitle(strings[0])
                                            .setMessage(strings[1])
                                            .setPositiveButton(R.string.dialog_url,
                                                    (dialogInterface, i) -> new Intent(Intent.ACTION_VIEW)
                                                            .setData(Uri.parse(strings[2])))
                                            .setNegativeButton(R.string.dialog_cancel, null).create().show()
                            )
                            .show();
                } else Log.i(TAG, strings[0]);
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}