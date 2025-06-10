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

import edu.pmdm.gympro.CryptoUtils;
import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnLogin.setOnClickListener(v -> iniciarSesion());
        binding.btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        ImageView ivToggle = binding.ivTogglePassword;
        EditText etPassword = binding.etLoginPassword;

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
    }

    private void iniciarSesion() {
        String correo = binding.etLoginEmail.getText().toString().trim();
        String password = binding.etLoginPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "El formato del correo no es válido", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("administradores").get().addOnSuccessListener(snapshot -> {
            boolean correoEncontrado = false;

            for (var doc : snapshot) {
                String correoCifrado = doc.getString("correo");

                try {
                    String correoDescifrado = CryptoUtils.decrypt(correoCifrado);
                    if (correo.equals(correoDescifrado)) {
                        correoEncontrado = true;

                        auth.signInWithEmailAndPassword(correo, password)
                                .addOnSuccessListener(authResult -> {
                                    String uid = auth.getCurrentUser().getUid();
                                    db.collection("administradores").document(uid).get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot.exists()) {
                                                    String rol = documentSnapshot.getString("rol");

                                                    if ("administrador".equals(rol)) {
                                                        Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(this, edu.pmdm.gympro.MainActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(this, "Rol no autorizado para acceder", Toast.LENGTH_SHORT).show();
                                                        auth.signOut();
                                                    }
                                                } else {
                                                    Toast.makeText(this, "Datos no encontrados en Firestore", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "La contraseña es incorrecta", Toast.LENGTH_SHORT).show();
                                });

                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            if (!correoEncontrado) {
                Toast.makeText(this, "El correo electrónico no está registrado", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al verificar el correo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
