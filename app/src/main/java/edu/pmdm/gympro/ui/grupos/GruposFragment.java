package edu.pmdm.gympro.ui.grupos;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.databinding.FragmentGruposBinding;
import edu.pmdm.gympro.model.Grupo;

public class GruposFragment extends Fragment {

    private FragmentGruposBinding binding;
    private GrupoAdapter grupoAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private final List<Grupo> listaGrupos = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGruposBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        cargarGruposDelAdministrador();

        // Botón para crear nuevo grupo
        binding.btnCrearGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CrearGrupoActivity.class);
            crearGrupoLauncher.launch(intent);
        });

        // Búsqueda por nombre
        binding.etBuscarGrupo.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                grupoAdapter.filtrar(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private void setupRecyclerView() {
        grupoAdapter = new GrupoAdapter(listaGrupos, grupo -> {
            Intent intent = new Intent(requireContext(), DetalleGrupoActivity.class);
            intent.putExtra("id_grupo", grupo.getIdgrupo());
            intent.putExtra("nombre", grupo.getNombre());
            intent.putExtra("descripcion", grupo.getDescripcion());
            intent.putExtra("foto", grupo.getFoto());
            intent.putExtra("idMonitor", grupo.getIdMonitor());
            detalleGrupoLauncher.launch(intent);
        });

        binding.rvGrupos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGrupos.setAdapter(grupoAdapter);
        binding.rvGrupos.addItemDecoration(new SpaceItemDecoration(24));
    }

    private void cargarGruposDelAdministrador() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("grupos")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Grupo> nuevosGrupos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Grupo grupo = doc.toObject(Grupo.class);
                        nuevosGrupos.add(grupo);
                    }
                    grupoAdapter.actualizarLista(nuevosGrupos);
                });
    }

    private final ActivityResultLauncher<Intent> crearGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    cargarGruposDelAdministrador();
                }
            });

    private final ActivityResultLauncher<Intent> detalleGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    cargarGruposDelAdministrador();
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
