/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import yk.shiroyk.lightword.MainActivity;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.exercise.Collocation;
import yk.shiroyk.lightword.db.entity.exercise.Example;
import yk.shiroyk.lightword.db.entity.exercise.Exercise;
import yk.shiroyk.lightword.db.entity.exercise.ExerciseList;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.UserStatisticRepository;
import yk.shiroyk.lightword.repository.VocabularyRepository;
import yk.shiroyk.lightword.ui.adapter.ExampleDetailAdapter;
import yk.shiroyk.lightword.ui.adapter.VocabDetailAdapter;
import yk.shiroyk.lightword.ui.widget.ExerciseCardView;
import yk.shiroyk.lightword.utils.ExerciseBuild;
import yk.shiroyk.lightword.utils.ThreadTask;
import yk.shiroyk.lightword.utils.VocabFileManage;

public class ExerciseFragment extends Fragment {

    private static final String TAG = "ExerciseFragment";

    private Context context;
    private ExerciseRepository exerciseRepository;
    private UserStatisticRepository statisticRepository;
    private VocabularyRepository vocabularyRepository;
    private VocabFileManage vocabFileManage;
    private ExerciseBuild exerciseBuild;
    private SharedPreferences sp;

    private LinearLayout exercise_container;
    private TextView tv_tip;
    private ExerciseCardView exercise_card;
    private TextView tv_translation;
    private ImageView btn_prev_card;
    private TextView tv_daily_target;
    private ToggleButton exercise_speech;

    private VocabDetailAdapter vocabDetailAdapter;
    private ExampleDetailAdapter exampleDetailAdapter;

    private TextToSpeech tts;
    private List<Exercise> exerciseList;
    private String dailyTarget;
    private String speakString;
    private String answer;
    private Long wordId;
    private Long vtypeId;
    private Integer cardQuantity = 10;
    private Integer cardIndex = 0;
    private Integer currentCard = 0;
    private Integer ttsSpeech = 0;
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
        statisticRepository = new UserStatisticRepository(getActivity().getApplication());
        vocabularyRepository = new VocabularyRepository(getActivity().getApplication());
        vocabFileManage = new VocabFileManage(getActivity().getBaseContext());
        exerciseBuild = new ViewModelProvider(this).get(ExerciseBuild.class);
        exerciseBuild.setApplication(this.getActivity().getApplication());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_exercise, container, false);
        context = root.getContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        setHasOptionsMenu(true);
        init(root);

        getCardData(cardQuantity);
        setExerciseListObserve();
        setTargetObserve();
        setTargetDialog();
        setStatusObserve();
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
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem info = menu.findItem(R.id.action_info);
        info.setVisible(true);
        info.setOnMenuItemClickListener(menuItem -> {
            setWordInfoDialog();
            return false;
        });
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setTargetObserve() {
        statisticRepository.getTodayCount().observe(getViewLifecycleOwner(), todayTarget -> {
            if (dailyTarget.equals("0")) {
                tv_daily_target.setVisibility(View.INVISIBLE);
            } else {
                tv_daily_target.setVisibility(View.VISIBLE);
                tv_daily_target.setText(String.format(getString(
                        R.string.exercise_fragment_today_target), todayTarget, dailyTarget));
            }
        });
    }

    private void setTargetDialog() {
        statisticRepository.getTodayCount().observe(getViewLifecycleOwner(), integer -> {
            int parseInt = Integer.parseInt(dailyTarget);
            if (parseInt != 0) {
                tv_daily_target.setVisibility(View.VISIBLE);
                tv_daily_target.setText(String.format(getString(
                        R.string.exercise_fragment_today_target), integer, dailyTarget));
                if (integer == parseInt) {
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.drawable.ic_create)
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
        exercise_container = root.findViewById(R.id.exercise_container);
        tv_tip = root.findViewById(R.id.tv_tip);
        exercise_card = root.findViewById(R.id.exercise_card);
        tv_translation = root.findViewById(R.id.tv_translation);
        btn_prev_card = root.findViewById(R.id.btn_prev_card);
        tv_daily_target = root.findViewById(R.id.tv_daily_target);
        exercise_speech = root.findViewById(R.id.exercise_speech);

        String cardValue = sp.getString("cardQuantity", "10");
        dailyTarget = sp.getString("dailyTarget", "0");
        vtypeId = sp.getLong("vtypeId", 1L);
        cardQuantity = Integer.parseInt(cardValue);

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

        ttsSpeech = sp.getInt("ttsSpeech", 0);
        if (ttsSpeech != 0) {
            isSpeech = true;
            exercise_speech.setChecked(true);
        }
        exercise_speech.setOnClickListener(view -> {
            int s;
            if (isSpeech) {
                s = 0;
            } else {
                if (ttsSpeech == 0) {
                    ttsSpeech = 1;
                }
                s = ttsSpeech;
            }
            isSpeech = !isSpeech;
            sp.edit().putInt("ttsSpeech", s).apply();

        });

        exercise_card.setReplayClickLister(view -> {
            if (initTTSSuccess) {
                tts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null, "");
            }
        });

    }

    private void setWordInfoDialog() {
        if (wordId != null) {
            ThreadTask.runOnThread(() -> vocabularyRepository.queryWordById(wordId, vtypeId), v -> {
                View view = getLayoutInflater().inflate(R.layout.layout_info_vocab, null);
                RecyclerView infoList = view.findViewById(R.id.recycler_info_vocab);

                vocabDetailAdapter = new VocabDetailAdapter(context,
                        this::setExampleInfoDialog, true);
                infoList.setLayoutManager(new LinearLayoutManager(context));
                infoList.setAdapter(vocabDetailAdapter);
                String wordInfo = vocabFileManage.readFile(vtypeId, v.getWord());
                ExerciseList eList = new Gson().fromJson(wordInfo, ExerciseList.class);
                vocabDetailAdapter.setCollocations(eList.getCollocation());

                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.mean_and_pos)
                        .setView(view)
                        .setNeutralButton(R.string.dialog_save, (dialogInterface, i) -> {
                            if (eList.getCollocation().size() == 0) {
                                Toast.makeText(context, String.format(
                                        getString(R.string.vocab_save_fail_collcation_not_null),
                                        v.getWord()), Toast.LENGTH_SHORT).show();
                            } else {
                                saveVocabulary(v.getWord(), eList);
                            }
                        })
                        .setPositiveButton(R.string.dialog_cancel, null).create().show();
            });
        }
    }

    private void setExampleInfoDialog(Collocation collocation) {
        View view = getLayoutInflater().inflate(R.layout.layout_info_vocab, null);
        RecyclerView infoList = view.findViewById(R.id.recycler_info_vocab);

        exampleDetailAdapter = new ExampleDetailAdapter(context,
                this::setExampleDialog, true);
        infoList.setLayoutManager(new LinearLayoutManager(context));
        infoList.setAdapter(exampleDetailAdapter);
        exampleDetailAdapter.setExampleList(collocation.getExample());

        new MaterialAlertDialogBuilder(context)
                .setTitle(collocation.getMeaning())
                .setView(view)
                .setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                    vocabDetailAdapter.getCollocations().remove(collocation);
                    vocabDetailAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void setExampleDialog(Example example) {
        View view = getLayoutInflater().inflate(R.layout.item_vocab_detail, null);
        TextView tv_example = view.findViewById(R.id.tv_detail_title);
        TextView tv_translation = view.findViewById(R.id.tv_detail_subtitle);

        tv_example.setSingleLine(false);
        tv_translation.setSingleLine(false);

        tv_example.setText(example.getExample());
        tv_translation.setText(example.getTranslation());

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.example)
                .setView(view)
                .setNeutralButton(R.string.dialog_delete, (dialogInterface, i) -> {
                    if (exampleDetailAdapter.getExampleList().size() > 1) {
                        exampleDetailAdapter.getExampleList().remove(example);
                        exampleDetailAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, R.string.example_del_fail_example_not_null,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
    }

    private void saveVocabulary(String word, ExerciseList eList) {
        String json = new Gson().toJson(eList, ExerciseList.class);
        vocabFileManage.overWriteFile(json, vtypeId, word);
        Toast.makeText(context, String.format(
                getString(R.string.vocab_save_success), word), Toast.LENGTH_SHORT).show();
    }

    private void reDialog(View view) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.exercise_fragment_exclude_dialog_title)
                .setMessage(R.string.exercise_fragment_exclude_dialog_message)
                .setPositiveButton(R.string.dialog_ensure, (dialogInterface, i) -> {
                    mastered(wordId, vtypeId);
                    exercise_card.showAnswer();
                    if (isSpeech && initTTSSuccess) {
                        tts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null, "next");
                    } else {
                        getNextCardSync();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null).create().show();
    }

    private void remember(Long wordId, Long vtypeId) {
        exerciseRepository.remember(wordId, vtypeId);
    }

    private void forget(Long wordId, Long vtypeId) {
        exerciseRepository.forget(wordId, vtypeId);
    }

    private void mastered(Long wordId, Long vtypeId) {
        Toast.makeText(context, answer + " 添加到已经掌握成功！", Toast.LENGTH_SHORT).show();
        exerciseRepository.mastered(wordId, vtypeId);
    }

    private void setStatusObserve() {
        exerciseRepository.getExerciseStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case ExerciseRepository.EXERCISE_NEW:
                    statisticRepository.updateCount();
                    statisticRepository.updateCorrect();
                    break;
                case ExerciseRepository.EXERCISE_WRONG:
                    statisticRepository.updateWrong();
                    break;
                case ExerciseRepository.EXERCISE_CORRECT:
                    statisticRepository.updateCorrect();
                    break;
            }
        });
    }

    private void prevCard() {
        if (cardIndex > 0) {
            cardIndex -= 1;
            setCardData(cardIndex);
            setPronounceAndReplayVisible();
            if (cardIndex == 0) {
                btn_prev_card.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void nextCard() {
        if (currentCard.equals(cardIndex)) {
            if (correctFlag) {
                String uAnswer = exercise_card.getAnswerLowerCaseString();
                if ((answer.length() > 0 && answer.toLowerCase().equals(uAnswer)) || inflection.contains(uAnswer)) {
                    exercise_card.setAnswerBaseLineColor(ExerciseCardView.CORRECT_BASELINE);
                    correctFlag = false;
                    remember(wordId, vtypeId);

                    if (isSpeech && initTTSSuccess) {
                        tts.speak(speakString, TextToSpeech.QUEUE_FLUSH, null, "next");
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
            setPronounceAndReplayVisible();
        }
    }

    private void setPronounceAndReplayVisible() {
        if (currentCard <= cardIndex) {
            exercise_card.setAnswerVisibility(View.VISIBLE);
            exercise_card.setPronounceVisibility(View.GONE);
            exercise_card.setReplayVisibility(View.GONE);
        } else {
            exercise_card.setAnswerVisibility(View.INVISIBLE);
            exercise_card.setPronounceVisibility(View.VISIBLE);
            if (initTTSSuccess)
                exercise_card.setReplayVisibility(View.VISIBLE);
        }
    }

    private void getNextCard() {
        exercise_card.setAnswerBaseLineColor(ExerciseCardView.DEFAULT_BASELINE);
        if (cardIndex < exerciseList.size() - 1) {
            cardIndex += 1;
            currentCard = cardIndex;
            setCardData(cardIndex);
            btn_prev_card.setVisibility(View.VISIBLE);
            exercise_card.clearAnswer();
            exercise_card.startExerciseCardAnim();
            setPronounceAndReplayVisible();
        } else {
            getCardData(cardQuantity);
        }
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
        exerciseBuild.autoExercise(vtypeId, limit);
    }

    private void setExerciseListObserve() {
        exerciseBuild.getExerciseList()
                .observe(getViewLifecycleOwner(), exercises -> {
                    if (exercises.size() > 0) {
                        exerciseList = exercises;
                        tv_tip.setVisibility(View.GONE);
                        exercise_container.setVisibility(View.VISIBLE);
                        tv_translation.setVisibility(View.VISIBLE);
                        btn_prev_card.setVisibility(View.INVISIBLE);
                        setCardData(cardIndex);
                        exercise_card.clearAnswer();
                        exercise_card.startExerciseCardAnim();
                    } else {
                        tv_tip.setVisibility(View.VISIBLE);
                        exercise_container.setVisibility(View.GONE);
                        tv_translation.setVisibility(View.GONE);
                        setExerciseMsgObserve();
                    }
                });
    }

    private void setExerciseMsgObserve() {
        exerciseBuild.getExerciseMsg()
                .observe(getViewLifecycleOwner(),
                        msg -> {
                            switch (msg) {
                                case ExerciseBuild.MISSING_VOCAB:
                                    tv_tip.setText(R.string.missing_vocab_data);
                                    break;
                                case ExerciseBuild.PARSE_FAILURE:
                                    tv_tip.setText(R.string.parse_vocab_data_error);
                                    break;
                            }
                        });
    }

    private void setCardData(Integer cardIndex) {
        Exercise exercise = exerciseList.get(cardIndex);
        if (exercise.getInflection() != null) {
            inflection = exercise.getInflection();
        } else {
            inflection = new ArrayList<>();
        }
        answer = exercise.getAnswer();
        speakString = ttsSpeech == 2 ? exercise.getSentence() : answer;
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
            if ("next".equals(s))
                ((MainActivity) context).runOnUiThread(ExerciseFragment.this::getNextCard);
        }

        @Override
        public void onError(String s) {
        }
    }
}
