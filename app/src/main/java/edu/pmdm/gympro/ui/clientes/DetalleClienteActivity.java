package edu.pmdm.gympro.ui.clientes;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import edu.pmdm.gympro.databinding.ActivityDetalleClienteBinding;
import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Cliente;

public class DetalleClienteActivity extends AppCompatActivity {

    private ActivityDetalleClienteBinding binding;
    private String idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtener datos del intent
        String nombre = getIntent().getStringExtra("nombre");
        String apellidos = getIntent().getStringExtra("apellidos");
        String dni = getIntent().getStringExtra("dni");
        String fecha = getIntent().getStringExtra("fechaNacimiento");
        String telefono = getIntent().getStringExtra("telefono");
        String correo = getIntent().getStringExtra("correo");
        String foto = getIntent().getStringExtra("foto");
        idCliente = getIntent().getStringExtra("idCliente");

        // Mostrar datos
        binding.etNombre.setText(nombre);
        binding.etApellidos.setText(apellidos);
        binding.etDni.setText(dni);
        binding.etFechaNacimiento.setText(fecha);
        binding.etTelefono.setText(telefono);
        binding.etCorreo.setText(correo);

        if (foto != null && !foto.isEmpty() && !foto.equals("logo_por_defecto")) {
            Glide.with(this).load(foto).into(binding.ivFotoCliente);
        } else {
            binding.ivFotoCliente.setImageResource(R.drawable.logo_gympro_sinfondo);
        }

        binding.btnEditarCliente.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("clientes")
                    .document(idCliente)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> clasesSeleccionadas = (ArrayList<String>) documentSnapshot.get("clasesSeleccionadas");

                            Intent intent = new Intent(this, CrearClienteActivity.class);
                            intent.putExtra("modoEdicion", true);
                            intent.putExtra("idCliente", idCliente);
                            intent.putExtra("nombre", nombre);
                            intent.putExtra("apellidos", apellidos);
                            intent.putExtra("dni", dni);
                            intent.putExtra("fechaNacimiento", fecha);
                            intent.putExtra("telefono", telefono);
                            intent.putExtra("correo", correo);
                            intent.putExtra("foto", foto);
                            intent.putStringArrayListExtra("clasesSeleccionadas", clasesSeleccionadas);

                            startActivity(intent);
                            finish();
                        }
                    });
        });

        binding.btnEliminarCliente.setOnClickListener(v -> confirmarEliminacion(nombre));

        List<String> idsClases = getIntent().getStringArrayListExtra("clasesSeleccionadas");

        if (idsClases != null && !idsClases.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("grupos")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<String> nombresClases = new ArrayList<>();
                        for (var doc : snapshot) {
                            if (idsClases.contains(doc.getId())) {
                                String nombreGrupo = doc.getString("nombre");
                                if (nombreGrupo != null) {
                                    nombresClases.add(nombreGrupo);
                                }
                            }
                        }

                        if (!nombresClases.isEmpty()) {
                            binding.tvClasesCliente.setText(String.join(", ", nombresClases));
                        } else {
                            binding.tvClasesCliente.setText("Sin clases asignadas");
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.tvClasesCliente.setText("Error al cargar clases");
                    });
        } else {
            binding.tvClasesCliente.setText("Sin clases asignadas");
        }
    }

    private void confirmarEliminacion(String nombreCliente) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cliente")
                .setMessage("¿Estás seguro de que quieres eliminar a " + nombreCliente + "?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCliente())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCliente() {
        FirebaseFirestore.getInstance()
                .collection("clientes")
                .document(idCliente)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
