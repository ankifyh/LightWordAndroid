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

    public Long vtypeInsert(VocabType vocabType) {
        Long id = ThreadTask.runOnThreadCall(vocabType,
                v -> vocabTypeDao.getVocabTypeId(v.getVocabtype()));
        if (id != null) {
            return id;
        } else {
            return ThreadTask.runOnThreadCall(vocabType, v -> vocabTypeDao.insert(v));
        }
    }

    public int update(VocabType vocabType) {
        return ThreadTask.runOnThreadCall(null, v -> vocabTypeDao.update(vocabType));
    }

    public int delete(VocabType vocabType) {
        return ThreadTask.runOnThreadCall(null, v -> vocabTypeDao.delete(vocabType));
    }

    public List<VocabType> getAllVocabTypes() {
        return ThreadTask.runOnThreadCall(null,
                n -> vocabTypeDao.getAllVocabTypes());
    }

    public LiveData<List<VocabType>> getAllVocabType() {
        return vocabTypeDao.getAllVocabType();
    }

    public LiveData<VocabType> getVocabTypeById(Long vtypeId) {
        return vocabTypeDao.getVocabTypeById(vtypeId);
    }

    public VocabType getVocabType(String vocabtype) {
        return vocabTypeDao.getVocabType(vocabtype);
    }

}
