package edu.pmdm.gympro.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.FragmentInicioBinding;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ✅ Usa navegación entre fragments correctamente con Navigation Component
        binding.btnClientes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_clientes));

        binding.btnGrupos.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_grupos));

        binding.btnMonitores.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_monitores));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
