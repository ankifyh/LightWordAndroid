package yk.shiroyk.lightword.repository;

import android.app.Application;

import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.UserStatisticDao;
import yk.shiroyk.lightword.db.entity.UserStatistic;
import yk.shiroyk.lightword.utils.ThreadTask;

public class UserStatisticRepository {
    private UserStatisticDao userStatisticDao;
    private UserStatistic statistic = null;

    public UserStatisticRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.userStatisticDao = db.userStatisticDao();
    }

    public void update(UserStatistic statistic) {
        ThreadTask.runOnThread(statistic,
                s -> userStatisticDao.update(s));
    }

    public List<UserStatistic> getStatistic(Integer days) {
        return userStatisticDao.getStatistic("-" + days + " day");
    }

    private void getTodayStatistic() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        statistic = Observable.create((ObservableOnSubscribe<UserStatistic>) emitter -> {
            UserStatistic s = userStatisticDao.getTodayStatistic(calendar.getTimeInMillis());
            if (s == null) {
                s = new UserStatistic();
                s.setTimestamp(calendar.getTime());
                s.setCorrect(0);
                s.setWrong(0);
                s.setCount(0);
                userStatisticDao.insert(s);
            }
            emitter.onNext(s);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .blockingSingle();
    }

    public void updateCorrect() {
        if (statistic == null) {
            getTodayStatistic();
        }
        int correct = statistic.getCorrect() + 1;
        statistic.setCorrect(correct);
        update(statistic);
    }

    public void updateWrong() {
        if (statistic == null) {
            getTodayStatistic();
        }
        int wrong = statistic.getWrong() + 1;
        statistic.setWrong(wrong);
        update(statistic);
    }

    public void updateCount() {
        if (statistic == null) {
            getTodayStatistic();
        }
        int count = statistic.getCount() + 1;
        statistic.setCount(count);
        update(statistic);
    }

    public Integer getTodayCount() {
        if (statistic == null) {
            getTodayStatistic();
        }
        return statistic.getCount();
    }

    public void insert(UserStatistic statistic) {
        ThreadTask.runOnThread(statistic,
                s -> userStatisticDao.insert(s));
    }
}
