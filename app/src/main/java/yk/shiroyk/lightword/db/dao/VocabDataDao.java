package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.Vocabulary;

@Dao
public interface VocabDataDao {
    @Insert
    Long insert(VocabData vocabData);

    @Transaction
    @Insert
    void insertMany(VocabData[] vocabData);

    @Query("SELECT COUNT(word_id) FROM vocab_data WHERE vtype_id = :vtypeId")
    LiveData<Integer> getCount(Long vtypeId);

    @Query("SELECT word_id FROM vocab_data WHERE vtype_id = :vtypeId")
    List<Long> getAllWordId(Long vtypeId);

    @Query("SELECT vocabulary.* FROM vocab_data, vocabulary " +
            "WHERE vtype_id = :vtypeId and vocab_data.word_id = vocabulary.id")
    LiveData<List<Vocabulary>> getAllWord(Long vtypeId);

    @Query("SELECT vocabulary.* FROM vocab_data, vocabulary " +
            "WHERE vtype_id = :vtypeId and vocab_data.word_id = vocabulary.id and word LIKE :word")
    LiveData<List<Vocabulary>> searchWord(Long vtypeId, String word);

    @Query("SELECT vocab_data.word_id " +
            "FROM vocab_data LEFT OUTER JOIN (" +
            "SELECT exercise_data.word_id FROM exercise_data " +
            "WHERE exercise_data.vtype_id = :vtypeId " +
            ") AS userword ON vocab_data.word_id = userword.word_id " +
            "WHERE userword.word_id IS NULL AND vocab_data.vtype_id = :vtypeId " +
            "ORDER BY CASE WHEN :order = 1 THEN frequency WHEN :order = 0 THEN RANDOM() END " +
            "LIMIT :limit")
    List<Long> loadNewWord(long vtypeId, Boolean order, Integer limit);
}
