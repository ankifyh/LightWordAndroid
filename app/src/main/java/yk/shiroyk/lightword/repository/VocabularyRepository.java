/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public LiveData<Vocabulary> getWordById(Long wordId, Long vtypeId) {
        return vocabularyDao.getWordById(wordId, vtypeId);
    }

    public Vocabulary queryWordById(Long wordId, Long vtypeId) {
        return vocabularyDao.queryWordById(wordId, vtypeId);
    }

    public List<String> getWordString(Long vtypeId) {
        return vocabularyDao.getWordString(vtypeId);
    }

    public List<Vocabulary> getWordListById(List<Long> wordId, Long vtypeId) {
        return vocabularyDao.getWordListById(wordId, vtypeId);
    }

    public List<Vocabulary> getWordList(Long vtypeId) {
        return vocabularyDao.getWordList(vtypeId);
    }

    public Map<String, Long> getWordIdMap(Long vtypeId) {
        Map<String, Long> wordIdMap = new HashMap<>();
        for (Vocabulary vocab : getWordList(vtypeId)) {
            wordIdMap.put(vocab.getWord(), vocab.getId());
        }
        return wordIdMap;
    }

    private List<Vocabulary> checkExists(Long newType, List<Vocabulary> vList) {
        List<Vocabulary> newList = new ArrayList<>();
        List<String> oldVList = getWordString(newType);
        for (Vocabulary vocab : vList) {
            if (!oldVList.contains(vocab.getWord())) {
                newList.add(vocab);
            }
        }
        return newList;
    }

    public Integer collectNewVocab(Long newType, List<Vocabulary> vList) {
        List<Vocabulary> newList = new ArrayList<>();
        for (Vocabulary v : checkExists(newType, vList)) {
            v.setId(null);
            v.setVtypeId(newType);
            newList.add(v);
        }
        int size = newList.size();
        insert(newList.toArray(new Vocabulary[size]));
        return size;
    }

    public LiveData<Vocabulary> getWord(String word, Long vtypeId) {
        return vocabularyDao.getWord(word, vtypeId);
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

    public void delete(Vocabulary v, Consumer<Integer> consumer) {
        ThreadTask.runOnThread(() -> vocabularyDao.delete(v), consumer);
    }
}
