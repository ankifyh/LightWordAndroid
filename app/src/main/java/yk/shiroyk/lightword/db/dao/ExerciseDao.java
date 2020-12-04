/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabExerciseData;

@Dao
public interface ExerciseDao {

    @Insert
    Long insert(ExerciseData exercisedata);

    @Transaction
    @Insert
    void insert(ExerciseData[] exercisedata);

    @Update
    void update(ExerciseData exercisedata);

    @Transaction
    @Update
    void update(ExerciseData[] exerciseData);

    @Query("SELECT word, word_id, vtype_id, timestamp, last_practice," +
            "stage, correct, wrong FROM vocabulary INNER JOIN (SELECT word_id, " +
            "timestamp, last_practice, stage, correct, wrong " +
            "FROM exercise_data WHERE vtype_id = :vtypeId) AS userword ON " +
            "vocabulary.id = userword.word_id")
    List<VocabExerciseData> getExerciseDataList(Long vtypeId);

    @Query("SELECT * FROM exercise_data WHERE vtype_id = :vtypeId")
    List<ExerciseData> getExerciseData(Long vtypeId);

    @Query("SELECT word_id FROM exercise_data WHERE vtype_id = :vtypeId")
    List<Long> getVocabIdList(Long vtypeId);

    @Transaction
    @Query("SELECT * FROM exercise_data WHERE vtype_id = :vtypeId AND word_id IN (:idList)")
    ExerciseData[] getWordListById(List<Long> idList, Long vtypeId);

    @Query("SELECT count(word_id) FROM exercise_data WHERE vtype_id = :vtypeId")
    LiveData<Integer> getExerciseProgress(long vtypeId);

    @Query("SELECT count(word_id) FROM exercise_data " +
            "WHERE vtype_id = :vtypeId " +
            "AND stage != 99 " +
            "AND timestamp <= strftime('%s','now') * 1000")
    LiveData<Integer> getExerciseReview(long vtypeId);

    @Query("SELECT * FROM exercise_data " +
            "WHERE exercise_data.word_id = :wordId " +
            "AND exercise_data.vtype_id = :vtypeId ")
    ExerciseData getWordDetail(long wordId, long vtypeId);

    @Query("SELECT * FROM exercise_data " +
            "WHERE exercise_data.word_id = :wordId " +
            "AND exercise_data.vtype_id = :vtypeId ")
    ExerciseData getSingleWord(long wordId, long vtypeId);

    @Query("SELECT word_id FROM exercise_data " +
            "WHERE exercise_data.vtype_id = :vtypeId " +
            "AND timestamp <= strftime('%s','now') * 1000 " +
            "AND stage != 99 " +
            "ORDER BY exercise_data.timestamp " +
            "LIMIT :limit")
    List<Long> LoadReviewWord(long vtypeId, Integer limit);
}
