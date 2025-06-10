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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.pmdm.gympro.databinding.ActivityEditarAdministradorBinding;
import edu.pmdm.gympro.ui.auth.LoginActivity;

public class EditarAdministradorActivity extends AppCompatActivity {

    private ActivityEditarAdministradorBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;
    private Uri imagenUriSeleccionada;
    private Uri imagenUriCamara;
    private String fotoActual = null;

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
        binding.btnEliminarAdmin.setOnClickListener(v -> confirmarEliminacion());

    }

    private void cargarDatosAdministrador() {
        db.collection("administradores").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        binding.etNombreAdmin.setText(document.getString("nombreAdministrador"));
                        binding.etApellidosAdmin.setText(document.getString("apellidoAdministrador"));
                        binding.etDniAdmin.setText(tryDecrypt(document.getString("dni")));
                        binding.etFechaAdmin.setText(tryDecrypt(document.getString("fechaNacimiento")));
                        binding.etCorreoAdmin.setText(tryDecrypt(document.getString("correo")));

                        String telefonoCompleto = tryDecrypt(document.getString("telefono"));
                        if (telefonoCompleto != null && telefonoCompleto.startsWith("+")) {
                            binding.countryCodePickerAdmin.registerCarrierNumberEditText(binding.etTelefonoAdmin);
                            binding.countryCodePickerAdmin.setFullNumber(telefonoCompleto.replace("+", ""));
                            String sinPrefijo = telefonoCompleto.replace("+" + binding.countryCodePickerAdmin.getSelectedCountryCode(), "");
                            binding.etTelefonoAdmin.setText(sinPrefijo);
                        }

                        String foto = document.getString("foto");
                        fotoActual = foto;
                        if (foto != null && !foto.trim().isEmpty() && !foto.equals("logo_por_defecto")) {
                            Glide.with(this).load(Uri.parse(foto)).into(binding.ivFotoAdmin);
                        } else {
                            binding.ivFotoAdmin.setImageResource(R.drawable.usuario_sinfondo);
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

    private String tryDecrypt(String valor) {
        try {
            return CryptoUtils.decrypt(valor);
        } catch (Exception e) {
            return valor;
        }
    }

    private void guardarCambios() {
        String nombre = binding.etNombreAdmin.getText().toString().trim();
        String apellidos = binding.etApellidosAdmin.getText().toString().trim();
        String dni = binding.etDniAdmin.getText().toString().trim();
        String fecha = binding.etFechaAdmin.getText().toString().trim();
        String telefono = binding.countryCodePickerAdmin.getFullNumberWithPlus().trim();
        String nuevaFoto = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : fotoActual;

        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || fecha.isEmpty() || !binding.countryCodePickerAdmin.isValidFullNumber()) {
            Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fechaValida(fecha)) {
            Toast.makeText(this, "La fecha debe tener formato dd/MM/yyyy y ser válida", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dni.matches("\\d{8}[A-Z]")) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos seguidos de una letra en mayúscula", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("administradores")
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean dniDuplicado = false;
                    boolean telDuplicado = false;

                    for (var doc : snapshot) {
                        if (doc.getId().equals(uid)) continue;

                        String dniCifrado = CryptoUtils.encrypt(dni);
                        String telefonoCifrado = CryptoUtils.encrypt(telefono);

                        if (dniCifrado.equals(doc.getString("dni"))) dniDuplicado = true;
                        if (telefonoCifrado.equals(doc.getString("telefono"))) telDuplicado = true;
                    }

                    if (dniDuplicado) {
                        Toast.makeText(this, "Ya existe un administrador con ese DNI", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (telDuplicado) {
                        Toast.makeText(this, "Ya existe un administrador con ese teléfono", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("administradores").document(uid).update(
                            "nombreAdministrador", nombre,
                            "apellidoAdministrador", apellidos,
                            "dni", CryptoUtils.encrypt(dni),
                            "fechaNacimiento", CryptoUtils.encrypt(fecha),
                            "telefono", CryptoUtils.encrypt(telefono),
                            "correo", CryptoUtils.encrypt(auth.getCurrentUser().getEmail()),
                            "foto", (nuevaFoto != null ? nuevaFoto : "logo_por_defecto")
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    );
                });
    }

    private boolean fechaValida(String fecha) {
        if (!fecha.matches("^\\d{2}/\\d{2}/\\d{4}$")) return false;

        String[] partes = fecha.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int año = Integer.parseInt(partes[2]);

        if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || año < 1900) return false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void eliminarAdministrador() {
        String idAdmin = uid;

        String[] coleccionesRelacionadas = {"clientes", "grupos", "monitores", "pagos"};

        for (String coleccion : coleccionesRelacionadas) {
            db.collection(coleccion)
                    .whereEqualTo("idAdministrador", idAdmin)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (var doc : snapshot.getDocuments()) {
                            db.collection(coleccion).document(doc.getId()).delete();
                        }
                    });
        }

        db.collection("administradores").document(idAdmin).delete()
                .addOnSuccessListener(aVoid -> {
                    auth.getCurrentUser().delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Administrador eliminado por completo", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Datos eliminados, pero no se pudo eliminar la cuenta: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                auth.signOut();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar datos del administrador", Toast.LENGTH_SHORT).show()
                );
    }


    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar administrador")
                .setMessage("¿Seguro que quieres eliminar tu cuenta? Se borrarán todos tus datos del sistema.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarAdministrador())
                .setNegativeButton("Cancelar", null)
                .show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
