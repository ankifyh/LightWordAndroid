package yk.shiroyk.lightword.repository;

import android.app.Application;

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

    public LiveData<Integer> getCount() {
        return vocabularyDao.getCount();
    }

    public LiveData<Vocabulary> getWordById(Long wordId) {
        return vocabularyDao.getWordById(wordId);
    }

    public LiveData<List<String>> getWordStringById(List<Long> wordId) {
        return vocabularyDao.getWordStringById(wordId);
    }

    public LiveData<List<String>> getWordStringIM() {
        return vocabularyDao.getWordStringIM();
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

    public Map<Long, String> getIdToWordMap() {
        Map<Long, String> map = new HashMap<>();
        for (Vocabulary v : vocabularyDao.getAllWord()
        ) {
            map.put(v.getId(), v.getWord());
        }
        return map;
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

    public void insert(Vocabulary[] vocabulary) {
        ThreadTask.runOnThread(vocabulary, v -> {
//            int chunk = 1000;
//            int len = vocabularies.length;
//
//            for (int i = 0; i < len - chunk + 1; i += chunk) {
//                vocabularyDao.insertMany(Arrays.copyOfRange(vocabularies, i, i + chunk));
//            }
//            if (len % chunk != 0) {
//                vocabularyDao.insertMany(Arrays.copyOfRange(vocabularies, len - len % chunk, len));
//            }
            vocabularyDao.insertMany(v);
        });
    }
}
