package yk.shiroyk.lightword.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import yk.shiroyk.lightword.db.LightWordDatabase;
import yk.shiroyk.lightword.db.dao.ExerciseDao;
import yk.shiroyk.lightword.db.entity.ExerciseData;
import yk.shiroyk.lightword.db.entity.Profile;
import yk.shiroyk.lightword.utils.CalDate;

public class ExerciseRepository {
    private ExerciseDao exerciseDao;
    private List<Integer> minList;

    public ExerciseRepository(Application application) {
        LightWordDatabase db = LightWordDatabase.getDatabase(application);
        this.exerciseDao = db.exerciseDao();
    }

    public List<Long> loadReviewWord(long vtypeId, Integer limit) {
        List<ExerciseData> userWordList = exerciseDao.LoadReviewWord(vtypeId, limit);
        List<Long> availableList = new ArrayList<>();
        Long now = new Date().getTime();
        for (ExerciseData e : userWordList
        ) {
            if (e.getTimestamp().getTime() <= now) {
                availableList.add(e.getWordId());
            }
        }
        return availableList;
    }

    public void setForgetTime(List<Integer> minList) {
        if (minList != null) {
            this.minList = minList;
        } else {
            defaultForgetTime();
        }
    }

    public void defaultForgetTime() {
        Profile profile = new Profile();
        List<Integer> minList = Arrays.asList(5, 20, 720, 1440, 2880, 5760, 10080, 14436, 46080, 92160);
        profile.setForgetTime(minList);
        this.minList = profile.getForgetTime();
    }

    public ExerciseData getWord(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        try {
            exerciseData = exerciseDao.getSingleWord(wordId, vtypeId);
        } catch (NullPointerException ex) {
            exerciseData = null;
        }
        return exerciseData;
    }

    public void remember(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        Date now = new Date();
        try {
            exerciseData = exerciseDao.getSingleWord(wordId, vtypeId);
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
            exerciseDao.update(exerciseData);
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
        }
    }

    public void forget(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        try {
            exerciseData = exerciseDao.getSingleWord(wordId, vtypeId);
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
            exerciseDao.update(exerciseData);
        } catch (NullPointerException ignored) {
        }
    }

    public void remembered(Long wordId, Long vtypeId) {
        ExerciseData exerciseData;
        Date now = new Date();
        long tenYears = now.getTime() + 5126400L * 60 * 1000;
        try {
            exerciseData = exerciseDao.getSingleWord(wordId, vtypeId);
            Integer stage = exerciseData.getStage();
            Integer correct = exerciseData.getCorrect();
            if (stage < 10) {
                exerciseData.setTimestamp(new Date(tenYears));
                exerciseData.setStage(11);
            }
            exerciseData.setLastPractice(now);
            exerciseData.setCorrect(correct + 1);
            exerciseDao.update(exerciseData);
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
        }
    }

    public LiveData<Integer> getExerciseProgress(Long vtypeId) {
        return exerciseDao.getExerciseProgress(vtypeId);
    }

    public void insert(ExerciseData exerciseData) {
        new ExerciseRepository.insertAsyncTask(exerciseDao).execute(exerciseData);
    }

    public static class insertAsyncTask extends AsyncTask<ExerciseData, Void, Long> {
        private ExerciseDao mAsyncTaskDao;

        insertAsyncTask(ExerciseDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Long doInBackground(ExerciseData... exerciseData) {
            return mAsyncTaskDao.insert(exerciseData[0]);
        }
    }
}