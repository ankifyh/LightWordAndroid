/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.VocabExerciseData;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.CalDate;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ExerciseRepository {
    private final ExerciseDao exerciseDao;
    private List<Integer> minList = Arrays.asList(5, 20, 720, 1440, 2880, 5760, 10080, 14436, 46080, 92160);
    public static final int EXERCISE_CORRECT = 10005;
    public static final int EXERCISE_WRONG = 10006;
    public static final int EXERCISE_NEW = 10007;
    private final MutableLiveData<Integer> exerciseStatus = new MutableLiveData<>();

    public ExerciseRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.exerciseDao = db.exerciseDao();
    }

    public List<Long> loadReviewWord(long vtypeId, Integer limit) {
        return exerciseDao.LoadReviewWord(vtypeId, limit);
    }

    public void setForgetTime(List<Integer> minList) {
        if (minList.size() > 0) {
            this.minList = minList;
        }
    }

    public Integer getForgetTimeSize() {
        return minList.size();
    }

    public void update(ExerciseData data) {
        ThreadTask.runOnThread(() -> exerciseDao.update(data));
    }

    public void update(ExerciseData[] data) {
        ThreadTask.runOnThread(() -> exerciseDao.update(data));
    }

    public Long getExerciseDataId(Long wordId, Long vtypeId) {
        return exerciseDao.getExerciseDataId(wordId, vtypeId);
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
                        Integer correct = data.getCorrect();
                        Date timestamp = data.getTimestamp();
                        CalDate calDate = new CalDate(minList, timestamp, stage);
                        if (stage < minList.size()) {
                            data.setTimestamp(calDate.timestamp(false));
                            data.setStage(stage + 1);
                        }
                        data.setLastPractice(now);
                        data.setCorrect(correct + 1);
                        update(data);
                        exerciseStatus.setValue(EXERCISE_CORRECT);
                    } else {
                        data = new ExerciseData();
                        data.setTimestamp(new Date(now.getTime() + minList.get(0) * 60 * 1000));
                        data.setWordId(wordId);
                        data.setVtypeId(vtypeId);
                        data.setLastPractice(now);
                        data.setStage(1);
                        data.setCorrect(1);
                        data.setWrong(0);
                        insert(data);
                        exerciseStatus.setValue(EXERCISE_NEW);
                    }
                });
    }

    public void forget(Long wordId, Long vtypeId) {
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        Integer stage = data.getStage();
                        Integer wrong = data.getWrong();
                        Date timestamp = data.getTimestamp();
                        CalDate calDate = new CalDate(minList, timestamp, stage);
                        if (stage > 1) {
                            data.setTimestamp(calDate.timestamp(true));
                            data.setStage(stage - 1);
                        }
                        data.setLastPractice(new Date());
                        data.setWrong(wrong + 1);
                        update(data);
                    }
                    exerciseStatus.setValue(EXERCISE_WRONG);
                });
    }

    public void mastered(Long wordId, Long vtypeId) {
        Date now = new Date();
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        Integer correct = data.getCorrect();
                        data.setTimestamp(new Date(0));
                        data.setStage(99);
                        data.setLastPractice(now);
                        data.setCorrect(correct + 1);
                        update(data);
                        exerciseStatus.setValue(EXERCISE_CORRECT);
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
                        exerciseStatus.setValue(EXERCISE_NEW);
                    }
                });
    }

    public List<VocabExerciseData> getExerciseDataList(Long vtypeId) {
        return exerciseDao.getExerciseDataList(vtypeId);
    }

    public List<Long> getVocabIdList(Long vtypeId) {
        return exerciseDao.getVocabIdList(vtypeId);
    }

    private Map<String, VocabExerciseData> getVocabExerciseMap(Long vtypeId) {
        Map<String, VocabExerciseData> vocabMap = new HashMap<>();
        for (VocabExerciseData data : getExerciseDataList(vtypeId)) {
            vocabMap.put(data.word, data);
        }
        return vocabMap;
    }

    private List<Vocabulary> checkVocabDataExists(Long newVId, List<Vocabulary> vList) {
        List<Vocabulary> newVList = new ArrayList<>();
        List<Long> dataIdList = getVocabIdList(newVId);
        for (Vocabulary vocab : vList) {
            if (!dataIdList.contains(vocab.getId())) {
                newVList.add(vocab);
            }
        }
        return newVList;
    }

    public LiveData<Integer> copyExerciseData(Long oldVId, Long newVId, List<Vocabulary> vList) {
        MutableLiveData<Integer> size = new MutableLiveData<>();
        ThreadTask.runOnThread(() -> {
            Map<String, VocabExerciseData> vocabMap = getVocabExerciseMap(oldVId);
            List<ExerciseData> exerciseData = new ArrayList<>();
            for (Vocabulary vocab : checkVocabDataExists(newVId, vList)) {
                VocabExerciseData vocabExerciseData = vocabMap.get(vocab.getWord());
                if (vocabExerciseData != null) {
                    ExerciseData data = new ExerciseData();
                    data.setWordId(vocab.getId());
                    data.setVtypeId(vocab.getVtypeId());
                    data.setLastPractice(vocabExerciseData.last_practice);
                    data.setTimestamp(vocabExerciseData.timestamp);
                    data.setStage(vocabExerciseData.stage);
                    data.setCorrect(vocabExerciseData.correct);
                    data.setWrong(vocabExerciseData.wrong);
                    exerciseData.add(data);
                }
            }
            return exerciseData;
        }, exerciseData -> {
            int result = exerciseData.size();
            size.setValue(result);
            insert(exerciseData.toArray(new ExerciseData[result]));
        });
        return size;
    }

    public void insertOrUpdate(ExerciseData[] dataArray) {
        if (dataArray.length == 0) return;
        List<ExerciseData> insertData = new ArrayList<>();
        List<ExerciseData> updateData = new ArrayList<>();
        for (ExerciseData data : dataArray) {
            Long id = getExerciseDataId(data.getWordId(), data.getVtypeId());
            if (id == null) insertData.add(data);
            else {
                data.setId(id);
                updateData.add(data);
            }
        }
        int insertSize = insertData.size();
        int updateSize = updateData.size();

        insert(insertData.toArray(new ExerciseData[insertSize]));
        update(updateData.toArray(new ExerciseData[updateSize]));
    }

    public LiveData<Integer> getExerciseStatus() {
        return exerciseStatus;
    }

    public LiveData<Integer> getExerciseProgress(Long vtypeId) {
        return exerciseDao.getExerciseProgress(vtypeId);
    }

    public LiveData<Integer> getExerciseReview(Long vtypeId) {
        return exerciseDao.getExerciseReview(vtypeId);
    }

    public void insert(ExerciseData e) {
        ThreadTask.runOnThread(() -> exerciseDao.insert(e));
    }

    public void insert(ExerciseData[] e) {
        ThreadTask.runOnThread(() -> exerciseDao.insert(e));
    }
}