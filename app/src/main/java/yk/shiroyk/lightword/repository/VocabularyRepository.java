package yk.shiroyk.lightword.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.VocabularyDao;
import yk.shiroyk.lightword.db.entity.Vocabulary;

public class VocabularyRepository {

    private VocabularyDao vocabularyDao;

    public VocabularyRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.vocabularyDao = db.vocabularyDao();
    }

    public Vocabulary[] getAllWords() {
        return vocabularyDao.getAllWord();
    }

    public LiveData<Integer> getCount() {
        return vocabularyDao.getCount();
    }

    public Vocabulary getWordById(Long wordId) {
        return vocabularyDao.getWordById(wordId);
    }

    public LiveData<List<String>> getWordStringById(List<Long> wordId) {
        return vocabularyDao.getWordStringById(wordId);
    }

    public LiveData<List<String>> getWordString() {
        return vocabularyDao.getWordString();
    }

    public Vocabulary[] getWordListById(List<Long> wordId) {
        return vocabularyDao.getWordListById(wordId);
    }

    public Vocabulary getWord(String word) {
        return vocabularyDao.getWord(word);
    }

    public Map<Long, String> getIdToWordMap() {
        Map<Long, String> wordMap = new HashMap<>();

        for (Vocabulary v : vocabularyDao.getAllWord()
        ) {
            wordMap.put(v.getId(), v.getWord());
        }
        return wordMap;
    }

    public Map<String, Long[]> getWordToFrequencyMap() {
        Map<String, Long[]> wordMap = new HashMap<>();

        for (Vocabulary v : vocabularyDao.getAllWord()
        ) {
            Long[] data = new Long[2];
            data[0] = v.getId();
            data[1] = v.getFrequency();
            wordMap.put(v.getWord(), data);
        }
        return wordMap;
    }

    public void insert(Vocabulary[] vocabulary) {
        new insertAsyncTask(vocabularyDao).execute(vocabulary);
    }

    public static class insertAsyncTask extends AsyncTask<Vocabulary, Void, Void> {
        private VocabularyDao mAsyncTaskDao;

        insertAsyncTask(VocabularyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Vocabulary... vocabularies) {
//            int chunk = 1000;
//            int len = vocabularies.length;
//
//            for (int i = 0; i < len - chunk + 1; i += chunk) {
//                mAsyncTaskDao.insertMany(Arrays.copyOfRange(vocabularies, i, i + chunk));
//            }
//            if (len % chunk != 0) {
//                mAsyncTaskDao.insertMany(Arrays.copyOfRange(vocabularies, len - len % chunk, len));
//            }
            mAsyncTaskDao.insertMany(vocabularies);
            return null;
        }
    }
}
