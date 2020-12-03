/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.constant;

public enum ThemeEnum {
    LightMode(0),
    DarkMode(1),
    AutoMode(2);

    private final int mode;

    ThemeEnum(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
