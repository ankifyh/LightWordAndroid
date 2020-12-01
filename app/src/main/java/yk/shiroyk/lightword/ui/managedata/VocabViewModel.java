package yk.shiroyk.lightword.ui.managedata;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import yk.shiroyk.lightword.db.entity.VocabType;


public class VocabViewModel extends ViewModel {

    private final MutableLiveData<VocabType> vocabType = new MutableLiveData<>();
    private final MutableLiveData<OrderEnum> orderBy = new MutableLiveData<>();

    public LiveData<VocabType> getVocabType() {
        return vocabType;
    }

    public void setVocabType(VocabType v) {
        vocabType.setValue(v);
    }

    public LiveData<OrderEnum> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderEnum order) {
        Log.d("TAG", "setOrderBy: " + order.name());
        orderBy.setValue(order);
    }
}
