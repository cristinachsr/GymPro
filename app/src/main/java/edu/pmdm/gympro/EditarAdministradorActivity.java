package edu.pmdm.gympro;

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

import edu.pmdm.gympro.databinding.ActivityEditarAdministradorBinding;

public class EditarAdministradorActivity extends AppCompatActivity {

    private ActivityEditarAdministradorBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;
    private Uri imagenUriSeleccionada;
    private Uri imagenUriCamara;

    private final int REQUEST_CAMERA_PERMISSION = 123;

    private final ActivityResultLauncher<Intent> launcherGaleria =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    Glide.with(this).load(imagenUriSeleccionada).into(binding.ivFotoAdmin);
                }
            });

    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imagenUriCamara != null) {
                    imagenUriSeleccionada = imagenUriCamara;
                    Glide.with(this).load(imagenUriCamara).into(binding.ivFotoAdmin);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditarAdministradorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();

        cargarDatosAdministrador();

        binding.btnGuardarAdmin.setOnClickListener(v -> guardarCambios());
        binding.btnCancelarAdmin.setOnClickListener(v -> finish());
        binding.btnSeleccionarFotoAdmin.setOnClickListener(v -> mostrarOpcionesFoto());
    }

    private void cargarDatosAdministrador() {
        db.collection("administradores").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        binding.etNombreAdmin.setText(document.getString("nombreAdministrador"));
                        binding.etApellidosAdmin.setText(document.getString("apellidoAdministrador"));
                        binding.etDniAdmin.setText(document.getString("dni"));
                        binding.etFechaAdmin.setText(document.getString("fechaNacimiento"));
                        binding.etCorreoAdmin.setText(document.getString("email"));

                        String telefonoCompleto = document.getString("telefono");
                        if (telefonoCompleto != null && telefonoCompleto.startsWith("+")) {
                            binding.countryCodePickerAdmin.registerCarrierNumberEditText(binding.etTelefonoAdmin);
                            binding.countryCodePickerAdmin.setFullNumber(telefonoCompleto.replace("+", ""));
                            String sinPrefijo = telefonoCompleto.replace("+" + binding.countryCodePickerAdmin.getSelectedCountryCode(), "");
                            binding.etTelefonoAdmin.setText(sinPrefijo);
                        }

                        String foto = document.getString("photo");
                        if (foto != null && !foto.trim().isEmpty() && !foto.equals("logo_por_defecto")) {
                            Glide.with(this).load(Uri.parse(foto)).into(binding.ivFotoAdmin);
                        } else {
                            binding.ivFotoAdmin.setImageResource(R.drawable.usuario_sinfondo); // asegúrate que existe
                        }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            File foto = crearArchivoTemporalImagen();
            if (foto != null) {
                imagenUriCamara = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
                launcherCamara.launch(intent);
            }
        }
    }

    private File crearArchivoTemporalImagen() {
        try {
            File storageDir = getExternalFilesDir(null);
            return File.createTempFile("foto_admin_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void guardarCambios() {
        String nombre = binding.etNombreAdmin.getText().toString().trim();
        String apellidos = binding.etApellidosAdmin.getText().toString().trim();
        String dni = binding.etDniAdmin.getText().toString().trim();
        String fecha = binding.etFechaAdmin.getText().toString().trim();
        String telefono = binding.countryCodePickerAdmin.getFullNumberWithPlus().trim();
        String nuevaFoto = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : null;

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || fecha.isEmpty() || !binding.countryCodePickerAdmin.isValidFullNumber()) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar duplicados en otros administradores
        db.collection("administradores")
                .whereNotEqualTo("email", auth.getCurrentUser().getEmail()) // ignorar el actual
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean dniDuplicado = false;
                    boolean telDuplicado = false;

                    for (var doc : snapshot) {
                        if (dni.equals(doc.getString("dni"))) dniDuplicado = true;
                        if (telefono.equals(doc.getString("telefono"))) telDuplicado = true;
                    }

                    if (dniDuplicado) {
                        Toast.makeText(this, "Ya existe un administrador con ese DNI", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (telDuplicado) {
                        Toast.makeText(this, "Ya existe un administrador con ese teléfono", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Si no hay duplicados, actualizar
                    db.collection("administradores").document(uid).update(
                            "nombreAdministrador", nombre,
                            "apellidoAdministrador", apellidos,
                            "dni", dni,
                            "fechaNacimiento", fecha,
                            "telefono", telefono,
                            "photo", (nuevaFoto != null ? nuevaFoto : "logo_por_defecto")
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK); // ✅ Notificar que hubo cambios
                        finish();             // ✅ Volver al MainActivity
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    );
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
