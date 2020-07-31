package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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

    public LiveData<List<Vocabulary>> getAllWords() {
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

    public LiveData<Map<Long, String>> getIdToWordMap() {
        return Transformations.switchMap(vocabularyDao.getAllWord(), wordList -> {
            MutableLiveData<Map<Long, String>> result = new MutableLiveData<>();
            Map<Long, String> wordMap = ThreadTask.runOnThreadCall(wordList, list -> {
                Map<Long, String> map = new HashMap<>();
                for (Vocabulary v : wordList
                ) {
                    map.put(v.getId(), v.getWord());
                }
                return map;
            });
            result.setValue(wordMap);
            return result;
        });
    }

    public LiveData<Map<String, Long[]>> getWordToFrequencyMap() {
        return Transformations.switchMap(vocabularyDao.getAllWord(), wordList -> {
            MutableLiveData<Map<String, Long[]>> result = new MutableLiveData<>();
            Map<String, Long[]> wordMap = ThreadTask.runOnThreadCall(wordList, list -> {
                Map<String, Long[]> map = new HashMap<>();
                for (Vocabulary v : list
                ) {
                    Long[] data = new Long[2];
                    data[0] = v.getId();
                    data[1] = v.getFrequency();
                    map.put(v.getWord(), data);
                }
                return map;
            });
            result.setValue(wordMap);
            return result;
        });
    }

    public void insert(Vocabulary[] vocabulary) {
        ThreadTask.runOnThread(vocabulary, (v) -> {
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
