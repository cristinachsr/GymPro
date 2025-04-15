package edu.pmdm.gympro.ui.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityRegisterBinding;
import edu.pmdm.gympro.model.Administrador;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private Uri fotoSeleccionadaUri;
    private Uri fotoCamaraUri;

    private final int REQUEST_CAMERA_PERMISSION = 100;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    fotoSeleccionadaUri = result.getData().getData();
                    binding.ivProfilePic.setImageURI(fotoSeleccionadaUri);
                }
            });

    private final ActivityResultLauncher<Intent> tomarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && fotoCamaraUri != null) {
                    binding.ivProfilePic.setImageURI(fotoCamaraUri);
                    fotoSeleccionadaUri = fotoCamaraUri;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnRegistrarse.setOnClickListener(v -> registrarAdministrador());
        binding.btnGoToLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        // Toggle contraseña
        ImageView ivToggle = binding.ivTogglePassword;
        EditText etPassword = binding.etPassword;
        ivToggle.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_visibility_sinfondo);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_visibility_off_sinfondo);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Selección de foto
        binding.btnSeleccionarFoto.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        binding.btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
    }

    private void mostrarOpcionesFoto() {
        String[] opciones = {"Galería", "Cámara"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar foto de perfil")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else abrirCamara();
                }).show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionarImagenLauncher.launch(intent);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            File foto = crearArchivoTemporalImagen();
            if (foto != null) {
                fotoCamaraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoCamaraUri);
                tomarFotoLauncher.launch(intent);
            }
        }
    }

    private File crearArchivoTemporalImagen() {
        try {
            File storageDir = getExternalFilesDir(null);
            return File.createTempFile("foto_admin_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void registrarAdministrador() {
        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String fecha = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (nombre.isEmpty() || nombre.length() > 28) {
            Toast.makeText(this, "Introduce un nombre válido (máx 28 caracteres)", Toast.LENGTH_SHORT).show(); return;
        }
        if (apellidos.isEmpty() || apellidos.length() > 28) {
            Toast.makeText(this, "Introduce apellidos válidos (máx 28 caracteres)", Toast.LENGTH_SHORT).show(); return;
        }
        if (dni.length() != 9 || !dni.matches("\\d{8}[A-Za-z]")) {
            Toast.makeText(this, "DNI inválido. Debe tener 8 números y una letra", Toast.LENGTH_SHORT).show(); return;
        }
        if (!fechaValida(fecha)) {
            Toast.makeText(this, "Fecha inválida. Usa el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show(); return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() > 30) {
            Toast.makeText(this, "Correo inválido (máx 30 caracteres)", Toast.LENGTH_SHORT).show(); return;
        }
        if (password.length() < 8 || password.length() > 15) {
            Toast.makeText(this, "La contraseña debe tener entre 8 y 15 caracteres", Toast.LENGTH_SHORT).show(); return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String photoUrl = (fotoSeleccionadaUri != null) ? fotoSeleccionadaUri.toString() : "";

                    Administrador admin = new Administrador(uid, nombre, apellidos, fecha, email, dni, photoUrl);

                    db.collection("administradores").document(uid).set(admin)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Administrador registrado correctamente", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("already in use")) {
                        Toast.makeText(this, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean fechaValida(String fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Permiso de cámara
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
