package edu.pmdm.gympro.ui.monitores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import edu.pmdm.gympro.databinding.FragmentMonitoresBinding;

public class MonitoresFragment extends Fragment {

    private FragmentMonitoresBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MonitoresViewModel galleryViewModel =
                new ViewModelProvider(this).get(MonitoresViewModel.class);

        binding = FragmentMonitoresBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGMonitores;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}