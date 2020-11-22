package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.Vocabulary;

@Dao
public interface ExerciseDao {

    @Insert
    Long insert(ExerciseData exercisedata);

    @Update
    void update(ExerciseData exercisedata);

    @Transaction
    @Update
    void update(ExerciseData[] exerciseData);

    @Query("SELECT * FROM exercise_data")
    ExerciseData getAllData();

    @Transaction
    @Query("SELECT * FROM exercise_data WHERE vtype_id = :vtypeId AND word_id IN (:idList)")
    ExerciseData[] getWordListById(List<Long> idList, Long vtypeId);

    @Query("SELECT vocabulary.* FROM exercise_data, vocabulary WHERE " +
            "stage = 11 AND exercise_data.vtype_id = :vtypeId" +
            " AND exercise_data.word_id = vocabulary.id")
    List<Vocabulary> getMasterWord(long vtypeId);

    @Query("SELECT * FROM exercise_data WHERE stage = 11")
    List<ExerciseData> getMastered();

    @Query("SELECT vocabulary.* FROM exercise_data, vocabulary WHERE " +
            "stage = 11 AND exercise_data.vtype_id = :vtypeId " +
            " AND exercise_data.word_id = vocabulary.id AND word LIKE :word")
    LiveData<List<Vocabulary>> searchMasterWord(long vtypeId, String word);

    @Query("SELECT count(word_id) FROM exercise_data WHERE vtype_id = :vtypeId")
    LiveData<Integer> getExerciseProgress(long vtypeId);

    @Query("SELECT count(word_id) FROM exercise_data " +
            "WHERE vtype_id = :vtypeId " +
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
            "ORDER BY exercise_data.timestamp " +
            "LIMIT :limit")
    List<Long> LoadReviewWord(long vtypeId, Integer limit);
}
