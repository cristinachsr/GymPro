package edu.pmdm.gympro.ui.monitores;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityCrearMonitorBinding;
import edu.pmdm.gympro.model.Monitor;


public class CrearMonitorActivity extends AppCompatActivity {

    private ActivityCrearMonitorBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imagenUriSeleccionada;
    private Uri imagenUriCamara;
    private final int REQUEST_CAMERA_PERMISSION = 101;
    private boolean modoEdicion = false;
    private String idMonitorEdicion;

    private final ActivityResultLauncher<Intent> launcherGaleria =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    Glide.with(this).load(imagenUriSeleccionada).into(binding.ivFotoMonitor);
                }
            });

    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imagenUriCamara != null) {
                    imagenUriSeleccionada = imagenUriCamara;
                    Glide.with(this).load(imagenUriCamara).into(binding.ivFotoMonitor);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearMonitorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.countryCodePicker.registerCarrierNumberEditText(binding.etTelefono);

        modoEdicion = getIntent().getBooleanExtra("modoEdicion", false);

        if (modoEdicion) {
            binding.toolbar.setTitle("Editar monitor");
            binding.btnCrearMonitor.setText("Guardar cambios");

            idMonitorEdicion = getIntent().getStringExtra("idMonitor");

            binding.etNombreMonitor.setText(getIntent().getStringExtra("nombre"));
            binding.etApellidosMonitor.setText(getIntent().getStringExtra("apellidos"));
            binding.etDni.setText(getIntent().getStringExtra("dni"));
            binding.etFechaNacimiento.setText(getIntent().getStringExtra("fechaNacimiento"));

            String telefonoCompleto = getIntent().getStringExtra("telefono");
            if (telefonoCompleto != null && telefonoCompleto.startsWith("+")) {
                binding.countryCodePicker.setFullNumber(telefonoCompleto.replace("+", ""));
                String sinPrefijo = telefonoCompleto.replace("+" + binding.countryCodePicker.getSelectedCountryCode(), "");
                binding.etTelefono.setText(sinPrefijo);
            } else {
                binding.etTelefono.setText(telefonoCompleto);
            }

            binding.etCorreo.setText(getIntent().getStringExtra("correo"));

            String foto = getIntent().getStringExtra("foto");
            if (foto != null && !foto.equals("logo_por_defecto")) {
                imagenUriSeleccionada = Uri.parse(foto);
                Glide.with(this).load(imagenUriSeleccionada).into(binding.ivFotoMonitor);
            } else {
                Glide.with(this).load(R.drawable.logo_gympro_sinfondo).into(binding.ivFotoMonitor);
            }
        } else {
            Glide.with(this).load(R.drawable.logo_gympro_sinfondo).into(binding.ivFotoMonitor);
        }

        binding.btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        binding.btnCancelar.setOnClickListener(v -> finish());
        binding.btnCrearMonitor.setOnClickListener(v -> {
            String nombre = binding.etNombreMonitor.getText().toString().trim();
            String apellidos = binding.etApellidosMonitor.getText().toString().trim();
            String dni = binding.etDni.getText().toString().trim();
            String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
            String telefono = binding.countryCodePicker.getFullNumberWithPlus().trim();
            String correo = binding.etCorreo.getText().toString().trim();
            String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";

            if (!validarCampos(nombre, apellidos, dni, fechaNacimiento, telefono, correo)) return;

            if (modoEdicion) {
                editarMonitor();
            } else {
                validarYCrearMonitor(nombre, apellidos, dni, fechaNacimiento, telefono, correo, fotoUrl);
            }
        });
    }

    private void mostrarOpcionesFoto() {
        String[] opciones = {"Galería", "Cámara"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else abrirCamara();
                }).show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            File foto = crearArchivoTemporalImagen();
            if (foto != null) {
                imagenUriCamara = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", foto);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
                launcherCamara.launch(intent);
            }
        }
    }

    private File crearArchivoTemporalImagen() {
        try {
            File storageDir = getExternalFilesDir(null);
            return File.createTempFile("foto_monitor_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void validarYCrearMonitor(String nombre, String apellidos, String dni, String fechaNacimiento,
                                      String telefono, String correo, String fotoUrl) {

        db.collection("monitores").whereEqualTo("dni", dni).get().addOnSuccessListener(snapshotDni -> {
            if (!snapshotDni.isEmpty()) {
                Toast.makeText(this, "Ya existe un monitor con ese DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("monitores").whereEqualTo("telefono", telefono).get().addOnSuccessListener(snapshotTel -> {
                if (!snapshotTel.isEmpty()) {
                    Toast.makeText(this, "Ya existe un monitor con ese número", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("monitores").whereEqualTo("correo", correo).get().addOnSuccessListener(snapshotCorreo -> {
                    if (!snapshotCorreo.isEmpty()) {
                        Toast.makeText(this, "Ya existe un monitor con ese correo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ No existen duplicados: crear monitor
                    String idAdministrador = auth.getCurrentUser().getUid();
                    String idMonitor = UUID.randomUUID().toString();

                    Monitor monitor = new Monitor(idMonitor, nombre, apellidos, dni, fechaNacimiento,
                            telefono, correo, fotoUrl, idAdministrador);

                    db.collection("monitores").document(idMonitor).set(monitor)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Monitor creado correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al guardar monitor", Toast.LENGTH_SHORT).show();
                            });

                });

            });

        });
    }

    private void editarMonitor() {
        String nombre = binding.etNombreMonitor.getText().toString().trim();
        String apellidos = binding.etApellidosMonitor.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String telefono = binding.countryCodePicker.getFullNumberWithPlus().trim();
        String correo = binding.etCorreo.getText().toString().trim();

        if (!validarCampos(nombre, apellidos, dni, fechaNacimiento, telefono, correo)) return;

        String fotoUrl = (imagenUriSeleccionada != null)
                ? imagenUriSeleccionada.toString()
                : "logo_por_defecto";

        String idAdministrador = auth.getCurrentUser().getUid();

        db.collection("monitores").get().addOnSuccessListener(snapshot -> {
            for (var doc : snapshot) {
                if (doc.getId().equals(idMonitorEdicion)) continue;

                if (dni.equalsIgnoreCase(doc.getString("dni"))) {
                    Toast.makeText(this, "Ya existe otro monitor con ese DNI", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (telefono.equals(doc.getString("telefono"))) {
                    Toast.makeText(this, "Ya existe otro monitor con ese número", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (correo.equalsIgnoreCase(doc.getString("correo"))) {
                    Toast.makeText(this, "Ya existe otro monitor con ese correo", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Si pasa todas las comprobaciones
            Monitor monitorActualizado = new Monitor(idMonitorEdicion, nombre, apellidos, dni,
                    fechaNacimiento, telefono, correo, fotoUrl, idAdministrador);

            db.collection("monitores").document(idMonitorEdicion).set(monitorActualizado)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Monitor actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        });
    }


    private boolean validarCampos(String nombre, String apellidos, String dni, String fechaNacimiento, String telefono, String correo) {
        if (nombre.trim().isEmpty() || apellidos.trim().isEmpty() || dni.trim().isEmpty() ||
                fechaNacimiento.trim().isEmpty() || telefono.trim().isEmpty() || correo.trim().isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Toast.makeText(this, "El nombre solo puede contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Toast.makeText(this, "Los apellidos solo pueden contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!dni.matches("\\d{8}[A-Za-z]")) {
            Toast.makeText(this, "DNI inválido (ejemplo: 12345678A)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!fechaNacimiento.matches("^\\d{2}/\\d{2}/\\d{4}$") || !fechaValida(fechaNacimiento)) {
            Toast.makeText(this, "Fecha inválida. Formato requerido: dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!binding.countryCodePicker.isValidFullNumber()) {
            Toast.makeText(this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean fechaValida(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            sdf.parse(fecha);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
