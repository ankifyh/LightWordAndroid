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
    public static final int MISSING_VDATA = 10003;
    public static final int PARSE_FAILURE = 10004;
    private static final String TAG = ExerciseBuild.class.getSimpleName();
    private MutableLiveData<List<Exercise>> exerciseList = new MutableLiveData<>();
    private MutableLiveData<Integer> exerciseMsg = new MutableLiveData<>();

    private VocabDataRepository vocabDataRepository;
    private ExerciseRepository exerciseRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabularyDataManage vocabularyDataManage;

    private Boolean byFrequency;
    private String isPronounce;

    public void setApplication(Application application) {
        vocabDataRepository = new VocabDataRepository(application);
        exerciseRepository = new ExerciseRepository(application);
        vocabularyRepository = new VocabularyRepository(application);
        vocabularyDataManage = new VocabularyDataManage(application.getBaseContext());
        this.byFrequency = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext())
                .getBoolean("byFrequency", false);
        this.isPronounce = PreferenceManager.getDefaultSharedPreferences(application.getBaseContext())
                .getString("isPronounce", "0");

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

    private String getPronounce(List<String> pronounce) {
        switch (isPronounce) {
            case "0":
                return "";
            case "1":
                switch (pronounce.size()) {
                    case 0:
                        return "";
                    case 1:
                        return pronounce.get(0);
                }
            case "2":
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
                            String[] ss = s.split("[^A-Za-z]");
                            int sl = 0;
                            for (String c : ss) {
                                ans.answer = str_compare(c, w);
                                if (ans.answer == null) {
                                    sl += c.length() > 0 ? c.length() : 1;
                                }
                            }
                            if (ans.answer != null) {
                                ans.index += sl;
                                break;
                            } else {
//                                Pattern p = Pattern.compile(w + "[A-Za-z]{0,4}");
//                                Matcher m = p.matcher(s);
//                                if (m.find()) {
//                                    ans.answer = m.group();
//                                    Log.d(TAG, "后缀匹配: " + ans.answer +
//                                            " " + ans.index +
//                                            " " + sentence);
//                                    break;
//                                }
                                continue;
                            }
                        }
                    }
                    ans.answer = null;
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

                    exercise.setPronounce(getPronounce(exampleList.getPronounce()));

                    Collocation collocation = rdCollocation(exampleList);
                    exercise.setMeaning(collocation.getMeaning());
                    exercise.setPartOfSpeech(collocation.getPartOfSpeech());

                    Example example = rdExample(collocation);
                    String sentence = example.getExample();

                    if (example.hasAnswer()) {
                        inflection = new ArrayList<>();
                        inflection.add(example.getAnswer());
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

    private static class Answer {
        String answer;
        Integer index;
    }

}
