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

import java.util.List;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.Vocabulary;

public class VocabularyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    final OnInfoClickListener listener;
    private Context context;
    private List<Vocabulary> words;

    public VocabularyAdapter(Context context, List<Vocabulary> words, OnInfoClickListener listener) {
        this.context = context;
        this.words = words;
        this.listener = listener;
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
        viewHolder.tv_vocab_word.setText(vocabulary.getWord());
        viewHolder.btn_vocab_info.setOnClickListener(view -> {
            listener.onClick(vocabulary);
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void setWords(List<Vocabulary> vocabularyList) {
        this.words = vocabularyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return words.get(i).getWord().substring(0, 1).toUpperCase();
    }

    public interface OnInfoClickListener {
        void onClick(Vocabulary vocabulary);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_vocab_word;
        private AppCompatImageButton btn_vocab_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_vocab_word = itemView.findViewById(R.id.tv_vocab_word);
            btn_vocab_info = itemView.findViewById(R.id.btn_vocab_info);
        }
    }
}
