/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import yk.shiroyk.lightword.db.constant.ThemeEnum;
import yk.shiroyk.lightword.utils.ThemeHelper;

public class DarkThemeApplication extends Application {

    public void onCreate() {
        super.onCreate();
        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(this);

        if (!sp.getBoolean("isClear", false)) {
            sp.edit().clear().apply();
            sp.edit().putBoolean("isClear", true).apply();
        }

        int themePref = sp.getInt("themePref", ThemeEnum.LightMode.getMode());
        ThemeHelper.applyTheme(ThemeEnum.values()[themePref]);
    }
}
