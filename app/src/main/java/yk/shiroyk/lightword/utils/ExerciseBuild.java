package yk.shiroyk.lightword.utils;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.db.entity.exercise.Collocation;
import yk.shiroyk.lightword.db.entity.exercise.Example;
import yk.shiroyk.lightword.db.entity.exercise.Exercise;
import yk.shiroyk.lightword.db.entity.exercise.ExerciseList;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabDataRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;

public class ExerciseBuild extends ViewModel {
    private static final String TAG = ExerciseBuild.class.getSimpleName();

    public static final int MISSING_VDATA = 10003;
    public static final int PARSE_FAILURE = 10004;

    private MutableLiveData<List<Exercise>> exerciseList = new MutableLiveData<>();
    private MutableLiveData<Integer> exerciseMsg = new MutableLiveData<>();

    private VocabDataRepository vocabDataRepository;
    private ExerciseRepository exerciseRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabularyDataManage vocabularyDataManage;

    private Boolean byFrequency;

    public void setApplication(Application application) {
        vocabDataRepository = new VocabDataRepository(application);
        exerciseRepository = new ExerciseRepository(application);
        vocabularyRepository = new VocabularyRepository(application);
        vocabularyDataManage = new VocabularyDataManage(application.getBaseContext());
        this.byFrequency = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext())
                .getBoolean("byFrequency", false);

    }

    private ExerciseList parseJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ExerciseList.class);
    }

    private Collocation rdCollocation(ExerciseList exerciseList) {
        List<Collocation> collocation = exerciseList.getCollocation();
        Collections.shuffle(collocation);

        return collocation.get(0);
    }

    private Example rdExample(Collocation collocation) {
        List<Example> examples = collocation.getExample();
        Collections.shuffle(examples);

        return examples.get(0);
    }

    private String[] getPronounce(String[] pronounce) {
        return null;
    }

    private String str_compare(String a, String b) {
        String c = null;
        if (a.equals(b)) {
            c = a;
        } else if (a.equals(b.substring(0, 1).toUpperCase()
                + b.substring(1).toLowerCase())) {
            c = a;
        }
        return c;
    }

    private AnswerObject getAnswer(List<String> inflection, String sentence) {
        AnswerObject answerObject = new AnswerObject();
        answerObject.answer = null;
        String[] st = sentence.split("\\s");

        for (String w : inflection) {

            String a = null;
            if (answerObject.answer != null) {
                break;
            } else {
                answerObject.answerIndex = 0;
            }
            for (String s : st) {
                //一般匹配
                a = str_compare(s, w);
                if (a != null) {
                    answerObject.answer = a;
                    Log.d(TAG, "一般匹配: " + answerObject.answer +
                            "  " + answerObject.answerIndex +
                            "  " + sentence);
                    break;
                } else {
                    answerObject.answerIndex += s.length() + 1;
                }
            }
            if (answerObject.answer == null) {
                //去除字符匹配
                answerObject.answerIndex = 0;
                for (String s : st) {

                    String[] ss = s.split("[^A-Za-z-]");
                    int sl = 0;
                    for (String c : ss) {
                        a = str_compare(c, w);
                        if (a != null) {
                            answerObject.answer = a;
                            answerObject.answerIndex += sl;
                            Log.d(TAG, "字符匹配: " + answerObject.answer +
                                    "  " + answerObject.answerIndex +
                                    "  " + sentence);
                        } else {
                            sl += c.length() > 0 ? c.length() : 1;
                        }
                    }
                    if (answerObject.answer != null) {
                        break;
                    } else {
                        answerObject.answerIndex += s.length() + 1;
                    }
                }
            }
            if (answerObject.answer == null) {
                //后缀匹配
                answerObject.answerIndex = 0;
                for (String s : st) {
                    String pattern = w + "[A-Za-z]{0,4}";
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(s);
                    if (m.find()) {
                        answerObject.answer = m.group();
                        Log.d(TAG, "后缀匹配: " + answerObject.answer +
                                "  " + answerObject.answerIndex +
                                "  " + sentence);
                        break;
                    } else {
                        answerObject.answerIndex += s.length() + 1;
                    }
                }
            }
        }

        return answerObject;
    }

    private List<Exercise> buildExercise(List<Long> wordList, boolean status) {
        List<Exercise> exerciseList = new ArrayList<>();
        Vocabulary[] vocabularies = vocabularyRepository.getWordListById(wordList);
        if (wordList != null) {
            Exercise exercise;
            for (Vocabulary vocabulary : vocabularies) {
                exercise = new Exercise();

                String word = vocabulary.getWord();
                String ex = vocabularyDataManage.readFile(word);

                exercise.setId(vocabulary.getId());
                exercise.setWord(word);
                exercise.setStatus(status);

                ExerciseList exampleList = parseJson(ex);
                if (exampleList != null) {

                    List<String> inflection = exampleList.getInflection();
                    exercise.setInflection(inflection);

                    Collocation collocation = rdCollocation(exampleList);
                    exercise.setMeaning(collocation.getMeaning());
                    exercise.setPartOfSpeech(collocation.getPartOfSpeech());

                    Example example = rdExample(collocation);
                    String sentence = example.getExample();

                    AnswerObject answerObject = getAnswer(inflection, sentence);
                    String answer = answerObject.answer;
                    int answerIndex = answerObject.answerIndex;
                    if (answer == null) {
                        answer = word;
                        sentence = word;
                        answerIndex = 0;
                    }
                    exercise.setSentence(sentence);
                    exercise.setTranslation(example.getTranslation());
                    exercise.setAnswer(answer);
                    exercise.setAnswerIndex(answerIndex);

                    exerciseList.add(exercise);
                } else {
                    Log.e(TAG, "词库例句缺失: " + "Word: " + word);
                }
            }
        }

        return exerciseList;
    }

    public LiveData<Integer> getExerciseMsg() {
        return exerciseMsg;
    }

    public void newExercise(Long vtypeId, Integer limit) {
        ThreadTask.runOnThread(() -> {
            List<Long> idList = vocabDataRepository.loadNewWord(vtypeId, byFrequency, limit);
            if (idList.size() == 0) {
                exerciseMsg.postValue(MISSING_VDATA);
            }
            List<Exercise> exercises = buildExercise(idList, false);
            if (idList.size() != 0 && exercises.size() == 0) {
                exerciseMsg.postValue(PARSE_FAILURE);
            }
            exerciseList.postValue(exercises);
        });
    }

    public void autoExercise(Long vtypeId, Integer limit) {
        ThreadTask.runOnThread(() -> {
            List<Long> idList = exerciseRepository.loadReviewWord(vtypeId, limit);
            if (idList.size() < limit) {
                newExercise(vtypeId, limit);
            } else {
                List<Exercise> exercises = buildExercise(idList, true);
                if (idList.size() != 0 && exercises.size() == 0) {
                    exerciseMsg.postValue(PARSE_FAILURE);
                }
                exerciseList.postValue(exercises);
            }
        });
    }

    public LiveData<List<Exercise>> getExerciseList() {
        return exerciseList;
    }

    private static class AnswerObject {
        String answer;
        Integer answerIndex;
    }

}
