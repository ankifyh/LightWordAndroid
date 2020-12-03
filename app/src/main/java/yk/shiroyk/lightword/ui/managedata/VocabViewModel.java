/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.ui.managedata;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import yk.shiroyk.lightword.db.constant.OrderEnum;
import yk.shiroyk.lightword.db.constant.VocabFilterEnum;
import yk.shiroyk.lightword.db.entity.VocabExercise;
import yk.shiroyk.lightword.db.entity.VocabType;
import yk.shiroyk.lightword.repository.VocabularyRepository;


public class VocabViewModel extends ViewModel {

    private final MutableLiveData<VocabType> vocabType = new MutableLiveData<>();
    private final MutableLiveData<VocabFilterEnum> vocabFilter = new MutableLiveData<>();
    private final MutableLiveData<OrderEnum> orderBy = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final VocabularyRepository vocabularyRepository;

    public VocabViewModel(Application application) {
        vocabularyRepository = new VocabularyRepository(application);
    }

    public void setVocabType(VocabType vocab) {
        vocabType.setValue(vocab);
    }

    public LiveData<VocabType> getVocabType() {
        return vocabType;
    }

    public LiveData<VocabFilterEnum> getVocabFilter() {
        return vocabFilter;
    }

    public void setVocabFilter(VocabFilterEnum filter) {
        vocabFilter.setValue(filter);
    }

    public void setOrderBy(OrderEnum order) {
        orderBy.setValue(order);
    }

    public LiveData<OrderEnum> getOrderBy() {
        return orderBy;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void setIsLoading(Boolean loading) {
        isLoading.setValue(loading);
    }

    public LiveData<List<VocabExercise>> getVocabExercise() {
        VocabFilterOrderLiveData vocabFilterOrderLiveData = new VocabFilterOrderLiveData(vocabType, vocabFilter, orderBy);
        return Transformations.switchMap(vocabFilterOrderLiveData, vocabFilterOrder -> {
            if (vocabFilterOrder.second == null) return null;
            if (vocabFilterOrder.third == null) return null;
            isLoading.setValue(true);
            switch (vocabFilterOrder.second) {
                case All:
                    return vocabularyRepository.getAllWordListOrderBy(
                            vocabFilterOrder.first.getId(), vocabFilterOrder.third);
                case Review:
                    return vocabularyRepository.getAllReviewWord(
                            vocabFilterOrder.first.getId(), vocabFilterOrder.third);
                case Master:
                    return vocabularyRepository.getAllMasterWord(
                            vocabFilterOrder.first.getId(), vocabFilterOrder.third);
                default:
                    return null;
            }
        });
    }
}
