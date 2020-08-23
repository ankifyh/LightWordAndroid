package yk.shiroyk.lightword.ui.managedata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import yk.shiroyk.lightword.R;

public class ManageDataFragment extends Fragment {

    private MaterialButton btn_import_data;
    private MaterialButton btn_master_word;
    private MaterialButton btn_create_vocab;
    private MaterialButton btn_create_vdata;
    private MaterialButton btn_data_clone;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manage_data, container, false);
        init(root);
        return root;
    }

    private void init(View root) {
        btn_import_data = root.findViewById(R.id.btn_import_data);
        btn_master_word = root.findViewById(R.id.btn_master_word);
        btn_create_vocab = root.findViewById(R.id.btn_create_vocab);
        btn_create_vdata = root.findViewById(R.id.btn_create_vdata);
        btn_data_clone = root.findViewById(R.id.btn_data_clone);

        btn_import_data.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(R.id.action_to_import));
        btn_master_word.setOnClickListener(view ->
                Navigation.findNavController(view).navigate(R.id.action_to_master_word));
    }
}