package edu.pmdm.gympro.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import edu.pmdm.gympro.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {



        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Navegaciones simples a actividades (debes crearlas)
        binding.btnClientes.setOnClickListener(v -> {
            // startActivity(new Intent(getContext(), ClientesActivity.class));
        });

        binding.btnGrupos.setOnClickListener(v -> {
            // startActivity(new Intent(getContext(), GruposActivity.class));
        });

        binding.btnMonitores.setOnClickListener(v -> {
            // startActivity(new Intent(getContext(), MonitoresActivity.class));
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
