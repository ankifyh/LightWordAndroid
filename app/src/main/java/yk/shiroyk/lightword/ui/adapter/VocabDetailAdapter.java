/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

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
import yk.shiroyk.lightword.db.entity.exercise.Collocation;

public class VocabDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final OnItemClickListener listener;
    private final Context context;
    private List<Collocation> collocations = new ArrayList<>();
    private final boolean multiLine;

    public VocabDetailAdapter(Context context, OnItemClickListener listener, boolean multiLine) {
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
        Collocation collocation = collocations.get(position);
        viewHolder.tv_detail_title.setText(collocation.getMeaning());
        viewHolder.tv_detail_subtitle.setText(collocation.getPartOfSpeech());
        viewHolder.itemView.setOnClickListener(view -> listener.onClick(collocation));
    }

    @Override
    public int getItemCount() {
        return collocations != null ? collocations.size() : 0;
    }

    public void addCollocation(Collocation collocation) {
        collocations.add(collocation);
        notifyDataSetChanged();
    }

    public List<Collocation> getCollocations() {
        return collocations;
    }

    public void setCollocations(List<Collocation> collocations) {
        this.collocations = collocations;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(Collocation collocation);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_detail_title;
        private final TextView tv_detail_subtitle;

        public ViewHolder(@NonNull View itemView, boolean multiLine) {
            super(itemView);
            tv_detail_title = itemView.findViewById(R.id.tv_detail_title);
            tv_detail_subtitle = itemView.findViewById(R.id.tv_detail_subtitle);
            if (multiLine) {
                tv_detail_title.setSingleLine(false);
                tv_detail_subtitle.setSingleLine(false);
            }
        }
    }

}
