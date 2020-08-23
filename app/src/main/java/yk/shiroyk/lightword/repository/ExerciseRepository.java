package yk.shiroyk.lightword.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.utils.CalDate;
import yk.shiroyk.lightword.utils.ThreadTask;

public class ExerciseRepository {
    private ExerciseDao exerciseDao;
    private List<Integer> minList = Arrays.asList(5, 20, 720, 1440, 2880, 5760, 10080, 14436, 46080, 92160);

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
        ThreadTask.runOnThread(data, d -> exerciseDao.update(d));
    }

    public ExerciseData getWordDetail(Long wordId, Long vtypeId) {
        return exerciseDao.getWordDetail(wordId, vtypeId);
    }

    public LiveData<List<Vocabulary>> getMasterWord(Long vtypeId) {
        return exerciseDao.getMasterWord(vtypeId);
    }

    public LiveData<List<Vocabulary>> searchMasterWord(Long vtypeId, String word) {
        return exerciseDao.searchMasterWord(vtypeId, word);
    }

    private ExerciseData getWord(Long wordId, Long vtypeId) {
        return Observable.create((ObservableOnSubscribe<ExerciseData>) emitter -> {
            emitter.onNext(exerciseDao.getSingleWord(wordId, vtypeId));
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .blockingSingle();
    }

    public boolean remember(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        Date now = new Date();
        try {
            exerciseData = getWord(wordId, vtypeId);
            Integer stage = exerciseData.getStage();
            Integer correct = exerciseData.getCorrect();
            Date timestamp = exerciseData.getTimestamp();
            CalDate calDate = new CalDate(minList, timestamp, stage);
            if (stage < 10) {
                exerciseData.setTimestamp(calDate.timestamp(false));
                exerciseData.setStage(stage + 1);
            }
            exerciseData.setLastPractice(now);
            exerciseData.setCorrect(correct + 1);
            update(exerciseData);
            return false;
        } catch (NullPointerException ex) {
            exerciseData = new ExerciseData();
            exerciseData.setTimestamp(new Date(now.getTime() + minList.get(0) * 60 * 1000));
            exerciseData.setWordId(wordId);
            exerciseData.setVtypeId(vtypeId);
            exerciseData.setLastPractice(now);
            exerciseData.setStage(1);
            exerciseData.setCorrect(1);
            exerciseData.setWrong(0);
            insert(exerciseData);
            return true;
        }
    }

    public void forget(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        try {
            exerciseData = getWord(wordId, vtypeId);
            Integer stage = exerciseData.getStage();
            Integer wrong = exerciseData.getWrong();
            Date timestamp = exerciseData.getTimestamp();
            CalDate calDate = new CalDate(minList, timestamp, stage);
            if (stage > 1) {
                exerciseData.setTimestamp(calDate.timestamp(true));
                exerciseData.setStage(stage - 1);
            }
            exerciseData.setLastPractice(new Date());
            exerciseData.setWrong(wrong + 1);
            update(exerciseData);
        } catch (NullPointerException ignored) {
        }
    }

    public boolean remembered(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        Date now = new Date();
        long tenYears = now.getTime() + 5126400L * 60 * 1000;
        try {
            exerciseData = getWord(wordId, vtypeId);
            Integer stage = exerciseData.getStage();
            Integer correct = exerciseData.getCorrect();
            if (stage < 10) {
                exerciseData.setTimestamp(new Date(tenYears));
                exerciseData.setStage(minList.size() + 1);
            }
            exerciseData.setLastPractice(now);
            exerciseData.setCorrect(correct + 1);
            update(exerciseData);
            return false;
        } catch (NullPointerException ex) {
            exerciseData = new ExerciseData();
            exerciseData.setTimestamp(new Date(tenYears));
            exerciseData.setWordId(wordId);
            exerciseData.setVtypeId(vtypeId);
            exerciseData.setLastPractice(now);
            exerciseData.setStage(11);
            exerciseData.setCorrect(1);
            exerciseData.setWrong(0);
            insert(exerciseData);
            return true;
        }
    }

    public LiveData<Integer> getExerciseProgress(Long vtypeId) {
        return exerciseDao.getExerciseProgress(vtypeId);
    }

    public void insert(ExerciseData exerciseData) {
        ThreadTask.runOnThread(exerciseData, e -> exerciseDao.insert(e));
    }
}