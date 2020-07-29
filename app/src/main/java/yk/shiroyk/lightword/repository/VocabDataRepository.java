package yk.shiroyk.lightword.repository;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabDataDao;
import yk.shiroyk.lightword.db.entity.VocabData;

public class VocabDataRepository {
    private VocabDataDao vocabDataDao;

    public VocabDataRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabDataDao = db.vocabDataDao();
    }

    public List<Long> loadNewWord(long vtypeId, boolean order, Integer limit) {
        return vocabDataDao.loadNewWord(vtypeId, order, limit);
    }

    public Integer getCount(Long vtypeId) {
        return vocabDataDao.getCount(vtypeId);
    }

    public List<Long> getAllWordId(Long vtypeId) {
        return vocabDataDao.getAllWordId(vtypeId);
    }

    public void insert(VocabData[] vocabData) {
        new VocabDataRepository.insertAsyncTask(vocabDataDao).execute(vocabData);
    }

    public static class insertAsyncTask extends AsyncTask<VocabData, Void, Void> {
        private VocabDataDao mAsyncTaskDao;

        insertAsyncTask(VocabDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(VocabData... vocabData) {
            mAsyncTaskDao.insertMany(vocabData);
            return null;
        }
    }

}
