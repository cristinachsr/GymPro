package edu.pmdm.gympro.ui.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
        binding.countryCodePicker.registerCarrierNumberEditText(binding.etTelefono);

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        int color;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            color = ContextCompat.getColor(this, android.R.color.white); // blanco para modo oscuro
        } else {
            color = ContextCompat.getColor(this, R.color.blue); // azul para modo claro
        }

        try {
            java.lang.reflect.Field field = binding.countryCodePicker.getClass().getDeclaredField("selectedTextView");
            field.setAccessible(true);
            TextView selectedTextView = (TextView) field.get(binding.countryCodePicker);
            selectedTextView.setTextColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //contraseña
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

        //confirmar contraseña
        EditText etConfirmarPassword = binding.etConfirmarPassword;
        ImageView ivToggleConfirm = binding.ivToggleConfirmPassword;

        ivToggleConfirm.setOnClickListener(v -> {
            if (etConfirmarPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etConfirmarPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggleConfirm.setImageResource(R.drawable.ic_visibility_sinfondo);
            } else {
                etConfirmarPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggleConfirm.setImageResource(R.drawable.ic_visibility_off_sinfondo);
            }
            etConfirmarPassword.setSelection(etConfirmarPassword.getText().length());
        });

        // Selección de foto
        binding.btnSeleccionarFoto.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        binding.btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
    }

    private void registrarAdministrador() {
        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String fecha = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmarPassword = binding.etConfirmarPassword.getText().toString().trim();
        String telefono = binding.countryCodePicker.getFullNumberWithPlus().trim();  // ← Usamos CountryCodePicker

        if (!nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{1,28}$")) {
            toast("Introduce un nombre válido (solo letras y espacios, máx 28 caracteres)"); return;
        }

        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{1,28}$")) {
            toast("Introduce apellidos válidos (solo letras y espacios, máx 28 caracteres)"); return;
        }

        if (dni.length() != 9 || !dni.matches("\\d{8}[A-Za-z]")) {
            toast("DNI inválido. Debe tener 8 números y una letra"); return;
        }

        if (!fecha.matches("\\d{2}/\\d{2}/\\d{4}") || !fechaValida(fecha)) {
            toast("Fecha inválida. Usa el formato dd/MM/yyyy (con año completo)"); return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() > 30) {
            toast("Correo inválido (máx 30 caracteres)"); return;
        }

        if (password.length() < 8 || password.length() > 15) {
            toast("La contraseña debe tener entre 8 y 15 caracteres"); return;
        }

        if (!password.equals(confirmarPassword)) {
            toast("Las contraseñas no coinciden");
            return;
        }

        if (!binding.countryCodePicker.isValidFullNumber()) {
            toast("Número de teléfono no válido"); return;
        }



        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String photoUrl = (fotoSeleccionadaUri != null) ? fotoSeleccionadaUri.toString() : "";

                    Administrador admin = new Administrador(uid, nombre, apellidos, fecha, email, dni, telefono, photoUrl);

                    db.collection("administradores").document(uid).set(admin)
                            .addOnSuccessListener(unused -> {
                                toast("Administrador registrado correctamente");
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> toast("Error al guardar en Firestore"));
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("already in use")) {
                        toast("El correo electrónico ya está registrado");
                    } else {
                        toast("Error: " + msg);
                    }
                });
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
            toast("Error al crear archivo de imagen");
            return null;
        }
    }

    private boolean fechaValida(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            toast("Permiso de cámara denegado");
        }
    }
}
