package yk.shiroyk.lightword.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabTypeDao;
import yk.shiroyk.lightword.db.entity.VocabType;

public class VocabTypeRepository {
    private VocabTypeDao vocabTypeDao;

    public VocabTypeRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabTypeDao = db.vocabTypeDao();
    }

    public Long vtypeInsert(VocabType vocabType) throws ExecutionException, InterruptedException {
        Long id = getVocabTypeId(vocabType.getVocabtype());
        if (id != null) {
            return id;
        } else {
            return new insertAsyncTask(vocabTypeDao).execute(vocabType).get();
        }
    }

    public int update(VocabType vocabType) {
        return vocabTypeDao.update(vocabType);
    }

    public int delete(VocabType vocabtype) {
        return vocabTypeDao.delete(vocabtype);
    }

    public List<VocabType> getAllVocabType() {
        return vocabTypeDao.getAllVocabType();
    }

    public LiveData<VocabType> getVocabTypeById(Long vtypeId) {
        return vocabTypeDao.getVocabTypeById(vtypeId);
    }

    public Long getVocabTypeId(String vocabtype) {
        return vocabTypeDao.getVocabTypeId(vocabtype);
    }

    public VocabType getVocabType(String vocabtype) {
        return vocabTypeDao.getVocabType(vocabtype);
    }

    public static class insertAsyncTask extends AsyncTask<VocabType, Void, Long> {
        private VocabTypeDao mAsyncTaskDao;

        insertAsyncTask(VocabTypeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Long doInBackground(VocabType... vocabTypes) {
            return mAsyncTaskDao.insert(vocabTypes[0]);
        }
    }

}
