package yk.shiroyk.lightword.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "user_statistic")
public class UserStatistic {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
    @NonNull
    private Date timestamp;
    @ColumnInfo(name = "correct")
    private Integer correct;
    @ColumnInfo(name = "wrong")
    private Integer wrong;
    @ColumnInfo(name = "count")
    private Integer count;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDay() {
        SimpleDateFormat formatter = new SimpleDateFormat("MMdd", Locale.CHINA);
        return formatter.format(timestamp);
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public Integer getWrong() {
        return wrong;
    }

    public void setWrong(Integer wrong) {
        this.wrong = wrong;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
