/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.constant;

public enum OrderEnum {
    Word(0),
    Frequency(1),
    Correct(2),
    Wrong(3),
    LastPractice(4),
    Timestamp(5);

    private final Integer orderBy;

    OrderEnum(Integer orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getOrderBy() {
        return orderBy;
    }
}
