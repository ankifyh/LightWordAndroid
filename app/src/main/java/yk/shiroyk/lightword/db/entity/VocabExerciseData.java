/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.entity;


import java.util.Date;

public class VocabExerciseData {
    public String word;
    public Long word_id;
    public Long vtype_id;
    public Date timestamp;
    public Date last_practice;
    public Integer stage;
    public Integer correct;
    public Integer wrong;
}
