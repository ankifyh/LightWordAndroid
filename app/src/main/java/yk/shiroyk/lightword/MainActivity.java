package yk.shiroyk.lightword;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;

import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.utils.ThemeHelper;

public class MainActivity extends AppCompatActivity {
    private Context context;
    private AppBarConfiguration mAppBarConfiguration;
    private SharedViewModel sharedViewModel;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getBaseContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        setStatusBarTransparent();
        setNavgationBarColor();

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
                R.id.nav_home, R.id.nav_import, R.id.nav_setting, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setUpSwitchTheme(navigationView);
    }

    private void setUpSwitchTheme(NavigationView nv) {
        AppCompatToggleButton btn_theme_toggle = nv.getHeaderView(0).findViewById(R.id.btn_theme_toggle);
        String themePref = sp.getString("themePref", ThemeHelper.DEFAULT_MODE);
        btn_theme_toggle.setOnClickListener(view -> {
            String switchTheme;
            if (ThemeHelper.DARK_MODE.equals(themePref) || ThemeHelper.DEFAULT_MODE.equals(themePref)) {
                switchTheme = ThemeHelper.LIGHT_MODE;
            } else {
                switchTheme = ThemeHelper.DARK_MODE;
            }
            ThemeHelper.applyTheme(switchTheme);
            sp.edit().putString("themePref", switchTheme).apply();
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

    public void setNavgationBarColor() {
        int color = sp.getBoolean("navigationBarBg", false) ? R.color.colorPrimary : R.color.transparent;
        getWindow().setNavigationBarColor(getResources().getColor(color));
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

    @Override
    protected void onStop() {
        super.onStop();
    }
}