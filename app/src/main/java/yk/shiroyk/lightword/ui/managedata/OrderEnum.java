package yk.shiroyk.lightword.ui.managedata;

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
