package yk.shiroyk.lightword.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import yk.shiroyk.lightword.db.entity.VocabType;

@Dao
public interface VocabTypeDao {
    @Insert
    Long insert(VocabType vocabtype);

    @Update
    int update(VocabType vocabtype);

    @Delete
    int delete(VocabType vocabtype);

    @Query("SELECT * FROM vocab_type")
    List<VocabType> getAllVocabType();

    @Query("SELECT * FROM vocab_type WHERE id = :vtypeId")
    LiveData<VocabType> getVocabTypeById(Long vtypeId);

    @Query("SELECT * FROM vocab_type WHERE vocabtype = :vocabtype")
    VocabType getVocabType(String vocabtype);

    @Query("SELECT id FROM vocab_type WHERE vocabtype = :vocabtype")
    Long getVocabTypeId(String vocabtype);
}

