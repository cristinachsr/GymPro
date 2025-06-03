package edu.pmdm.gympro.ui.monitores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityDetalleMonitorBinding;
import edu.pmdm.gympro.CryptoUtils;

public class DetalleMonitorActivity extends AppCompatActivity {

    private ActivityDetalleMonitorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetalleMonitorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtener datos del intent
        String nombre = getIntent().getStringExtra("nombre");
        String apellidos = getIntent().getStringExtra("apellidos");
        String dni = CryptoUtils.decrypt(getIntent().getStringExtra("dni"));
        String fecha = CryptoUtils.decrypt(getIntent().getStringExtra("fechaNacimiento"));
        String telefono = CryptoUtils.decrypt(getIntent().getStringExtra("telefono"));
        String correo = CryptoUtils.decrypt(getIntent().getStringExtra("correo"));
        String foto = getIntent().getStringExtra("foto");

        // Mostrar datos en los EditText deshabilitados
        binding.etNombre.setText(nombre);
        binding.etApellidos.setText(apellidos);
        binding.etDni.setText(dni);
        binding.etFechaNacimiento.setText(fecha);
        binding.etTelefono.setText(telefono);
        binding.etCorreo.setText(correo);

        if (foto != null && !foto.equals("logo_por_defecto") && !foto.isEmpty()) {
            Glide.with(this)
                    .load(foto)
                    .placeholder(R.drawable.logo_gympro_sinfondo) // mientras carga
                    .error(R.drawable.logo_gympro_sinfondo)       // si falla
                    .into(binding.ivFotoMonitor);
        } else {
            binding.ivFotoMonitor.setImageResource(R.drawable.logo_gympro_sinfondo);
        }

        binding.btnEliminarMonitor.setOnClickListener(v -> {
            String idMonitor = getIntent().getStringExtra("idMonitor");

            if (idMonitor != null && !idMonitor.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("monitores")
                        .document(idMonitor)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Monitor eliminado", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al eliminar monitor", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "No se pudo obtener el ID del monitor", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEditarMonitor.setOnClickListener(v -> {
            Intent intent = new Intent(this, CrearMonitorActivity.class);
            intent.putExtra("modoEdicion", true);
            intent.putExtra("idMonitor", getIntent().getStringExtra("idMonitor")); // si lo estás enviando desde el adapter
            intent.putExtra("nombre", binding.etNombre.getText().toString());
            intent.putExtra("apellidos", binding.etApellidos.getText().toString());
            intent.putExtra("dni", binding.etDni.getText().toString());
            intent.putExtra("fechaNacimiento", binding.etFechaNacimiento.getText().toString());
            intent.putExtra("telefono", binding.etTelefono.getText().toString());
            intent.putExtra("correo", binding.etCorreo.getText().toString());
            intent.putExtra("foto", getIntent().getStringExtra("foto")); // pasar la URI original
            startActivity(intent);
            finish(); // opcional: para que no vuelva aquí al presionar atrás
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
