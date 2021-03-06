/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.constant.OrderEnum;
import yk.shiroyk.lightword.db.dao.VocabularyDao;
import yk.shiroyk.lightword.db.entity.VocabExercise;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.ThreadTask;

public class VocabularyRepository {

    private final VocabularyDao vocabularyDao;

    public VocabularyRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabularyDao = db.vocabularyDao();
    }

    public LiveData<List<Vocabulary>> getAllWordList(Long vtypeId) {
        return vocabularyDao.getAllWord(vtypeId);
    }

    public LiveData<List<VocabExercise>> getAllWordListOrderBy(Long vtypeId, OrderEnum order) {
        return vocabularyDao.getAllWordOrderBy(vtypeId, order);
    }

    public LiveData<List<VocabExercise>> searchWord(String word, Long vtypeId) {
        return vocabularyDao.searchWord(word, vtypeId);
    }

    public LiveData<List<VocabExercise>> getAllReviewWord(Long vtypeId, OrderEnum order) {
        return vocabularyDao.getAllReviewWord(vtypeId, order);
    }

    public LiveData<List<VocabExercise>> searchReviewWord(String word, Long vtypeId) {
        return vocabularyDao.searchReviewWord(word, vtypeId);
    }

    public LiveData<List<VocabExercise>> getAllMasterWord(Long vtypeId, OrderEnum order) {
        return vocabularyDao.getAllMasterWord(vtypeId, order);
    }

    public LiveData<List<VocabExercise>> searchMasterWord(String word, Long vtypeId) {
        return vocabularyDao.searchMasterWord(word, vtypeId);
    }

    public Integer countWord(Long vtypeId) {
        return vocabularyDao.countWord(vtypeId);
    }

    public Vocabulary queryWordById(Long wordId, Long vtypeId) {
        return vocabularyDao.queryWordById(wordId, vtypeId);
    }

    public List<String> getWordString(Long vtypeId) {
        return vocabularyDao.getWordString(vtypeId);
    }

    public Set<String> getWordStringSet(Long vtypeId) {
        return new HashSet<>(vocabularyDao.getWordString(vtypeId));
    }

    public List<Vocabulary> getWordListById(List<Long> wordId, Long vtypeId) {
        return vocabularyDao.getWordListById(wordId, vtypeId);
    }

    public Set<Vocabulary> getWordSetById(List<Long> wordId, Long vtypeId) {
        return new HashSet<>(vocabularyDao.getWordListById(wordId, vtypeId));
    }

    public List<Vocabulary> getWordList(Long vtypeId) {
        return vocabularyDao.getWordList(vtypeId);
    }

    public Set<Vocabulary> getWordSet(Long vtypeId) {
        return new HashSet<>(vocabularyDao.getWordList(vtypeId));
    }

    public Map<String, Long> getWordIdMap(Long vtypeId) {
        Map<String, Long> wordIdMap = new HashMap<>();
        for (Vocabulary vocab : getWordList(vtypeId)) {
            wordIdMap.put(vocab.getWord(), vocab.getId());
        }
        return wordIdMap;
    }

    public Vocabulary[] collectNewVocab(Long newType, Set<Vocabulary> vList) {
        Set<Vocabulary> newVList = new HashSet<>();
        Set<Vocabulary> oldVList = new HashSet<>(getWordList(newType));
        vList.removeAll(oldVList);
        for (Vocabulary v : vList) {
            v.setId(null);
            v.setVtypeId(newType);
            newVList.add(v);
        }
        int size = newVList.size();
        return newVList.toArray(new Vocabulary[size]);
    }

    public Vocabulary queryWord(String word, Long vtypeId) {
        return vocabularyDao.queryWord(word, vtypeId);
    }

    public List<Vocabulary> loadNewWord(Long vtypeId, Boolean order, Integer limit) {
        return vocabularyDao.loadNewWord(vtypeId, order, limit);
    }

    public void insert(Vocabulary[] v) {
        vocabularyDao.insertMany(v);
    }

    public void update(Vocabulary v) {
        ThreadTask.runOnThread(() -> vocabularyDao.update(v));
    }

    public void insert(Vocabulary v, Consumer<Long> consumer) {
        ThreadTask.runOnThread(() -> vocabularyDao.insert(v), consumer);
    }

    public Integer delete(Vocabulary[] v) {
        return vocabularyDao.delete(v);
    }

    public Integer delete(Vocabulary v) {
        return vocabularyDao.delete(v);
    }
}
