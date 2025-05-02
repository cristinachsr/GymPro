package edu.pmdm.gympro.ui.monitores;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.databinding.FragmentMonitoresBinding;
import edu.pmdm.gympro.model.Monitor;

public class MonitoresFragment extends Fragment {

    private FragmentMonitoresBinding binding;
    private MonitorAdapter monitorAdapter;
    private FirebaseFirestore db;
    private List<Monitor> listaMonitores = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMonitoresBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        cargarMonitores();

        binding.btnCrearMonitor.setOnClickListener(v -> {
            // Aquí iría la actividad para crear un monitor
            startActivity(new Intent(requireContext(), CrearMonitorActivity.class));
        });

        binding.etBuscarMonitor.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                monitorAdapter.filtrar(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private void setupRecyclerView() {
        monitorAdapter = new MonitorAdapter(listaMonitores, monitor -> {
            Intent intent = new Intent(requireContext(), DetalleMonitorActivity.class);
            intent.putExtra("idMonitor", monitor.getIdMonitor());
            intent.putExtra("nombre", monitor.getNombre());
            intent.putExtra("apellidos", monitor.getApellidos());
            intent.putExtra("dni", monitor.getDni());
            intent.putExtra("fechaNacimiento", monitor.getFechaNacimiento());
            intent.putExtra("telefono", monitor.getTelefono());
            intent.putExtra("correo", monitor.getCorreo());
            intent.putExtra("foto", monitor.getFoto());
            startActivity(intent);
        });

        binding.rvMonitores.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMonitores.setAdapter(monitorAdapter);
        //binding.rvMonitores.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        //binding.rvMonitores.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.rvMonitores.addItemDecoration(new SpaceItemDecoration(24));
    }


    private void cargarMonitores() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Obtener UID del administrador

        Log.d("FIRESTORE_MONITORES", "UID actual: " + uid);

        db.collection("monitores")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FIRESTORE_MONITORES", "Documentos obtenidos: " + querySnapshot.size());

                    List<Monitor> monitoresCargados = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Monitor monitor = doc.toObject(Monitor.class);
                        monitoresCargados.add(monitor);
                        Log.d("FIRESTORE_MONITORES", "Nombre: " + monitor.getNombre() + ", Apellidos: " + monitor.getApellidos());
                    }

                    monitorAdapter.actualizarLista(monitoresCargados); // ← CORRECTO

                    if (monitoresCargados.isEmpty()) {
                        Toast.makeText(getContext(), "No hay monitores disponibles", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_MONITORES", "Error al cargar monitores: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar monitores", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMonitores(); // Vuelve a cargar la lista cuando regresas al fragmento
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
