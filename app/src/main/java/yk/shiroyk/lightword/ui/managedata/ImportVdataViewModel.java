package yk.shiroyk.lightword.ui.managedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.repository.VocabDataRepository;

public class ImportVdataViewModel extends AndroidViewModel {
    private VocabDataRepository vocabDataRepository;

    private Map<Long, String> idToWordMap;
    private MutableLiveData<List<String>> wordList = new MutableLiveData<>();

    public ImportVdataViewModel(@NonNull Application application) {
        super(application);
        vocabDataRepository = new VocabDataRepository(application);
    }

    public void setWordMap(Map<Long, String> wordMap) {
        this.idToWordMap = wordMap;
    }

    public Integer getWordMapSize() {
        return idToWordMap != null ? this.idToWordMap.size() : 0;
    }

    public LiveData<List<String>> getWordList() {
        return wordList;
    }

    public void setWordList(Long vtypeId) {
        Observable.create(
                (ObservableOnSubscribe<List<Long>>) emitter -> {
                    emitter.onNext(vocabDataRepository.getAllWordId(vtypeId));
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(longs -> {
                    List<String> strings = new ArrayList<>();
                    if (idToWordMap.size() > 0) {
                        for (Long w : longs) {
                            strings.add(idToWordMap.get(w));
                        }
                    }
                    return strings;
                }).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull List<String> stringList) {
                wordList.postValue(stringList);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }
}
