package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabDataDao;
import yk.shiroyk.lightword.db.entity.VocabData;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.ThreadTask;

public class VocabDataRepository {
    private VocabDataDao vocabDataDao;

    public VocabDataRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabDataDao = db.vocabDataDao();
    }

    public List<Long> loadNewWord(long vtypeId, boolean order, Integer limit) {
        return vocabDataDao.loadNewWord(vtypeId, order, limit);
    }

    public LiveData<Integer> getCount(Long vtypeId) {
        return vocabDataDao.getCount(vtypeId);
    }

    public List<Long> getAllWordId(Long vtypeId) {
        return vocabDataDao.getAllWordId(vtypeId);
    }

    public LiveData<List<Vocabulary>> getAllWord(Long vtypeId) {
        return vocabDataDao.getAllWord(vtypeId);
    }

    public LiveData<List<Vocabulary>> searchWord(Long vtypeId, String word) {
        return vocabDataDao.searchWord(vtypeId, word);
    }

    public void insert(VocabData[] v) {
        ThreadTask.runOnThread(() -> vocabDataDao.insert(v));
    }
}
