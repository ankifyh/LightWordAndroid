package yk.shiroyk.lightword.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> subTitle = new MutableLiveData<String>();

    public LiveData<String> getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String title) {
        subTitle.setValue(title);
    }

}
