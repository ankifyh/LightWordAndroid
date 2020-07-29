package yk.shiroyk.lightword.db.entity;

import java.util.List;

public class Profile {
    private List<Integer> minList;

    public List<Integer> getForgetTime() {
        return minList;
    }

    public void setForgetTime(List<Integer> minList) {
        this.minList = minList;
    }
}
