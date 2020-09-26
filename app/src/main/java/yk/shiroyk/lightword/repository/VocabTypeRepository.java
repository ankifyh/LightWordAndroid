package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabTypeDao;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.utils.ThreadTask;

public class VocabTypeRepository {
    private VocabTypeDao vocabTypeDao;

    public VocabTypeRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabTypeDao = db.vocabTypeDao();
    }

    public int update(VocabType v) {
        return vocabTypeDao.update(v);
    }

    public void updateAmount(VocabType v) {
        ThreadTask.runOnThread(() -> vocabTypeDao.update(v));
    }

    public void delete(VocabType v) {
        ThreadTask.runOnThread(() -> vocabTypeDao.delete(v));
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
