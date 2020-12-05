/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.repository;

import android.app.Application;
import android.util.Log;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.constant.Constant;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabExerciseData;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ExerciseRepository {
    private static final String TAG = ExerciseRepository.class.getSimpleName();
    private final ExerciseDao exerciseDao;
    private List<Integer> forgetTime = Arrays.asList(5, 20, 720, 1440, 2880, 5760, 10080, 14436, 46080, 92160);
    private final MutableLiveData<Integer> exerciseStatus = new MutableLiveData<>();

    public ExerciseRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.exerciseDao = db.exerciseDao();
    }

    public List<Long> loadReviewWord(long vtypeId, Integer limit) {
        return exerciseDao.LoadReviewWord(vtypeId, limit);
    }

    public void setForgetTime(List<Integer> forgetTime) {
        if (forgetTime.size() > 0) {
            this.forgetTime = forgetTime;
        }
    }

    public Integer getForgetTimeSize() {
        return forgetTime.size();
    }

    private Date calculateDate(Date timestamp, int stage, boolean minus) {
        if (stage < forgetTime.size()) {
            int calStage = minus && (stage > 1) ? 2 : 0;
            long time = Long.valueOf(forgetTime.get(stage - calStage)) * 60 * 1000;
            timestamp = new Date(new Date().getTime() + time);
            Log.d(TAG, "Calculate Date: " + forgetTime.get(stage - calStage)
                    + " Next Review Time: " + timestamp.toString());
        }
        return timestamp;
    }

    public void update(ExerciseData data) {
        ThreadTask.runOnThread(() -> exerciseDao.update(data));
    }

    public void update(ExerciseData[] data) {
        ThreadTask.runOnThread(() -> exerciseDao.update(data));
    }

    public void update(ExerciseData[] data, Consumer<Integer> consumer) {
        ThreadTask.runOnThread(() -> exerciseDao.update(data), consumer);
    }

    public Set<ExerciseData> getExerciseDataSet(Long vtypeId) {
        return new HashSet<>(exerciseDao.getExerciseData(vtypeId));
    }

    public ExerciseData[] getWordListById(List<Long> idList, Long vtypeId) {
        return exerciseDao.getWordListById(idList, vtypeId);
    }

    public ExerciseData getWordDetail(Long wordId, Long vtypeId) {
        return exerciseDao.getWordDetail(wordId, vtypeId);
    }

    public void remember(Long wordId, Long vtypeId) {
        Date now = new Date();
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        Integer stage = data.getStage();
                        Date timestamp = data.getTimestamp();
                        if (stage < forgetTime.size()) {
                            data.setTimestamp(calculateDate(timestamp, stage, false));
                            data.inStage();
                        }
                        data.setLastPractice(now);
                        data.inCorrect();
                        update(data);
                        exerciseStatus.setValue(Constant.EXERCISE_CORRECT);
                    } else {
                        data = new ExerciseData();
                        data.setTimestamp(new Date(now.getTime() + forgetTime.get(0) * 60 * 1000));
                        data.setWordId(wordId);
                        data.setVtypeId(vtypeId);
                        data.setLastPractice(now);
                        data.setStage(1);
                        data.setCorrect(1);
                        data.setWrong(0);
                        insert(data);
                        exerciseStatus.setValue(Constant.EXERCISE_NEW);
                    }
                });
    }

    public void forget(Long wordId, Long vtypeId) {
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        Integer stage = data.getStage();
                        Date timestamp = data.getTimestamp();
                        if (stage > 1) {
                            data.setTimestamp(calculateDate(timestamp, stage, true));
                            data.deStage();
                        }
                        data.setLastPractice(new Date());
                        data.inWrong();
                        update(data);
                    }
                    exerciseStatus.setValue(Constant.EXERCISE_WRONG);
                });
    }

    public void mastered(Long wordId, Long vtypeId) {
        Date now = new Date();
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        data.setTimestamp(new Date(0));
                        data.setStage(99);
                        data.setLastPractice(now);
                        data.inCorrect();
                        update(data);
                        exerciseStatus.setValue(Constant.EXERCISE_CORRECT);
                    } else {
                        data = new ExerciseData();
                        data.setTimestamp(new Date(0));
                        data.setWordId(wordId);
                        data.setVtypeId(vtypeId);
                        data.setLastPractice(now);
                        data.setStage(99);
                        data.setCorrect(1);
                        data.setWrong(0);
                        insert(data);
                        exerciseStatus.setValue(Constant.EXERCISE_NEW);
                    }
                });
    }

    public List<VocabExerciseData> getVocabExerciseDataList(Long vtypeId) {
        return exerciseDao.getExerciseDataList(vtypeId);
    }

    public Set<VocabExerciseData> getVocabExerciseDataSet(Long vtypeId) {
        return new HashSet<>(exerciseDao.getExerciseDataList(vtypeId));
    }

    private Map<String, VocabExerciseData> getVocabExerciseMap(Long vtypeId) {
        Map<String, VocabExerciseData> vocabMap = new HashMap<>();
        for (VocabExerciseData data : getVocabExerciseDataList(vtypeId)) {
            vocabMap.put(data.word, data);
        }
        return vocabMap;
    }

    public LiveData<Integer[]> copyExerciseData(Boolean mode, Long oldVId, Long newVId, List<Vocabulary> vList) {
        //mode true, only Insert
        //mode false, Insert and Update

        MutableLiveData<Integer[]> size = new MutableLiveData<>();
        ThreadTask.runOnThread(() -> {
            Map<String, VocabExerciseData> vocabMap = getVocabExerciseMap(oldVId);
            Set<ExerciseData> existEDSet = getExerciseDataSet(newVId);
            Set<ExerciseData> newData = new HashSet<>();
            for (Vocabulary vocab : vList) {
                VocabExerciseData exData = vocabMap.get(vocab.getWord());
                if (exData != null) {
                    ExerciseData data = new ExerciseData();
                    data.setWordId(vocab.getId());
                    data.setVtypeId(newVId);
                    data.setTimestamp(exData.timestamp);
                    data.setLastPractice(exData.last_practice);
                    data.setStage(exData.stage);
                    data.setCorrect(exData.correct);
                    data.setWrong(exData.wrong);
                    newData.add(data);
                }
            }

            Integer[] result = new Integer[2];
            if (!mode) {
                Set<ExerciseData> updateSet = new HashSet<>(existEDSet);
                updateSet.retainAll(newData);
                result[1] = updateSet.size();
                update(updateSet.toArray(new ExerciseData[result[1]]));
            }

            newData.removeAll(existEDSet);
            result[0] = newData.size();
            insert(newData.toArray(new ExerciseData[result[0]]));

            size.postValue(result);
        });
        return size;
    }

    public Integer[] insertOrUpdate(Boolean mode, Long vtypeId, Set<ExerciseData> dataSet) {
        Integer[] result = new Integer[2];
        if (dataSet.size() == 0) return result;

        //mode true, only Insert
        //mode false, Insert and Update

        Set<ExerciseData> oldSet = getExerciseDataSet(vtypeId);

        if (!mode) {
            Set<ExerciseData> updateSet = new HashSet<>(oldSet);
            updateSet.retainAll(dataSet);
            result[1] = updateSet.size();
            update(updateSet.toArray(new ExerciseData[result[1]]));
        }

        dataSet.removeAll(oldSet);
        result[0] = dataSet.size();
        insert(dataSet.toArray(new ExerciseData[result[0]]));

        return result;
    }

    public LiveData<Integer> getExerciseStatus() {
        return exerciseStatus;
    }

    public LiveData<Integer> getExerciseProgress(Long vtypeId) {
        return exerciseDao.getExerciseProgress(vtypeId);
    }

    public LiveData<Integer> countExerciseReview(Long vtypeId) {
        return exerciseDao.countExerciseReview(vtypeId);
    }

    public void insert(ExerciseData e) {
        ThreadTask.runOnThread(() -> exerciseDao.insert(e));
    }

    public void insert(ExerciseData[] e) {
        ThreadTask.runOnThread(() -> exerciseDao.insert(e));
    }
}