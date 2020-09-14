package yk.shiroyk.lightword.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.exercise.Example;

public class ExampleDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private OnItemClickListener listener;
    private Context context;
    private List<Example> exampleList = new ArrayList<>();
    private boolean multiLine;

    public ExampleDetailAdapter(Context context, OnItemClickListener listener, boolean multiLine) {
        this.context = context;
        this.listener = listener;
        this.multiLine = multiLine;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_vocab_detail, parent, false), multiLine);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Example example = exampleList.get(position);
        viewHolder.tv_detail_title.setText(example.getExample());
        viewHolder.tv_detail_subtitle.setText(example.getTranslation());
        if (example.hasAnswer()) {
            viewHolder.tv_detail_extra_title.setText(example.getAnswer());
        }
        if (listener != null) {
            viewHolder.itemView.setOnClickListener(view -> listener.onClick(example));
        }
    }

    @Override
    public int getItemCount() {
        return exampleList != null ? exampleList.size() : 0;
    }

    public void addExample(Example example) {
        exampleList.add(example);
        notifyDataSetChanged();
    }

    public List<Example> getExampleList() {
        return exampleList;
    }

    public void setExampleList(List<Example> exampleList) {
        this.exampleList = exampleList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(Example example);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_detail_title, tv_detail_subtitle, tv_detail_extra_title;

        public ViewHolder(@NonNull View itemView, boolean multiLine) {
            super(itemView);
            tv_detail_title = itemView.findViewById(R.id.tv_detail_title);
            tv_detail_subtitle = itemView.findViewById(R.id.tv_detail_subtitle);
            tv_detail_extra_title = itemView.findViewById(R.id.tv_detail_extra_title);
            if (multiLine) {
                tv_detail_title.setSingleLine(false);
                tv_detail_subtitle.setSingleLine(false);
                tv_detail_extra_title.setSingleLine(false);
            }
        }
    }
}
