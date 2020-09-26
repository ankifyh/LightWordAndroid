package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabularyDao;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.ThreadTask;

public class VocabularyRepository {

    private VocabularyDao vocabularyDao;

    public VocabularyRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabularyDao = db.vocabularyDao();
    }

    public LiveData<List<Vocabulary>> getAllWordList(Long vtypeId) {
        return vocabularyDao.getAllWord(vtypeId);
    }

    public LiveData<Integer> getCount(Long vtypeId) {
        return vocabularyDao.getCount(vtypeId);
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

    public LiveData<Vocabulary> getWord(String word, Long vtypeId) {
        return vocabularyDao.getWord(word, vtypeId);
    }

    public Vocabulary queryWord(String word, Long vtypeId) {
        return vocabularyDao.queryWord(word, vtypeId);
    }

    public LiveData<List<Vocabulary>> searchWord(String word, Long vtypeId) {
        return vocabularyDao.searchWord(word, vtypeId);
    }

    public List<Vocabulary> loadNewWord(Long vtypeId, Boolean order, Integer limit) {
        return vocabularyDao.loadNewWord(vtypeId, order, limit);
    }

    public void insert(Vocabulary[] v) {
        ThreadTask.runOnThread(() -> vocabularyDao.insertMany(v));
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
