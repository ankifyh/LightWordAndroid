/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import yk.shiroyk.lightword.db.constant.ThemeEnum;
import yk.shiroyk.lightword.utils.ThemeHelper;

public class LightWordApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Context context = getBaseContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        initTheme(sp);

        firstRun(sp);
    }

    private void initTheme(SharedPreferences sp) {
        if (!sp.getBoolean("isClear", false)) {
            sp.edit().clear().apply();
            sp.edit().putBoolean("isClear", true).apply();
        }
        int themePref = sp.getInt("themePref", ThemeEnum.LightMode.getMode());
        ThemeHelper.applyTheme(ThemeEnum.values()[themePref]);
    }

    private void firstRun(SharedPreferences sp) {
        sp.edit().putBoolean("firstTime", true).apply();
    }
}
