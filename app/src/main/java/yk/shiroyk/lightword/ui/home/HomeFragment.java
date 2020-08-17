package yk.shiroyk.lightword.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.db.entity.UserStatistic;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.UserStatisticRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;
import yk.shiroyk.lightword.ui.adapter.TimeLineAdapter;

public class HomeFragment extends Fragment {

    private final static String TAG = HomeFragment.class.getSimpleName();
    private Context context;
    private ExerciseRepository exerciseRepository;
    private VocabTypeRepository vocabTypeRepository;
    private UserStatisticRepository statisticRepository;
    private CompositeDisposable compositeDisposable;
    private SharedPreferences sp;

    private TextView tv_home_title;
    private TextView tv_home_subtitle;
    private LineChart home_chart;
    private AppCompatButton home_chart_menu;
    private RecyclerView home_timeline;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        statisticRepository = new UserStatisticRepository(getActivity().getApplication());
        context = getContext();
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        compositeDisposable = new CompositeDisposable();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        init(root);

        return root;
    }

    private void init(View root) {
        tv_home_title = root.findViewById(R.id.tv_home_title);
        tv_home_subtitle = root.findViewById(R.id.tv_home_subtitle);
        home_chart = root.findViewById(R.id.home_chart);
        home_chart_menu = root.findViewById(R.id.home_chart_menu);
        home_timeline = root.findViewById(R.id.home_timeline);

        root.findViewById(R.id.btn_start_exercise).setOnClickListener(
                view -> Navigation.findNavController(view).navigate(R.id.action_to_exercise));
        setChartMenu();
    }

    private void setHomeCard() {
        String preValue = sp.getString("vtypeId", "1");

        vocabTypeRepository.getVocabTypeById(Long.valueOf(preValue)).observe(getViewLifecycleOwner(), vocabType -> {
            exerciseRepository.getExerciseProgress(Long.valueOf(preValue)).observe(getViewLifecycleOwner(), integer -> {
                if (vocabType != null) {
                    tv_home_title.setText(vocabType.getVocabtype());
                    tv_home_subtitle.setVisibility(View.VISIBLE);
                    tv_home_subtitle.setText(integer + "/" + vocabType.getAmount());
                } else {
                    tv_home_title.setText("暂无数据");
                    tv_home_subtitle.setVisibility(View.INVISIBLE);
                }

            });
        });
    }

    private LineData setChartData(List<UserStatistic> recentDays) {
        List<Entry> correct = new ArrayList<>();
        List<Entry> wrong = new ArrayList<>();
        List<Entry> count = new ArrayList<>();

        for (UserStatistic day : recentDays
        ) {
            int d = Integer.parseInt(day.getDay());
            correct.add(new Entry(d, day.getCorrect()));
            wrong.add(new Entry(d, day.getWrong()));
            count.add(new Entry(d, day.getCount()));
            Log.d(TAG, "day " + d + "count " + day.getCount() + " correct "
                    + day.getCorrect() + " wrong " + day.getWrong());
        }
        int correctColor = ContextCompat.getColor(context, R.color.correctColor);
        int wrongColor = ContextCompat.getColor(context, R.color.errorColor);
        int countColor = ContextCompat.getColor(context, R.color.colorPrimary);
        LineDataSet setCorrect = new LineDataSet(correct, "Correct");
        setCorrect.setAxisDependency(YAxis.AxisDependency.LEFT);
        setCorrect.setColor(correctColor);
        setCorrect.setCircleColor(correctColor);
        setCorrect.setCircleRadius(4f);
        LineDataSet setWrong = new LineDataSet(wrong, "Wrong");
        setWrong.setAxisDependency(YAxis.AxisDependency.LEFT);
        setWrong.setColor(wrongColor);
        setWrong.setCircleColor(wrongColor);
        setWrong.setCircleRadius(4f);
        LineDataSet setCount = new LineDataSet(count, "Count");
        setCount.setDrawFilled(true);
        setCount.setDrawCircles(false);
        setCount.setColor(countColor);
        setCount.setValueTextColor(countColor);
        setCount.setFillColor(countColor);
        setCount.setFillAlpha(100);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setCorrect);
        dataSets.add(setWrong);
        dataSets.add(setCount);

        LineData lineData = new LineData(dataSets);
        lineData.setValueTextSize(12f);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        lineData.setDrawValues(false);
        return lineData;
    }

    private void showChart(LineData data) {
        int white = Color.parseColor("#FFFFFF");

        home_chart.setData(data);
        home_chart.setDragEnabled(false);
        home_chart.setScaleEnabled(false);
        home_chart.setTouchEnabled(false);
        home_chart.getDescription().setEnabled(false);
        home_chart.animateY(1000);
        home_chart.invalidate();

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String day = String.valueOf((int) value);
                return new StringBuilder(day).
                        insert(day.length() - 2, "/").toString();
            }
        };

        XAxis xAxis = home_chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(white);
        xAxis.setAxisLineColor(white);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(formatter);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis yAxis = home_chart.getAxisLeft();
        yAxis.setTextColor(white);
        yAxis.setGridColor(white);
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        yAxis.setAxisLineColor(white);
        home_chart.getAxisRight().setEnabled(false);
        home_chart.getLegend().setEnabled(false);
    }

    private void setHomeChart(Integer days) {
        Disposable disposable = Observable.create(
                (ObservableOnSubscribe<List<UserStatistic>>)
                        emitter -> emitter.onNext(statisticRepository.getStatistic(days)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(statistics -> statistics.size() > 0)
                .doOnNext(this::setTimeLine)
                .map(this::setChartData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showChart);
        compositeDisposable.add(disposable);
    }

    private void setChartMenu() {
        home_chart_menu.setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(context, home_chart_menu);
            menu.getMenuInflater().inflate(R.menu.home_chart_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.week:
                        setHomeChart(7);
                        return true;
                    case R.id.two_week:
                        setHomeChart(14);
                        return true;
                    case R.id.month:
                        setHomeChart(30);
                        return true;
                    default:
                        return super.onOptionsItemSelected(item);
                }
            });
            menu.show();
        });
    }

    private void setTimeLine(List<UserStatistic> statistics) {
        if (statistics.size() > 0) {
            home_timeline.setVisibility(View.VISIBLE);
            List<UserStatistic> ns = new ArrayList<>(statistics);
            Collections.sort(ns,
                    (s1, s2) -> s2.getTimestamp().compareTo(s1.getTimestamp()));
            TimeLineAdapter adapter = new TimeLineAdapter(context, ns);
            home_timeline.setLayoutManager(new LinearLayoutManager(context));
            home_timeline.setAdapter(adapter);
        } else {
            home_timeline.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setHomeCard();
        setHomeChart(7);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}