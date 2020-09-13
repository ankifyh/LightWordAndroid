package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Vocabulary> getAllWord() {
        return vocabularyDao.getAllWord();
    }

    public LiveData<List<Vocabulary>> getAllWordList() {
        return vocabularyDao.getAllWordList();
    }

    public LiveData<Integer> getCount() {
        return vocabularyDao.getCount();
    }

    public LiveData<Vocabulary> getWordById(Long wordId) {
        return vocabularyDao.getWordById(wordId);
    }

    public List<String> getWordString() {
        return vocabularyDao.getWordString();
    }

    public Vocabulary[] getWordListById(List<Long> wordId) {
        return vocabularyDao.getWordListById(wordId);
    }

    public LiveData<Vocabulary> getWord(String word) {
        return vocabularyDao.getWord(word);
    }

    public Vocabulary queryWord(String word) {
        return vocabularyDao.queryWord(word);
    }

    public LiveData<List<Vocabulary>> searchWord(String word) {
        return vocabularyDao.searchWord(word);
    }

    public Map<String, Long[]> getWordToFrequencyMap() {
        Map<String, Long[]> map = new HashMap<>();
        for (Vocabulary v : vocabularyDao.getAllWord()
        ) {
            Long[] data = new Long[2];
            data[0] = v.getId();
            data[1] = v.getFrequency();
            map.put(v.getWord(), data);
        }
        return map;
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
