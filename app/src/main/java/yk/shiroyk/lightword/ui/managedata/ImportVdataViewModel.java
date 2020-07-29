package yk.shiroyk.lightword.ui.managedata;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yk.shiroyk.lightword.db.entity.VocabData;

public class ImportVdataViewModel extends ViewModel {
    private Map<Long, String> idToWordMap;
    private MutableLiveData<List<String>> wordList = new MutableLiveData<>();

    private LiveData<List<String>> mWordList = Transformations.map(wordList, input -> input);

    public void setWordMap(Map<Long, String> wordMap) {
        this.idToWordMap = wordMap;
    }

    public Integer getWordMapSize() {
        return idToWordMap != null ? this.idToWordMap.size() : 0;
    }

    public void setVocabData(VocabData[] vocabDataList) {
        List<Long> idList = new ArrayList<>();
        for (VocabData v : vocabDataList) {
            idList.add(v.getWordId());
        }
        setWordList(idList);
    }

    public LiveData<List<String>> getWordList() {
        return mWordList;
    }

    public void setWordList(List<Long> wordId) {
        List<String> strings = new ArrayList<>();
        if (wordId.size() > 0) {
            for (Long w : wordId) {
                strings.add(idToWordMap.get(w));
            }
        }
        Log.d("WordList", strings.size() + "");
        wordList.setValue(strings);
    }
}
