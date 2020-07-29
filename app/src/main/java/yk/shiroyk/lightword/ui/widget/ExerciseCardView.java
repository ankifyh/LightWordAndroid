package yk.shiroyk.lightword.ui.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.AnimRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.exercise.Exercise;

public class ExerciseCardView extends CardView {

    private static final String TAG = "ExerciseCardView";

    public static final int DEFAULT_BASELINE = R.color.defaultAnswerBaseLineColor;
    public static final int CORRECT_BASELINE = R.color.correctAnswerBaseLineColor;
    public static final int ERROR_BASELINE = R.color.errorAnswerBaseLineColor;

    private Context context;
    private LinearLayout exercise_card_container;
    private ProgressBar card_progress_circle;
    private TextView tv_card_pos;
    private TextView tv_card_sentence;
    private TextInputLayout card_answer_layout;
    private TextInputEditText et_card_answer;
    private View second_view_answer;
    private TextView tv_card_meaning;

    public ExerciseCardView(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public ExerciseCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public ExerciseCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.exercise_card_view, this, true);
        exercise_card_container = findViewById(R.id.exercise_card_container);
        card_progress_circle = findViewById(R.id.card_progress_circle);
        tv_card_pos = findViewById(R.id.tv_card_pos);
        tv_card_sentence = findViewById(R.id.tv_card_sentence);
        card_answer_layout = findViewById(R.id.card_answer_layout);
        et_card_answer = findViewById(R.id.et_card_answer);
        second_view_answer = findViewById(R.id.second_view_answer);
        tv_card_meaning = findViewById(R.id.tv_card_meaning);

        setCardTypeface();
    }

    public void setCardTypeface() {
        Typeface sansRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Google-Sans-Regular.ttf");
        tv_card_sentence.setTypeface(sansRegular);
        et_card_answer.setTypeface(sansRegular);
    }

    public void setCardData(Exercise exercise) {
        if (exercise != null) {
            tv_card_sentence.setText(exercise.getSentence());
            et_card_answer.setHint(exercise.getAnswer());
            tv_card_pos.setText(exercise.getPartOfSpeech());
            tv_card_meaning.setText(exercise.getMeaning());
            setAnswerInputStyle(exercise.getAnswerIndex(), exercise.getAnswer());
            setStatusColor(exercise.getStatus());
        }
    }

    private void setStatusColor(boolean status) {
        LayerDrawable progressBarDrawable = (LayerDrawable) card_progress_circle.getProgressDrawable();
        int color = status ? R.color.reviewStatusColor : R.color.rememberStatusColor;
        progressBarDrawable.getDrawable(1)
                .setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_IN);
    }

    private void setAnswerInputStyle(Integer answerIndex, String answer) {
        tv_card_sentence.post(() -> {
            int width = Math.round(tv_card_sentence.getPaint().measureText(answer));
            et_card_answer.setMaxWidth(width);

            Layout layout = tv_card_sentence.getLayout();
            int line = layout.getLineForOffset(answerIndex);
            try {
                int x = Math.round(layout.getPrimaryHorizontal(answerIndex));
                int y = layout.getLineTop(line);

                FrameLayout.LayoutParams answerParams = new FrameLayout.LayoutParams(card_answer_layout.getLayoutParams());
                answerParams.setMargins(x, y, 0, 0);
                card_answer_layout.setLayoutParams(answerParams);
            } catch (IndexOutOfBoundsException ex) {
                Log.e(TAG, "Set Answer Layout failed Answer: " + answer + " , Index: " +
                        answerIndex + " ,  Sentence: " + tv_card_sentence.getText().toString());
                ex.printStackTrace();
            }
        });
    }

    public void setCardProgress(Integer progress) {
        card_progress_circle.setProgress(progress);
    }

    public void startExerciseCardAnim() {
        exercise_card_container.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.gradually
        ));
    }

    public void showAnswer() {
        clearAnswer();
        et_card_answer.setBackgroundResource(R.color.transparent);
    }

    public void hideAnswer() {
        et_card_answer.setBackgroundResource(R.color.cardColor);
    }

    public void setAnswerVisibility(int visibility) {
        et_card_answer.setVisibility(visibility);
    }

    public void clearAnswer() {
        et_card_answer.setText("");
    }

    public String getAnswerLowerCaseString() {
        return et_card_answer.getText().toString().trim().toLowerCase();
    }

    public void requestAnswerFocus() {
        et_card_answer.requestFocus();
    }

    public void startAnswerAnim() {
        et_card_answer.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.gradually
        ));
    }

    public void setAnswerBaseLineColor(@ColorRes int color) {
        second_view_answer.setBackgroundResource(color);
    }

    public void startAnswerBaseLineAnim(@AnimRes int anim) {
        second_view_answer.startAnimation(AnimationUtils.loadAnimation(
                context, anim
        ));
    }

    public void setTextSize() {
        int textSize = (int) (tv_card_sentence.getTextSize() / getResources().getDisplayMetrics().scaledDensity);
        if (textSize % 2 > 0) {
            textSize += 1;
        }
        et_card_answer.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void setOnCardProgressClickListener(OnClickListener listener) {
        card_progress_circle.setOnClickListener(listener);
    }

    public void setEtAnswerEditorActionListener(TextInputEditText.OnEditorActionListener listener) {
        et_card_answer.setOnEditorActionListener(listener);
    }

    public void setOnEtAnswerFocusChangeListener(OnFocusChangeListener listener) {
        et_card_answer.setOnFocusChangeListener(listener);
    }

    public void etAnswerTextChangedListener(TextWatcher watcher) {
        et_card_answer.addTextChangedListener(watcher);
    }
}
