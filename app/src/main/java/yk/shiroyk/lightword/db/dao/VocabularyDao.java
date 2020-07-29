package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import yk.shiroyk.lightword.db.entity.Vocabulary;

@Dao
public interface VocabularyDao {
    @Insert
    Long insert(Vocabulary vocabulary);

    @Transaction
    @Insert
    void insertMany(Vocabulary[] vocabularies);

    @Query("SELECT * FROM vocabulary WHERE id = :wordId")
    Vocabulary getWordById(Long wordId);

    @Query("SELECT * FROM vocabulary WHERE word = :word")
    Vocabulary getWord(String word);

    @Transaction
    @Query("SELECT * FROM vocabulary WHERE id IN (:Id)")
    Vocabulary[] getWordListById(List<Long> Id);

    @Transaction
    @Query("SELECT word FROM vocabulary")
    LiveData<List<String>> getWordString();

    @Transaction
    @Query("SELECT word FROM vocabulary WHERE id IN (:Id)")
    LiveData<List<String>> getWordStringById(List<Long> Id);

    @Transaction
    @Query("SELECT * FROM vocabulary")
    Vocabulary[] getAllWord();

    @Query("SELECT COUNT(word) FROM vocabulary")
    LiveData<Integer> getCount();
}
