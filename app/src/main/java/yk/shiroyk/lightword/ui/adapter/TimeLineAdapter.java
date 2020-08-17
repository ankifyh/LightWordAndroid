package yk.shiroyk.lightword.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.UserStatistic;

public class TimeLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TOP = 0;
    private static final int VIEW_NORMAL = 1;
    private LayoutInflater inflater;
    private List<UserStatistic> statisticList;

    public TimeLineAdapter(Context context, List<UserStatistic> userStatistic) {
        this.inflater = LayoutInflater.from(context);
        this.statisticList = userStatistic;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_home_timeline, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        UserStatistic statistic = statisticList.get(position);
        if (getItemViewType(position) == VIEW_TOP) {
            viewHolder.home_timeline_top.setVisibility(View.INVISIBLE);
        }
        int correct = statistic.getCorrect();
        int wrong = statistic.getWrong();
        int count = correct + wrong;

        viewHolder.home_timeline_date.setText(
                String.format(Locale.CHINA, "%s 共%d张",
                        statistic.getFormatDay(), count));

        int rate = 0;
        if (count > 0) {
            rate = (int) ((float) correct / (float) count * 100);
            int dot_color = rate < 30 ? R.drawable.timeline_dot_red : R.drawable.timeline_dot_green;
            viewHolder.home_timeline_dot.setBackgroundResource(dot_color);

        }
        viewHolder.home_timeline_statistic.setText(
                Html.fromHtml(String.format(Locale.CHINA,
                        "<font color=#66bb6a>%d</font>｜" +
                                "<font color=#ff7043>%d</font>｜" +
                                "<font color=#66bb6a>%d%%</font>", correct, wrong, rate)));

    }

    @Override
    public int getItemCount() {
        return statisticList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TOP;
        }
        return VIEW_NORMAL;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private View home_timeline_top, home_timeline_dot;
        private TextView home_timeline_date, home_timeline_statistic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            home_timeline_top = itemView.findViewById(R.id.home_timeline_top);
            home_timeline_dot = itemView.findViewById(R.id.home_timeline_dot);
            home_timeline_date = itemView.findViewById(R.id.home_timeline_date);
            home_timeline_statistic = itemView.findViewById(R.id.home_timeline_statistic);
        }
    }
}
