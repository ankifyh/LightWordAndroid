package yk.shiroyk.lightword.db.entity;

import java.util.Date;

public class VocabExercise {
    public Long id;
    public String word;
    public Long vtypeId;
    public Long frequency;
    public Date lastPractice;
    public Date timestamp;
    public short correct;
    public short wrong;
}
