package yk.shiroyk.lightword.ui.managedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MasterWordViewModel extends ViewModel {

    private MutableLiveData<Long> vTypeId = new MutableLiveData<>();

    public LiveData<Long> getVTypeId() {
        return vTypeId;
    }

    public void setVTypeId(Long id) {
        vTypeId.setValue(id);
    }
}
