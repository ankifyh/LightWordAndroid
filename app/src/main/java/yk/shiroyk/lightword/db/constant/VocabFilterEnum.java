/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.constant;

public enum VocabFilterEnum {
    All(0),
    Review(1),
    Master(2);

    private final int filter;

    VocabFilterEnum(int filter) {
        this.filter = filter;
    }

    public int getFilter() {
        return filter;
    }

}
