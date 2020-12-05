/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

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

import yk.shiroyk.lightword.db.constant.Constant;
import yk.shiroyk.lightword.db.entity.Vocabulary;
import yk.shiroyk.lightword.db.entity.exercise.Collocation;
import yk.shiroyk.lightword.db.entity.exercise.Example;
import yk.shiroyk.lightword.db.entity.exercise.Exercise;
import yk.shiroyk.lightword.db.entity.exercise.ExerciseList;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;

public class ExerciseBuild extends ViewModel {
    private static final String TAG = ExerciseBuild.class.getSimpleName();
    private final MutableLiveData<List<Exercise>> exerciseList = new MutableLiveData<>();
    private final MutableLiveData<Integer> exerciseMsg = new MutableLiveData<>();

    private ExerciseRepository exerciseRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabFileManage vocabFileManage;

    private Boolean byFrequency;
    private Integer isPronounce;

    public void setApplication(Application application) {
        exerciseRepository = new ExerciseRepository(application);
        vocabularyRepository = new VocabularyRepository(application);
        vocabFileManage = new VocabFileManage(application.getBaseContext());
        this.byFrequency = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext())
                .getBoolean("byFrequency", false);
        this.isPronounce = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext())
                .getInt("isPronounce", 0);

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

        if (examples.size() > 0)
            return examples.get(0);
        else
            return null;
    }

    private String getPronounce(List<String> pronounce) {
        switch (isPronounce) {
            case 0:
                return "";
            case 1:
                switch (pronounce.size()) {
                    case 0:
                        return "";
                    case 1:
                        return pronounce.get(0);
                }
            case 2:
                switch (pronounce.size()) {
                    case 0:
                        return "";
                    case 1:
                        return pronounce.get(0);
                    case 2:
                        return pronounce.get(1);
                }
            default:
                return "";
        }
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase()
                + s.substring(1).toLowerCase();
    }

    private String str_compare(String a, String b) {
        String c = null;
        if (a.equals(b)) {
            c = a;
        } else if (a.equals(capitalize(b))) {
            c = a;
        }
        return c;
    }

    private Answer getAnswer(List<String> inflection, String sentence) {
        Answer ans = new Answer();
        String[] st = sentence.split("\\s");
        String a = null;
        int index = 0;

        for (String w : inflection) {
            if (ans.answer != null) {
                break;
            } else {
                ans.index = 0;
            }
            for (String s : st) {
                // eg: find 'ever' in sentence 'I have never, ever, forgive myself.'
                if (s.length() >= w.length()) {
                    if (Pattern.matches(".*" + w + ".*", s.toLowerCase())) {
                        ans.answer = str_compare(s, w);
                        if (ans.answer != null) {
                            break;
                        } else {
                            // split symbol
                            String[] ss = s.split("(?!^)\\b");
                            int sl = 0;
                            for (String c : ss) {
                                ans.answer = str_compare(c, w);
                                if (ans.answer == null) {
                                    sl += c.length();
                                } else {
                                    break;
                                }
                            }
                            if (ans.answer != null) {
                                ans.index += sl;
                                break;
                            }
                        }
                    }
                }
                ans.index += s.length() + 1;
            }
            if (a == null) {
                Pattern p = Pattern.compile(w);
                Matcher m = p.matcher(sentence);
                if (m.find()) {
                    a = m.group();
                    index = m.start();
                } else {
                    p = Pattern.compile(capitalize(w));
                    m = p.matcher(sentence);
                    if (m.find()) {
                        a = m.group();
                        index = m.start();
                    }
                }
            }
        }
        if (ans.answer == null) {
            ans.answer = a;
            ans.index = index;
        } else {
            if (a != null && a.length() > ans.answer.length()) {
                ans.answer = a;
                ans.index = index;
            }
        }
        return ans;
    }

    private List<Exercise> buildExercise(List<Vocabulary> wordList, Long vtypeId, boolean status) {
        List<Exercise> exerciseList = new ArrayList<>();
        if (wordList != null) {
            Exercise exercise;
            for (Vocabulary vocabulary : wordList) {
                exercise = new Exercise();

                String word = vocabulary.getWord();
                String ex = vocabFileManage.readFile(vtypeId, word);

                exercise.setId(vocabulary.getId());
                exercise.setWord(word);
                exercise.setStatus(status);

                ExerciseList exampleList = parseJson(ex);
                if (exampleList != null) {

                    List<String> inflection = exampleList.getInflection();
                    if (inflection == null)
                        inflection = new ArrayList<>();
                    exercise.setInflection(inflection);

                    exercise.setPronounce(getPronounce(exampleList.getPronounce()));

                    try {
                        Collocation collocation = rdCollocation(exampleList);
                        exercise.setMeaning(collocation.getMeaning());
                        exercise.setPartOfSpeech(collocation.getPartOfSpeech());

                        Example example = rdExample(collocation);
                        if (example == null) {
                            throw new NullPointerException();
                        }
                        String sentence = example.getExample();

                        if (example.hasAnswer()) {
                            inflection.add(example.getAnswer());
                        } else {
                            inflection.add(word);
                        }

                        Answer ans = getAnswer(inflection, sentence);
                        String answer = ans.answer;
                        String translation = example.getTranslation();
                        int index = ans.index;
                        if (answer == null) {
                            answer = word;
                            sentence = word;
                            index = 0;
                            translation = "";
                        }
                        exercise.setSentence(sentence);
                        exercise.setTranslation(translation);
                        exercise.setAnswer(answer);
                        exercise.setAnswerIndex(index);

                        exerciseList.add(exercise);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Log.e(TAG, "例句缺失 - " + "Word: " + word);
                    }
                } else {
                    Log.e(TAG, "词汇例句缺失 - " + "Word: " + word);
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
            List<Vocabulary> idList = vocabularyRepository.loadNewWord(vtypeId, byFrequency, limit);
            if (idList.size() == 0) {
                exerciseMsg.postValue(Constant.MISSING_VOCAB);
            }
            List<Exercise> exercises = buildExercise(idList, vtypeId, false);
            if (idList.size() != 0 && exercises.size() == 0) {
                exerciseMsg.postValue(Constant.PARSE_FAILURE);
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
                List<Vocabulary> wordList = vocabularyRepository.getWordListById(idList, vtypeId);
                List<Exercise> exercises = buildExercise(wordList, vtypeId, true);
                if (idList.size() != 0 && exercises.size() == 0) {
                    exerciseMsg.postValue(Constant.PARSE_FAILURE);
                }
                exerciseList.postValue(exercises);
            }
        });
    }

    public LiveData<List<Exercise>> getExerciseList() {
        return exerciseList;
    }

    private static class Answer {
        String answer;
        Integer index;
    }

}
