package yk.shiroyk.lightword.ui.managedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import yk.shiroyk.lightword.db.entity.VocabType;


public class ImportVdataViewModel extends ViewModel {

    private MutableLiveData<VocabType> vocabType = new MutableLiveData<>();

    public LiveData<VocabType> getVocabType() {
        return vocabType;
    }

    public void setVocabType(VocabType v) {
        vocabType.setValue(v);
    }

}
