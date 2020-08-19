package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.ExerciseData;

@Dao
public interface ExerciseDao {

    @Insert
    Long insert(ExerciseData exercisedata);

    @Update
    void update(ExerciseData exercisedata);

    @Query("SELECT * FROM exercise_data")
    ExerciseData getAllData();

    @Query("SELECT count(word_id) FROM exercise_data WHERE vtype_id = :vtypeId")
    LiveData<Integer> getExerciseProgress(long vtypeId);

    @Query("SELECT * FROM exercise_data " +
            "WHERE exercise_data.word_id = :wordId " +
            "AND exercise_data.vtype_id = :vtypeId ")
    LiveData<ExerciseData> getWordDetail(long wordId, long vtypeId);

    @Query("SELECT * FROM exercise_data " +
            "WHERE exercise_data.word_id = :wordId " +
            "AND exercise_data.vtype_id = :vtypeId ")
    ExerciseData getSingleWord(long wordId, long vtypeId);

    @Query("SELECT word_id FROM exercise_data " +
            "WHERE exercise_data.vtype_id = :vtypeId " +
            "AND timestamp <= strftime('%s','now') * 1000 " +
            "ORDER BY exercise_data.timestamp " +
            "LIMIT :limit")
    List<Long> LoadReviewWord(long vtypeId, Integer limit);
}
