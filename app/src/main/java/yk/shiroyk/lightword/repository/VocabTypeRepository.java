/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabTypeDao;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.utils.ThreadTask;

public class VocabTypeRepository {
    private final VocabTypeDao vocabTypeDao;

    public VocabTypeRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabTypeDao = db.vocabTypeDao();
    }

    public int update(VocabType v) {
        return vocabTypeDao.update(v);
    }

    public void updateOnThread(VocabType v) {
        ThreadTask.runOnThread(() -> vocabTypeDao.update(v));
    }

    public void delete(VocabType v) {
        ThreadTask.runOnThread(() -> vocabTypeDao.delete(v));
    }

    public void delete(VocabType v, Consumer<Integer> consumer) {
        ThreadTask.runOnThread(() -> vocabTypeDao.delete(v), consumer);
    }

    public List<VocabType> getAllVocabTypes() {
        return vocabTypeDao.getAllVocabTypes();
    }

    public LiveData<List<VocabType>> getAllVocabType() {
        return vocabTypeDao.getAllVocabType();
    }

    public LiveData<VocabType> getVocabTypeById(Long vtypeId) {
        return vocabTypeDao.getVocabTypeById(vtypeId);
    }

    public VocabType queryVocabTypeById(Long vtypeId) {
        return vocabTypeDao.queryVocabTypeById(vtypeId);
    }

    public VocabType getVocabType(String name) {
        return vocabTypeDao.getVocabType(name);
    }

    public Long insert(VocabType v) {
        return vocabTypeDao.insert(v);
    }

}
