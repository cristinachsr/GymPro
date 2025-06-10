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
import edu.pmdm.gympro.CryptoUtils;

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
        binding.rvMonitores.addItemDecoration(new SpaceItemDecoration(24));
    }


    private void cargarMonitores() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("monitores")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Monitor> monitoresCargados = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Monitor monitor = doc.toObject(Monitor.class);

                        try {
                            monitor.setDni(CryptoUtils.decrypt(monitor.getDni()));
                            monitor.setCorreo(CryptoUtils.decrypt(monitor.getCorreo()));
                            monitor.setTelefono(CryptoUtils.decrypt(monitor.getTelefono()));
                            monitor.setFechaNacimiento(CryptoUtils.decrypt(monitor.getFechaNacimiento()));
                        } catch (Exception e) {
                            continue;
                        }

                        monitoresCargados.add(monitor);
                    }

                    monitorAdapter.actualizarLista(monitoresCargados);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar monitores", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        cargarMonitores();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
