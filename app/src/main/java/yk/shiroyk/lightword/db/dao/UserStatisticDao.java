package yk.shiroyk.lightword.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.UserStatistic;

@Dao
public interface UserStatisticDao {

    @Insert
    void insert(UserStatistic statistic);

    @Update
    int update(UserStatistic statistic);

    @Query("SELECT * FROM user_statistic WHERE timestamp = :timestamp")
    UserStatistic getTodayStatistic(Long timestamp);

    @Query("SELECT * FROM user_statistic WHERE timestamp >= strftime('%s','now', :days) * 1000")
    List<UserStatistic> getStatistic(String days);
}
