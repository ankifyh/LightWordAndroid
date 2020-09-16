package yk.shiroyk.lightword.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.Vocabulary;

public class VocabularyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    public OnInfoClickListener infoClickListener;
    public OnLongClickListener longClickListener;
    public OnSelectedChanged onSelectedChanged;
    private Context context;
    private List<Vocabulary> words;
    private List<Long> selectedItem = new ArrayList<>();
    private boolean multiSelectMode = false;

    public VocabularyAdapter(Context context, List<Vocabulary> words) {
        this.context = context;
        this.words = words;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Vocabulary vocabulary = words.get(position);
        viewHolder.setBackground(vocabulary.getId());
        viewHolder.tv_vocab_word.setText(vocabulary.getWord());
        viewHolder.btn_vocab_info.setOnClickListener(view ->
                infoClickListener.onClick(vocabulary));
        viewHolder.itemView.setOnClickListener(view ->
                viewHolder.setSelected(vocabulary.getId()));

    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void setWords(List<Vocabulary> vocabularyList) {
        this.words = vocabularyList;
        notifyDataSetChanged();
    }

    public void clearSelected() {
        selectedItem.clear();
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
        return words.get(i).getWord().substring(0, 1).toUpperCase();
    }

    public interface OnInfoClickListener {
        void onClick(Vocabulary vocabulary);
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
        private TextView tv_vocab_word;
        private AppCompatImageButton btn_vocab_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_vocab_word = itemView.findViewById(R.id.tv_vocab_word);
            btn_vocab_info = itemView.findViewById(R.id.btn_vocab_info);

            itemView.setOnLongClickListener(view -> {
                if (!multiSelectMode) {
                    multiSelectMode = true;
                    longClickListener.onLongClick(true);
                }
                return false;
            });
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
