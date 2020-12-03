/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.ui.managedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import yk.shiroyk.lightword.db.constant.OrderEnum;
import yk.shiroyk.lightword.db.constant.VocabFilterEnum;
import yk.shiroyk.lightword.db.entity.Triple;
import yk.shiroyk.lightword.db.entity.VocabType;

public class VocabFilterOrderLiveData extends MediatorLiveData<Triple<VocabType, VocabFilterEnum, OrderEnum>> {
    public VocabFilterOrderLiveData(LiveData<VocabType> vocabType,
                                    LiveData<VocabFilterEnum> vocabFilter,
                                    LiveData<OrderEnum> orderBy) {
        addSource(vocabType, vocab ->
                setValue(Triple.create(vocab, vocabFilter.getValue(), orderBy.getValue())));
        addSource(vocabFilter, filter ->
                setValue(Triple.create(vocabType.getValue(), filter, orderBy.getValue())));
        addSource(orderBy, order ->
                setValue(Triple.create(vocabType.getValue(), vocabFilter.getValue(), order)));
    }
}
