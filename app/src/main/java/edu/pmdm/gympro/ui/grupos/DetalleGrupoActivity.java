package edu.pmdm.gympro.ui.grupos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import edu.pmdm.gympro.ui.clientes.ClienteGrupoAdapter;

public class DetalleGrupoActivity extends AppCompatActivity {

    private ActivityDetalleGrupoBinding binding;
    private String idGrupo; // Lo puedes pasar por intent si lo necesitas para editar/eliminar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtener datos del intent
        String nombre = getIntent().getStringExtra("nombre");
        String descripcion = getIntent().getStringExtra("descripcion");
        String foto = getIntent().getStringExtra("foto");
        String monitor = getIntent().getStringExtra("id_empleado");
        idGrupo = getIntent().getStringExtra("id_grupo");

        // Mostrar datos
        binding.etNombreGrupo.setText(nombre);
        binding.etDescripcionGrupo.setText(descripcion);
        binding.etMonitorGrupo.setText((monitor == null || monitor.isEmpty()) ? "Sin monitor asignado" : monitor);

        if (foto != null && !foto.isEmpty() && !foto.equals("logo_por_defecto")) {
            Glide.with(this).load(foto).into(binding.ivFotoGrupo);
        } else {
            binding.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
        }

        // Botón Editar (pendiente de implementar)
        binding.btnEditarGrupo.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearGrupoActivity.class);
            intent.putExtra("modo", "editar");
            intent.putExtra("id_grupo", idGrupo);
            intent.putExtra("nombre", binding.etNombreGrupo.getText().toString());
            intent.putExtra("descripcion", binding.etDescripcionGrupo.getText().toString());
            intent.putExtra("foto", getIntent().getStringExtra("foto"));
            intent.putExtra("monitor", binding.etMonitorGrupo.getText().toString());
            editarGrupoLauncher.launch(intent);

        });

        // Botón Eliminar
        binding.btnEliminarGrupo.setOnClickListener(v -> {
            String nombreGrupo = binding.etNombreGrupo.getText().toString();
            confirmarEliminacion(nombreGrupo);
        });

        RecyclerView recyclerClientesGrupo = findViewById(R.id.recyclerClientesGrupo);
        List<Cliente> listaClientesGrupo = new ArrayList<>();
        ClienteGrupoAdapter adapter = new ClienteGrupoAdapter(this, listaClientesGrupo);

        recyclerClientesGrupo.setLayoutManager(new LinearLayoutManager(this));
        recyclerClientesGrupo.addItemDecoration(new SpaceItemDecoration(24));
        recyclerClientesGrupo.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String grupoId = getIntent().getStringExtra("id_grupo");

        db.collection("clientes")
                .whereArrayContains("clasesSeleccionadas", grupoId)
                .get()
                .addOnSuccessListener(query -> {
                    listaClientesGrupo.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Cliente cliente = doc.toObject(Cliente.class);
                        listaClientesGrupo.add(cliente);
                    }
                    adapter.notifyDataSetChanged();
                });
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
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar grupo", Toast.LENGTH_SHORT).show();
                });
    }


    private final ActivityResultLauncher<Intent> editarGrupoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    setResult(RESULT_OK);
                    recargarGrupoDesdeFirestore(); // 👈 Volvemos a consultar los datos actualizados
                }
            });

    private void recargarGrupoDesdeFirestore() {
        FirebaseFirestore.getInstance()
                .collection("grupos")
                .document(idGrupo)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String foto = documentSnapshot.getString("photo");
                        String monitor = documentSnapshot.getString("id_empleado");

                        binding.etNombreGrupo.setText(nombre);
                        binding.etDescripcionGrupo.setText(descripcion);
                        binding.etMonitorGrupo.setText((monitor == null || monitor.isEmpty()) ? "Sin monitor asignado" : monitor);

                        if (foto != null && !foto.isEmpty() && !foto.equals("logo_por_defecto")) {
                            Glide.with(this).load(foto).into(binding.ivFotoGrupo);
                        } else {
                            binding.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK); // Notifica al fragmento que hubo cambios
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }
}
