package yk.shiroyk.lightword.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.exercise.Exercise;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.ui.viewmodel.SharedViewModel;
import yk.shiroyk.lightword.ui.widget.ExerciseCardView;
import yk.shiroyk.lightword.utils.ExerciseBuild;

public class ExerciseFragment extends Fragment {

    private static final String TAG = "ExerciseFragment";

    private Context context;
    private ExerciseRepository exerciseRepository;
    private ExerciseBuild exerciseBuild;
    private SharedViewModel sharedViewModel;
    private SharedPreferences sp;
    private CompositeDisposable compositeDisposable;

    private ProgressBar exercise_loading;
    private LinearLayout exercise_container;
    private TextView tv_tip;
    private ExerciseCardView exercise_card;
    private TextView tv_translation;
    private ImageView btn_prev_card;
    private TextView tv_daily_target;
    private ToggleButton exercise_speech;

    private TextToSpeech tts;
    private List<Exercise> exerciseList;
    private String dailyTarget;
    private String speakString;
    private String answer;
    private Long wordId;
    private Long vtypeId;
    private Integer cardIndex = 0;
    private Integer currentCard = 0;
    private String ttsSpeech = "close";
    private boolean correctFlag = true;
    private boolean wrongFlag = true;
    private boolean showAnswer = false;
    private boolean isSpeech = false;
    private boolean initTTSSuccess = false;
    private List<String> inflection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        exerciseBuild = new ViewModelProvider(this).get(ExerciseBuild.class);
        exerciseBuild.setApplication(this.getActivity().getApplication());
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exercise, container, false);
        context = root.getContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        init(root);

        getCardData(10);
        initTarget();
        setUptargetDialog();
        initTTS();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        compositeDisposable.dispose();
    }

    private void updateTarget() {
        int target = sp.getInt("todayTarget", 0);
        long timestamp = new Date().getTime();
        target += 1;
        sp.edit().putLong("lastUpdateTarget", timestamp).apply();
        sp.edit().putInt("todayTarget", target).apply();
        sharedViewModel.setTarget(target);
    }

    private void initTarget() {
        int todayTarget = sp.getInt("todayTarget", 0);
        SimpleDateFormat formatter = new SimpleDateFormat("dd", Locale.CHINA);
        long timestamp = new Date().getTime();
        long lastUpdate = sp.getLong("lastUpdateTarget", timestamp);
        String nowDay = formatter.format(timestamp);
        String lastDay = formatter.format(new Date(lastUpdate));
        if (Integer.parseInt(nowDay) > Integer.parseInt(lastDay)) {
            sharedViewModel.setTarget(0);
            sp.edit().putInt("todayTarget", 0).apply();
        }
        if (dailyTarget.equals("0")) {
            tv_daily_target.setVisibility(View.INVISIBLE);
        } else {
            tv_daily_target.setVisibility(View.VISIBLE);
            tv_daily_target.setText(String.format(getString(
                    R.string.exercise_fragment_today_target), todayTarget, dailyTarget));
        }
    }

    private void setUptargetDialog() {
        sharedViewModel.getTarget().observe(getViewLifecycleOwner(), integer -> {
            int parseInt = Integer.parseInt(dailyTarget);
            if (parseInt != 0) {
                tv_daily_target.setVisibility(View.VISIBLE);
                tv_daily_target.setText(String.format(getString(
                        R.string.exercise_fragment_today_target), integer, dailyTarget));
                if (integer == parseInt) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.drawable.ic_edit)
                            .setTitle(String.format(getString(
                                    R.string.exercise_fragment_target_dialog_title), integer, parseInt))
                            .setNegativeButton(R.string.dialog_ensure, null).create().show();
                }
            } else {
                tv_daily_target.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void init(View root) {
        exercise_loading = root.findViewById(R.id.exercise_loading);
        exercise_container = root.findViewById(R.id.exercise_container);
        tv_tip = root.findViewById(R.id.tv_tip);
        exercise_card = root.findViewById(R.id.exercise_card);
        tv_translation = root.findViewById(R.id.tv_translation);
        btn_prev_card = root.findViewById(R.id.btn_prev_card);
        tv_daily_target = root.findViewById(R.id.tv_daily_target);
        exercise_speech = root.findViewById(R.id.exercise_speech);

        String preValue = sp.getString("vtypeId", "1");
        dailyTarget = sp.getString("dailyTarget", "0");
        vtypeId = Long.valueOf(preValue);

        exercise_card.setOnCardProgressClickListener(this::reDialog);
        exercise_card.setEtAnswerEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                nextCard();
                return true;
            } else if (i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                nextCard();
                return true;
            }
            return false;
        });
        exercise_card.setOnEtAnswerFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                exercise_card.startAnswerBaseLineAnim(R.anim.scale_x);
            } else {
                exercise_card.startAnswerBaseLineAnim(R.anim.scale_y);
            }
        });
        exercise_card.etAnswerTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (showAnswer) {
                    exercise_card.hideAnswer();
                    exercise_card.startAnswerAnim();
                    wrongFlag = true;
                    showAnswer = false;
                }
            }
        });
        btn_prev_card.setOnClickListener(view -> {
            prevCard();
        });
        root.findViewById(R.id.btn_next_card).setOnClickListener(view -> {
            if (exerciseList.size() > 0) {
                nextCard();
            }
        });

        ttsSpeech = sp.getString("ttsSpeech", "close");
        if (!"close".equals(ttsSpeech)) {
            isSpeech = true;
            exercise_speech.setChecked(true);
        }
        exercise_speech.setOnClickListener(view -> {
            String s;
            if (isSpeech) {
                s = "close";
            } else {
                if ("close".equals(ttsSpeech)) {
                    ttsSpeech = "vocabulary";
                }
                s = ttsSpeech;
            }
            isSpeech = !isSpeech;
            Log.d("ns", s + " t " + ttsSpeech + " is " + isSpeech);
            sp.edit().putString("ttsSpeech", s).apply();

        });

    }

    private void reDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.exercise_fragment_exclude_dialog_title)
                .setMessage(R.string.exercise_fragment_exclude_dialog_message)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    remembered(wordId, vtypeId);
                    exercise_card.showAnswer();
                    tts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null, "");
                })
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void remember(Long wordId, Long vtypeId) {
        exerciseRepository.defaultForgetTime();
        if (exerciseRepository.remember(wordId, vtypeId)) {
            updateTarget();
        }
    }

    private void forget(Long wordId, Long vtypeId) {
        exerciseRepository.defaultForgetTime();
        exerciseRepository.forget(wordId, vtypeId);
    }

    private void remembered(Long wordId, Long vtypeId) {
        exerciseRepository.defaultForgetTime();
        if (exerciseRepository.remembered(wordId, vtypeId)) {
            updateTarget();
        }
    }

    private void prevCard() {
        if (cardIndex > 0) {
            cardIndex -= 1;
            setCardData(cardIndex);
            exercise_card.startExerciseCardAnim();
            exercise_card.setAnswerVisibility(View.GONE);
            if (cardIndex == 0) {
                btn_prev_card.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void nextCard() {
        if (currentCard.equals(cardIndex)) {
            if (correctFlag) {
                String uAnswer = exercise_card.getAnswerLowerCaseString();
                if ((answer.length() > 0 && answer.equals(uAnswer)) || inflection.contains(uAnswer)) {
                    exercise_card.setAnswerBaseLineColor(ExerciseCardView.CORRECT_BASELINE);
                    correctFlag = false;
                    remember(wordId, vtypeId);

                    if (isSpeech && initTTSSuccess) {
                        tts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null, "");
                    } else {
                        getNextCardSync();
                    }
                } else {
                    if (wrongFlag) {
                        wrongFlag = false;
                        forget(wordId, vtypeId);
                        exercise_card.showAnswer();
                        showAnswer = true;
                    }
                    exercise_card.setAnswerBaseLineColor(ExerciseCardView.ERROR_BASELINE);
                }
            }
        } else {
            cardIndex += 1;
            setCardData(cardIndex);
            exercise_card.startExerciseCardAnim();
            btn_prev_card.setVisibility(View.VISIBLE);
            if (currentCard > cardIndex) {
                exercise_card.setAnswerVisibility(View.GONE);
            } else {
                exercise_card.setAnswerVisibility(View.VISIBLE);
            }
        }
    }

    private void getNextCard() {
        exercise_card.clearAnswer();
        exercise_card.setAnswerBaseLineColor(ExerciseCardView.DEFAULT_BASELINE);
        if (cardIndex < exerciseList.size() - 1) {
            cardIndex += 1;
            currentCard = cardIndex;
            setCardData(cardIndex);
            btn_prev_card.setVisibility(View.VISIBLE);
        } else {
            getCardData(10);
        }
        exercise_card.startExerciseCardAnim();
        correctFlag = true;
        exercise_card.hideAnswer();
    }

    private void getNextCardSync() {
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                getNextCard();
            }
        }.start();
    }

    private void getCardData(Integer limit) {
        cardIndex = 0;
        currentCard = 0;
        exercise_container.setVisibility(View.GONE);
        tv_translation.setVisibility(View.GONE);
        exerciseBuild.autoExercise(vtypeId, limit);
        exerciseBuild.getExerciseList()
                .observe(getViewLifecycleOwner(), exercises -> {
                    if (exercises.size() > 0) {
                        exerciseList = exercises;
                        setCardData(cardIndex);
                        tv_tip.setVisibility(View.GONE);
                        exercise_container.setVisibility(View.VISIBLE);
                        tv_translation.setVisibility(View.VISIBLE);
                        btn_prev_card.setVisibility(View.INVISIBLE);
                    } else {
                        tv_tip.setVisibility(View.VISIBLE);
                        exerciseBuild.getExerciseMsg()
                                .observe(getViewLifecycleOwner(),
                                        msg -> tv_tip.setText(msg));
                    }
                });
    }

    private void setCardData(Integer cardIndex) {
        Exercise exercise = exerciseList.get(cardIndex);
        inflection = exercise.getInflection();
        answer = exercise.getAnswer();
        speakString = "sentence".equals(ttsSpeech) ? exercise.getSentence() : answer;
        Log.d(TAG, "vtypeId: " + vtypeId + " Answer: " + answer + " Word: " +
                exercise.getWord() + "\nSentence: " + exercise.getSentence());
        wordId = exercise.getId();

        tv_translation.setText(exercise.getTranslation());
        exercise_card.setCardData(exercise);
        exercise_card.setCardProgress((cardIndex + 1) * 100 / exerciseList.size());
    }

    public void setTTS(float pitch, float rate) {
        tts.setPitch(pitch);
        tts.setSpeechRate(rate);
    }

    private Disposable setOnDoneObserve(String s) {
        return Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    emitter.onNext(s);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s1 -> getNextCard());
    }

    private void initTTS() {
        if (tts == null) {
            tts = new TextToSpeech(context, new TTSEngine());
        }
    }

    private final class TTSEngine implements TextToSpeech.OnInitListener {

        @Override
        public void onInit(int i) {
            if (i == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                tts.setOnUtteranceProgressListener(new TTSUtteranceProgressListener());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(context, "语音包丢失或语音不支持", Toast.LENGTH_SHORT).show();
                } else {
                    initTTSSuccess = true;
                }
            } else {
                Toast.makeText(context, "TTS引擎初始化失败", Toast.LENGTH_SHORT).show();
                exercise_speech.setChecked(false);
                exercise_speech.setClickable(false);
            }
        }
    }

    private class TTSUtteranceProgressListener extends UtteranceProgressListener {

        @Override
        public void onStart(String s) {

        }

        @Override
        public void onDone(String s) {
            compositeDisposable.add(setOnDoneObserve(s));
        }

        @Override
        public void onError(String s) {
        }
    }
}
