/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import yk.shiroyk.lightword.R;

public class ColorPickAdapter extends ArrayAdapter {
    private final Context context;
    private final TypedArray colorRes;
    OnColorClickedListener listener;

    public ColorPickAdapter(@NonNull Context context,
                            int resource,
                            TypedArray colorRes) {
        super(context, resource);
        this.context = context;
        this.colorRes = colorRes;
    }

    @Override
    public int getCount() {
        return colorRes.length();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_color_btn, parent, false);
        }
        Button button = convertView.findViewById(R.id.btn_color_pick);
        String color = context.getResources().
                getResourceEntryName(colorRes.getResourceId(position, 0));
        button.setBackgroundColor(colorRes.getColor(position, 0));
        button.setOnClickListener(view -> {
            if (listener != null)
                listener.onClicked(color);
        });
        return convertView;
    }

    public interface OnColorClickedListener {
        void onClicked(String newTheme);
    }

    public void setOnColorClickedListener(OnColorClickedListener listener) {
        this.listener = listener;
    }
}


