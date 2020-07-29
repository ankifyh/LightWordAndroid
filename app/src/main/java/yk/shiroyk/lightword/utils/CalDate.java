package yk.shiroyk.lightword.utils;

import android.util.Log;

import java.util.Date;
import java.util.List;

public class CalDate {
    private List<Integer> minList;
    private Date timestamp;
    private int stage;

    public CalDate(List<Integer> minList, Date timestamp, int stage) {
        this.minList = minList;
        this.timestamp = timestamp;
        this.stage = stage;
    }

    public Date timestamp(boolean minus) {
        if (stage < minList.size()) {
            int calStage = minus && (stage > 1) ? 2 : 0;
            long time = Long.valueOf(minList.get(stage - calStage)) * 60 * 1000;
            timestamp = new Date(new Date().getTime() + time);
            Log.d("Calculate Date", minList.get(stage - calStage) + " Time: " + timestamp.toString());
        }
        return timestamp;
    }
}
