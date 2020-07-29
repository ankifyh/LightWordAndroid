package yk.shiroyk.lightword.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.repository.ExerciseRepository;
import yk.shiroyk.lightword.repository.VocabTypeRepository;

public class HomeFragment extends Fragment {

    private ExerciseRepository exerciseRepository;
    private VocabTypeRepository vocabTypeRepository;
    private SharedPreferences sp;

    private TextView tv_home_title;
    private TextView tv_home_subtitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseRepository = new ExerciseRepository(getActivity().getApplication());
        vocabTypeRepository = new VocabTypeRepository(getActivity().getApplication());
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
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

        setHomeCard();
        root.findViewById(R.id.btn_start_exercise).setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_to_exercise));
    }

    public void setHomeCard() {
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

    @Override
    public void onResume() {
        super.onResume();
        setHomeCard();
    }
}