package yk.shiroyk.lightword.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.VocabExercise;
import yk.shiroyk.lightword.ui.managedata.OrderEnum;

public class VocabularyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    public OnInfoClickListener infoClickListener;
    public OnLongClickListener longClickListener;
    public OnSelectedChanged onSelectedChanged;
    private final Context context;
    private final List<Long> selectedItem = new ArrayList<>();
    private final OrderEnum orderEnum;
    private List<VocabExercise> words;
    private boolean multiSelectMode = false;

    public VocabularyAdapter(Context context, List<VocabExercise> words, OrderEnum orderEnum) {
        this.context = context;
        this.words = words;
        this.orderEnum = orderEnum;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        VocabExercise vocab = words.get(position);
        viewHolder.setBackground(vocab.id);
        viewHolder.tv_vocab_word.setText(vocab.word);
        viewHolder.btn_vocab_info.setOnClickListener(view ->
                infoClickListener.onClick(vocab));
        viewHolder.itemView.setOnClickListener(view ->
                viewHolder.setSelected(vocab.id));
        switch (orderEnum) {
            case Correct:
            case Wrong:
                viewHolder.setVocabCorrectWrong(vocab.correct, vocab.wrong);
                break;
            case Frequency:
                viewHolder.setVocabFrequency(vocab.frequency);
                break;
            case LastPractice:
                viewHolder.setVocabDate(vocab.lastPractice);
                break;
            case Timestamp:
                viewHolder.setVocabDate(vocab.timestamp);
                break;
            default:
                viewHolder.tv_vocab_statistic.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void setWords(List<VocabExercise> vocabExerciseList) {
        this.words = vocabExerciseList;
        notifyDataSetChanged();
    }

    public void clearSelected() {
        selectedItem.clear();
        if (words != null)
            notifyItemRangeChanged(0, words.size());
    }

    public List<Long> getSelectedItem() {
        return selectedItem;
    }

    public void exitMultiSelectMode() {
        clearSelected();
        multiSelectMode = false;
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return words.get(i).word.substring(0, 1).toUpperCase();
    }

    public interface OnInfoClickListener {
        void onClick(VocabExercise vocabExercise);
    }

    public void setOnInfoClickListener(OnInfoClickListener infoClickListener) {
        this.infoClickListener = infoClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setOnSelectedChanged(OnSelectedChanged onSelectedChanged) {
        this.onSelectedChanged = onSelectedChanged;
    }

    public interface OnLongClickListener {
        void onLongClick(boolean multiSelectMode);
    }

    public interface OnSelectedChanged {
        void onChanged(Integer size);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_vocab_word;
        private final TextView tv_vocab_statistic;
        private final AppCompatImageButton btn_vocab_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_vocab_word = itemView.findViewById(R.id.tv_vocab_word);
            btn_vocab_info = itemView.findViewById(R.id.btn_vocab_info);
            tv_vocab_statistic = itemView.findViewById(R.id.tv_vocab_statistic);

            itemView.setOnLongClickListener(view -> {
                if (!multiSelectMode) {
                    multiSelectMode = true;
                    longClickListener.onLongClick(true);
                }
                return false;
            });
        }

        public void setVocabCorrectWrong(short correct, short wrong) {
            if (correct > 0 || wrong > 0) {
                tv_vocab_statistic.setText(Html.fromHtml(String.format(Locale.CHINA,
                        "<font color=#66bb6a>%d</font>｜" +
                                "<font color=#ff7043>%d</font>",
                        correct, wrong)));
            } else {
                tv_vocab_statistic.setText(R.string.tv_vocab_statistic);
            }
            tv_vocab_statistic.setVisibility(View.VISIBLE);
        }

        public void setVocabFrequency(Long frequency) {
            tv_vocab_statistic.setVisibility(View.VISIBLE);
            if (frequency != null) {
                tv_vocab_statistic.setText(String.valueOf(frequency));
            }
        }

        public void setVocabDate(Date date) {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm", Locale.CHINA);
            if (date != null) {
                tv_vocab_statistic.setVisibility(View.VISIBLE);
                tv_vocab_statistic.setText(format.format(date));
            } else {
                tv_vocab_statistic.setVisibility(View.GONE);
            }

        }

        public void setSelected(Long id) {
            if (multiSelectMode) {
                boolean selected = selectedItem.contains(id);
                if (selected) {
                    itemView.setSelected(false);
                    selectedItem.remove(id);
                } else {
                    itemView.setSelected(true);
                    selectedItem.add(id);
                }
                onSelectedChanged.onChanged(selectedItem.size());
            }
        }

        public void setBackground(Long id) {
            itemView.setSelected(selectedItem.contains(id));
        }
    }
}
