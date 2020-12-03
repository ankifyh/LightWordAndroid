/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.constant.ThemeEnum;

public class ThemeHelper {

    public static void applyTheme(ThemeEnum theme) {
        switch (theme) {
            case LightMode: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            }
            case DarkMode: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            }
            case AutoMode: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
            }
        }
    }

    public static void setPrimaryColor(Activity activity, SharedPreferences sp) {
        String theme = sp.getString("primaryColor", "AppTheme");
        activity.getTheme().applyStyle(activity.getResources().
                        getIdentifier("AppTheme_" + theme, "style", activity.getPackageName()),
                true);
    }

    public static void setNavigationBarColor(Activity activity, SharedPreferences sp) {
        TypedValue typedValue = new TypedValue();
        int color = R.color.transparent;
        if (activity.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            color = sp.getBoolean("navigationBarBg", false) ? typedValue.resourceId : R.color.transparent;
        }
        activity.getWindow().setNavigationBarColor(activity.getResources().getColor(color));
    }
}
