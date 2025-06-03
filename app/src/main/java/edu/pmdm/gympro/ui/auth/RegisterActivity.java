package edu.pmdm.gympro.ui.auth;

import android.Manifest;
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

import edu.pmdm.gympro.CryptoUtils;
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
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        fotoSeleccionadaUri = uri;
                        binding.ivProfilePic.setImageURI(fotoSeleccionadaUri);
                        toast("Imagen seleccionada desde galería");
                    } else {
                        toast("No se pudo obtener la imagen de galería");
                    }
                }
            });

    private final ActivityResultLauncher<Intent> tomarFotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && fotoCamaraUri != null) {
                    fotoSeleccionadaUri = fotoCamaraUri;
                    binding.ivProfilePic.setImageURI(fotoSeleccionadaUri);
                    toast("Imagen capturada desde cámara");
                } else {
                    toast("No se capturó la imagen correctamente");
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

        configurarModoColor();
        configurarTogglePassword(binding.etPassword, binding.ivTogglePassword);
        configurarTogglePassword(binding.etConfirmarPassword, binding.ivToggleConfirmPassword);

        binding.btnSeleccionarFoto.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.blue));
        binding.btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
    }

    private void registrarAdministrador() {
        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String fecha = binding.etFechaNacimiento.getText().toString().trim();
        String correo = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmarPassword = binding.etConfirmarPassword.getText().toString().trim();
        String telefono = binding.countryCodePicker.getFullNumberWithPlus().trim();

        if (!nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{1,28}$")) {
            toast("Introduce un nombre válido"); return;
        }
        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúñÑ ]{1,28}$")) {
            toast("Introduce apellidos válidos"); return;
        }
        if (dni.length() != 9 || !dni.matches("\\d{8}[A-Z]")) {
            toast("DNI inválido"); return;
        }
        if (!fecha.matches("\\d{2}/\\d{2}/\\d{4}") || !fechaValida(fecha)) {
            toast("Fecha inválida"); return;
        }
        if (!correo.matches("^[a-zA-Z0-9._%+-]+@(gmail|hotmail)\\.(com|es)$") || correo.length() > 30) {
            toast("Correo inválido. Escribe un correo válido como ejemplo@gmail.com o ejemplo@hotmail.es"); return;
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\\$%\\^&\\*._-]).{8,15}$")) {
            toast("La contraseña debe tener entre 8 y 15 caracteres, incluir al menos una mayúscula, un número y un símbolo (como . o @)");
            return;
        }
        if (!password.equals(confirmarPassword)) {
            toast("Las contraseñas no coinciden"); return;
        }
        if (!binding.countryCodePicker.isValidFullNumber()) {
            toast("Número de teléfono no válido"); return;
        }

        // 🔒 Cifrar los datos antes de consultar Firestore
        String dniCifrado = CryptoUtils.encrypt(dni);
        String telefonoCifrado = CryptoUtils.encrypt(telefono);
        String correoCifrado = CryptoUtils.encrypt(correo);

        // Buscar por DNI cifrado
        db.collection("administradores")
                .whereEqualTo("dni", dniCifrado)
                .get()
                .addOnSuccessListener(snapshotDni -> {
                    if (!snapshotDni.isEmpty()) {
                        toast("Ya existe un administrador con este DNI");
                        return;
                    }

                    // Buscar por teléfono cifrado
                    db.collection("administradores")
                            .whereEqualTo("telefono", telefonoCifrado)
                            .get()
                            .addOnSuccessListener(snapshotTel -> {
                                if (!snapshotTel.isEmpty()) {
                                    toast("Ya existe un administrador con este teléfono");
                                    return;
                                }

                                // Buscar por correo cifrado
                                db.collection("administradores")
                                        .whereEqualTo("correo", correoCifrado)
                                        .get()
                                        .addOnSuccessListener(snapshotCorreo -> {
                                            if (!snapshotCorreo.isEmpty()) {
                                                toast("Ya existe un administrador con este correo");
                                                return;
                                            }

                                            // Ningún campo está duplicado, proceder a crear el usuario
                                            crearUsuarioFirebase(nombre, apellidos, dni, fecha, correo, password, telefono);
                                        })
                                        .addOnFailureListener(e -> toast("Error al verificar correo"));
                            })
                            .addOnFailureListener(e -> toast("Error al verificar teléfono"));
                })
                .addOnFailureListener(e -> toast("Error al verificar DNI"));
    }

    private void crearUsuarioFirebase(String nombre, String apellidos, String dni, String fecha,
                                      String correo, String password, String telefono) {

        auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    String fotoUrl = (fotoSeleccionadaUri != null) ? fotoSeleccionadaUri.toString() : "logo_por_defecto";

                    // Cifrar campos sensibles antes de guardar en Firestore
                    String fechaCifrada = CryptoUtils.encrypt(fecha);
                    String correoCifrado = CryptoUtils.encrypt(correo);
                    String dniCifrado = CryptoUtils.encrypt(dni);
                    String telefonoCifrado = CryptoUtils.encrypt(telefono);

                    Administrador admin = new Administrador(uid, nombre, apellidos, fechaCifrada, correoCifrado, dniCifrado, telefonoCifrado, fotoUrl);

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
                .setTitle("Seleccionar imagen de perfil")
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
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        // Validar formato con regex antes de parsear
        if (!fecha.matches("^\\d{2}/\\d{2}/\\d{4}$")) return false;

        String[] partes = fecha.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int año = Integer.parseInt(partes[2]);

        // Validar valores lógicos de día, mes y año
        if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || año < 1900) return false;

        // Validación estricta usando SimpleDateFormat
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }


    private void configurarTogglePassword(EditText editText, ImageView toggleIcon) {
        toggleIcon.setOnClickListener(v -> {
            boolean visible = (editText.getInputType() != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            editText.setInputType(visible ?
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) :
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
            toggleIcon.setImageResource(visible ? R.drawable.ic_visibility_off_sinfondo : R.drawable.ic_visibility_sinfondo);
            editText.setSelection(editText.getText().length());
        });
    }

    private void configurarModoColor() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int color = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ?
                ContextCompat.getColor(this, android.R.color.white) :
                ContextCompat.getColor(this, R.color.blue);
        try {
            java.lang.reflect.Field field = binding.countryCodePicker.getClass().getDeclaredField("selectedTextView");
            field.setAccessible(true);
            TextView selectedTextView = (TextView) field.get(binding.countryCodePicker);
            selectedTextView.setTextColor(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            toast("Permiso de cámara denegado");
        }
    }
}
