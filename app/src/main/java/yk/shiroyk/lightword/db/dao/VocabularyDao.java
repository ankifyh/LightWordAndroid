package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.VocabExercise;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.ui.managedata.OrderEnum;

@Dao
public interface VocabularyDao {
    @Insert
    Long insert(Vocabulary vocabulary);

    @Transaction
    @Insert
    void insertMany(Vocabulary[] vocabularies);

    @Update
    int update(Vocabulary vocabulary);

    @Delete
    int delete(Vocabulary vocabulary);

    @Query("SELECT * FROM vocabulary WHERE" +
            " id = :wordId AND vtype_id = :vtypeId")
    LiveData<Vocabulary> getWordById(Long wordId, Long vtypeId);

    @Query("SELECT * FROM vocabulary WHERE" +
            " id = :wordId AND vtype_id = :vtypeId")
    Vocabulary queryWordById(Long wordId, Long vtypeId);

    @Query("SELECT * FROM vocabulary WHERE" +
            " word = :word AND vtype_id = :vtypeId")
    LiveData<Vocabulary> getWord(String word, Long vtypeId);

    @Query("SELECT * FROM vocabulary WHERE" +
            " word = :word AND vtype_id = :vtypeId")
    Vocabulary queryWord(String word, Long vtypeId);

    @Transaction
    @Query("SELECT * FROM vocabulary WHERE" +
            " id IN (:Id) AND vtype_id = :vtypeId")
    List<Vocabulary> getWordListById(List<Long> Id, Long vtypeId);

    @Transaction
    @Query("SELECT word FROM vocabulary WHERE vtype_id = :vtypeId")
    List<String> getWordString(Long vtypeId);

    @Query("SELECT COUNT(id) FROM vocabulary WHERE vtype_id = :vtypeId")
    LiveData<Integer> getCount(Long vtypeId);

    @Query("SELECT * FROM vocabulary WHERE vtype_id = :vtypeId")
    LiveData<List<Vocabulary>> getAllWord(Long vtypeId);

    @Query("SELECT id, word, vocabulary.vtype_id AS vtypeId, frequency, userword.last_practice AS lastPractice, " +
            " userword.timestamp, correct, wrong FROM vocabulary LEFT JOIN ( SELECT word_id, timestamp, " +
            " last_practice, correct, wrong FROM exercise_data WHERE exercise_data.vtype_id = :vtypeId) AS userword " +
            " ON vocabulary.id = userword.word_id WHERE vocabulary.vtype_id = :vtypeId ORDER BY " +
            " CASE WHEN :order = 0 THEN word COLLATE NOCASE " +
            "      WHEN :order = 1 THEN frequency END ASC," +
            " CASE WHEN :order = 2 THEN correct " +
            "      WHEN :order = 3 THEN wrong " +
            "      WHEN :order = 4 THEN lastPractice " +
            "      WHEN :order = 5 THEN timestamp END DESC")
    LiveData<List<VocabExercise>> getAllWordOrderBy(Long vtypeId, OrderEnum order);

    @Query("SELECT id, word, vocabulary.vtype_id AS vtypeId, frequency, userword.last_practice AS lastPractice, " +
            " userword.timestamp, correct, wrong FROM vocabulary LEFT JOIN ( SELECT word_id, timestamp, " +
            " last_practice, correct, wrong FROM exercise_data WHERE exercise_data.vtype_id = :vtypeId) " +
            " AS userword ON vocabulary.id = userword.word_id WHERE word LIKE :word " +
            " AND vocabulary.vtype_id = :vtypeId ORDER BY word COLLATE NOCASE")
    LiveData<List<VocabExercise>> searchWord(String word, Long vtypeId);

    @Query("SELECT id, word, vocabulary.vtype_id AS vtypeId, frequency, userword.last_practice AS lastPractice, " +
            " userword.timestamp, correct, wrong FROM vocabulary INNER JOIN ( SELECT word_id, timestamp, last_practice, " +
            " correct, wrong FROM exercise_data WHERE exercise_data.vtype_id = :vtypeId AND " +
            " timestamp <= strftime('%s','now') * 1000) AS userword ON vocabulary.id = userword.word_id ORDER BY " +
            " CASE WHEN :order = 0 THEN word COLLATE NOCASE " +
            "      WHEN :order = 1 THEN frequency END ASC," +
            " CASE WHEN :order = 2 THEN correct " +
            "      WHEN :order = 3 THEN wrong " +
            "      WHEN :order = 4 THEN lastPractice " +
            "      WHEN :order = 5 THEN timestamp END DESC")
    LiveData<List<VocabExercise>> getAllReviewWord(Long vtypeId, OrderEnum order);

    @Query("SELECT id, word, vocabulary.vtype_id AS vtypeId, frequency, userword.last_practice AS lastPractice, " +
            " userword.timestamp, correct, wrong FROM vocabulary INNER JOIN ( SELECT word_id, timestamp, " +
            " last_practice, correct, wrong FROM exercise_data WHERE exercise_data.vtype_id = :vtypeId " +
            " AND timestamp <=  strftime('%s','now') * 1000) AS userword " +
            " ON vocabulary.id = userword.word_id WHERE vocabulary.word LIKE :word ORDER BY word COLLATE NOCASE")
    LiveData<List<VocabExercise>> searchReviewWord(String word, Long vtypeId);

    @Query("SELECT id FROM vocabulary WHERE vtype_id = :vtypeId")
    List<Long> getAllWordId(Long vtypeId);

    @Query("SELECT * FROM vocabulary " +
            "WHERE vtype_id = :vtypeId")
    List<Vocabulary> getAllWordList(Long vtypeId);

    @Query("SELECT vocabulary.* " +
            "FROM vocabulary LEFT OUTER JOIN (" +
            "SELECT exercise_data.word_id FROM exercise_data " +
            "WHERE exercise_data.vtype_id = :vtypeId " +
            ") AS userword ON vocabulary.id = userword.word_id " +
            "WHERE userword.word_id IS NULL AND vocabulary.vtype_id = :vtypeId " +
            "ORDER BY CASE WHEN :order = 1 THEN frequency WHEN :order = 0 THEN RANDOM() END " +
            "LIMIT :limit")
    List<Vocabulary> loadNewWord(Long vtypeId, Boolean order, Integer limit);
}
