package edu.pmdm.gympro.ui.monitores;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityDetalleMonitorBinding;

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
        String dni = getIntent().getStringExtra("dni");
        String fecha = getIntent().getStringExtra("fechaNacimiento");
        String telefono = getIntent().getStringExtra("telefono");
        String correo = getIntent().getStringExtra("correo");
        String foto = getIntent().getStringExtra("foto");

        binding.tvNombreCompleto.setText(nombre + " " + apellidos);
        binding.tvDni.setText("DNI: " + dni);
        binding.tvFechaNacimiento.setText("Fecha de nacimiento: " + fecha);
        binding.tvTelefono.setText("Teléfono: " + telefono);
        binding.tvCorreo.setText("Correo: " + correo);

        if (foto != null && !foto.equals("logo_por_defecto")) {
            Glide.with(this).load(foto).into(binding.ivFotoMonitor);
        } else {
            binding.ivFotoMonitor.setImageResource(R.drawable.logo_gympro_sinfondo);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
