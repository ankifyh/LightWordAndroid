package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.CalDate;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ExerciseRepository {
    private ExerciseDao exerciseDao;
    private List<Integer> minList = Arrays.asList(5, 20, 720, 1440, 2880, 5760, 10080, 14436, 46080, 92160);
    public static final int EXERCISE_CORRECT = 10005;
    public static final int EXERCISE_WRONG = 10006;
    public static final int EXERCISE_NEW = 10007;
    private MutableLiveData<Integer> exerciseStatus = new MutableLiveData<>();

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

    public ExerciseData[] getWordListById(List<Long> idList, Long vtypeId) {
        return exerciseDao.getWordListById(idList, vtypeId);
    }

    public ExerciseData getWordDetail(Long wordId, Long vtypeId) {
        return exerciseDao.getWordDetail(wordId, vtypeId);
    }

    public List<Vocabulary> getMasterWord(Long vtypeId) {
        return exerciseDao.getMasterWord(vtypeId);
    }

    public LiveData<List<Vocabulary>> searchMasterWord(Long vtypeId, String word) {
        return exerciseDao.searchMasterWord(vtypeId, word);
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
        long tenYears = now.getTime() + 5126400L * 60 * 1000;
        ThreadTask.runOnThread(() -> exerciseDao.getSingleWord(wordId, vtypeId),
                data -> {
                    if (data != null) {
                        Integer stage = data.getStage();
                        Integer correct = data.getCorrect();
                        if (stage < minList.size()) {
                            data.setTimestamp(new Date(tenYears));
                            data.setStage(minList.size() + 1);
                        }
                        data.setLastPractice(now);
                        data.setCorrect(correct + 1);
                        update(data);
                        exerciseStatus.setValue(EXERCISE_CORRECT);
                    } else {
                        data = new ExerciseData();
                        data.setTimestamp(new Date(tenYears));
                        data.setWordId(wordId);
                        data.setVtypeId(vtypeId);
                        data.setLastPractice(now);
                        data.setStage(minList.size() + 1);
                        data.setCorrect(1);
                        data.setWrong(0);
                        insert(data);
                        exerciseStatus.setValue(EXERCISE_NEW);
                    }
                });
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
}