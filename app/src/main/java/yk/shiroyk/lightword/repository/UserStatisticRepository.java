package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.UserStatisticDao;
import yk.shiroyk.lightword.db.entity.UserStatistic;
import yk.shiroyk.lightword.utils.ThreadTask;

public class UserStatisticRepository {
    private UserStatisticDao userStatisticDao;
    private UserStatistic statistic = null;
    private MutableLiveData<Integer> todayCount = new MutableLiveData<>();

    public UserStatisticRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.userStatisticDao = db.userStatisticDao();
    }

    public void update(UserStatistic s) {
        ThreadTask.runOnThread(() -> userStatisticDao.update(s));
    }

    public List<UserStatistic> getStatistic(Integer days) {
        return userStatisticDao.getStatistic("-" + days + " day");
    }

    private UserStatistic getTodayStatistic() {
        if (statistic == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            UserStatistic s = userStatisticDao.getTodayStatistic(calendar.getTimeInMillis());
            if (s == null) {
                s = new UserStatistic();
                s.setTimestamp(calendar.getTime());
                s.setCorrect(0);
                s.setWrong(0);
                s.setCount(0);
                userStatisticDao.insert(s);
            }
            statistic = s;
        }
        return statistic;
    }

    public void updateCorrect() {
        ThreadTask.runOnThread(
                this::getTodayStatistic,
                statistic -> {
                    int correct = statistic.getCorrect() + 1;
                    statistic.setCorrect(correct);
                    update(statistic);
                });
    }

    public void updateWrong() {
        ThreadTask.runOnThread(
                this::getTodayStatistic,
                statistic -> {
                    int wrong = statistic.getWrong() + 1;
                    statistic.setWrong(wrong);
                    update(statistic);
                });
    }

    public void updateCount() {
        ThreadTask.runOnThread(
                this::getTodayStatistic,
                statistic -> {
                    int count = statistic.getCount() + 1;
                    statistic.setCount(count);
                    update(statistic);
                    todayCount.postValue(statistic.getCount());
                });
    }

    public LiveData<Integer> getTodayCount() {
        ThreadTask.runOnThread(() -> {
            getTodayStatistic();
            todayCount.postValue(statistic.getCount());
        });
        return todayCount;
    }

    public void insert(UserStatistic s) {
        ThreadTask.runOnThread(() -> userStatisticDao.insert(s));
    }
}
