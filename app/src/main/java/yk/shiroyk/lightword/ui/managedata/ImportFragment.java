package yk.shiroyk.lightword.ui.managedata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import yk.shiroyk.lightword.R;
import yk.shiroyk.lightword.ui.adapter.ImportDataPagerAdapter;

public class ImportFragment extends Fragment {

    private String[] titles = new String[]{"词库数据管理", "词汇数据管理"};
    private ArrayList<Fragment> fragments = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_import_data, container, false);
        init(root);
        return root;
    }

    public void init(View root) {

        TabLayout import_tabs = root.findViewById(R.id.import_tabs);
        ViewPager import_viewpager = root.findViewById(R.id.import_viewpager);

        ImportVocabFragment importVocabFragment = new ImportVocabFragment();
        ImportVdataFragment importVdataFragment = new ImportVdataFragment();

        fragments.add(importVocabFragment);
        fragments.add(importVdataFragment);

        import_tabs.setupWithViewPager(import_viewpager, false);

        ImportDataPagerAdapter pagerAdapter = new ImportDataPagerAdapter(getChildFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                fragments,
                titles);
        import_viewpager.setAdapter(pagerAdapter);


    }

}
