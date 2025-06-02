package edu.pmdm.gympro.ui.grupos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.databinding.ActivityDetalleGrupoBinding;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.model.Grupo;
import edu.pmdm.gympro.model.Horario;
import edu.pmdm.gympro.ui.clientes.ClienteGrupoAdapter;

public class DetalleGrupoActivity extends AppCompatActivity {

    private ActivityDetalleGrupoBinding binding;
    private String idGrupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        idGrupo = getIntent().getStringExtra("id_grupo");
        cargarDatosGrupoDesdeFirestore();

        binding.btnEditarGrupo.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("grupos")
                    .document(idGrupo)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Grupo grupo = documentSnapshot.toObject(Grupo.class);
                            if (grupo == null) return;

                            String resumenHorarios = generarResumenHorarios(grupo.getHorarios());

                            Intent intent = new Intent(this, CrearGrupoActivity.class);
                            intent.putExtra("modo", "editar");
                            intent.putExtra("id_grupo", grupo.getIdgrupo());
                            intent.putExtra("nombre", grupo.getNombre());
                            intent.putExtra("descripcion", grupo.getDescripcion());
                            intent.putExtra("foto", grupo.getFoto());
                            intent.putExtra("idMonitor", grupo.getIdMonitor());
                            intent.putExtra("resumenHorarios", resumenHorarios);
                            editarGrupoLauncher.launch(intent);
                        }
                    });
        });

        binding.btnEliminarGrupo.setOnClickListener(v -> {
            String nombreGrupo = binding.etNombreGrupo.getText().toString();
            confirmarEliminacion(nombreGrupo);
        });

        binding.btnVerClientes.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaClientesGrupoActivity.class);
            intent.putExtra("id_grupo", idGrupo);
            startActivity(intent);
        });
    }

    private void cargarDatosGrupoDesdeFirestore() {
        FirebaseFirestore.getInstance()
                .collection("grupos")
                .document(idGrupo)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Grupo grupo = documentSnapshot.toObject(Grupo.class);
                        if (grupo == null) return;

                        binding.etNombreGrupo.setText(grupo.getNombre());
                        binding.etDescripcionGrupo.setText(grupo.getDescripcion());
                        if (grupo.getIdMonitor() == null || grupo.getIdMonitor().isEmpty()) {
                            binding.etMonitorGrupo.setText("Sin monitor asignado");
                        } else {
                            FirebaseFirestore.getInstance()
                                    .collection("monitores")
                                    .whereEqualTo("idMonitor", grupo.getIdMonitor())
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot monitorDoc = querySnapshot.getDocuments().get(0);
                                            String nombre = monitorDoc.getString("nombre");
                                            String apellidos = monitorDoc.getString("apellidos");
                                            binding.etMonitorGrupo.setText(nombre + " " + apellidos);
                                        } else {
                                            binding.etMonitorGrupo.setText("Sin monitor asignado");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        binding.etMonitorGrupo.setText("Error al cargar monitor");
                                    });
                        }

                        if (grupo.getFoto() != null && !grupo.getFoto().isEmpty()
                                && !grupo.getFoto().equals("logo_por_defecto")) {
                            Glide.with(this).load(grupo.getFoto()).into(binding.ivFotoGrupo);
                        } else {
                            binding.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
                        }

                        String resumenHorarios = generarResumenHorarios(grupo.getHorarios());
                        TextView tvHorarios = findViewById(R.id.tvHorariosGrupo);
                        tvHorarios.setText(resumenHorarios.isEmpty() ? "Sin horarios asignados" : resumenHorarios);
                    }
                });
    }

    private String generarResumenHorarios(List<Horario> horarios) {
        if (horarios == null || horarios.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Horario h : horarios) {
            sb.append(h.getDia()).append(" ").append(h.getHoraInicio()).append(" - ").append(h.getHoraFin()).append("\n");
        }
        return sb.toString().trim();
    }

    private void confirmarEliminacion(String nombreGrupo) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar grupo")
                .setMessage("¿Estás seguro de que quieres eliminar este grupo?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarGrupoPorNombre(nombreGrupo))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarGrupoPorNombre(String nombreGrupo) {
        FirebaseFirestore.getInstance()
                .collection("grupos")
                .whereEqualTo("nombre", nombreGrupo)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        query.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Grupo eliminado", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "No se encontró el grupo para eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al buscar grupo", Toast.LENGTH_SHORT).show());
    }

    private final ActivityResultLauncher<Intent> editarGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    cargarDatosGrupoDesdeFirestore();
                }
            });

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }
}