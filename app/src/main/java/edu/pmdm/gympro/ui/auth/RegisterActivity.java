package edu.pmdm.gympro.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityRegisterBinding;
import edu.pmdm.gympro.model.Empleado;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnRegistrarse.setOnClickListener(v -> registrarEmpleado());

        binding.btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // opcional
        });

        ImageView ivToggle = binding.ivTogglePassword;
        EditText etPassword = binding.etPassword;

        ivToggle.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggle.setImageResource(R.drawable.ic_visibility_off);
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void registrarEmpleado() {
        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String dni = binding.etDni.getText().toString().trim();
        String fecha = binding.etFechaNacimiento.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // VALIDACIONES
        if (nombre.isEmpty() || nombre.length() > 28) {
            Toast.makeText(this, "Introduce un nombre válido (máx 28 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (apellidos.isEmpty() || apellidos.length() > 28) {
            Toast.makeText(this, "Introduce apellidos válidos (máx 28 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dni.length() != 9 || !dni.matches("\\d{8}[A-Za-z]")) {
            Toast.makeText(this, "DNI inválido. Debe tener 8 números y una letra", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fechaValida(fecha)) {
            Toast.makeText(this, "Fecha inválida. Usa el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() > 30) {
            Toast.makeText(this, "Correo inválido (máx 30 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8 || password.length() > 15) {
            Toast.makeText(this, "La contraseña debe tener entre 8 y 15 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // REGISTRO
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    Empleado nuevoEmpleado = new Empleado(uid, nombre, apellidos, fecha, email, dni);

                    db.collection("empleados").document(uid).set(nuevoEmpleado)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Empleado registrado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    String mensaje;

                    if (errorMessage != null && errorMessage.contains("The email address is already in use")) {
                        mensaje = "El correo electrónico ya está registrado";
                    } else {
                        mensaje = "Error: " + errorMessage;
                    }

                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
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
}
