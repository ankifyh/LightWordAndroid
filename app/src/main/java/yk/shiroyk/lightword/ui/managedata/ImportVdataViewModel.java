package yk.shiroyk.lightword.ui.managedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yk.shiroyk.lightword.repository.VocabDataRepository;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ImportVdataViewModel extends AndroidViewModel {
    private VocabDataRepository vocabDataRepository;

    private Map<Long, String> idToWordMap;

    public ImportVdataViewModel(@NonNull Application application) {
        super(application);
        vocabDataRepository = new VocabDataRepository(application);
    }

    public LiveData<List<Long>> getAllWordId(Long id) {
        return vocabDataRepository.getAllWordId(id);
    }

    public void setWordMap(Map<Long, String> wordMap) {
        this.idToWordMap = wordMap;
    }

    public Integer getWordMapSize() {
        return idToWordMap != null ? this.idToWordMap.size() : 0;
    }

    public LiveData<List<String>> getWordList(Long vtypeId) {
        return Transformations.map(vocabDataRepository.getAllWordId(vtypeId),
                idList -> ThreadTask.runOnThreadCall(idList, wordId -> {
                    if (idToWordMap != null) {
                        List<String> strings = new ArrayList<>();
                        for (Long w : idList) {
                            strings.add(idToWordMap.get(w));
                        }
                        return strings;
                    }
                    return null;
                }));
    }
}
