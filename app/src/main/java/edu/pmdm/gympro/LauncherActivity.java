package edu.pmdm.gympro;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Usuario logueado → va al panel principal
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // No logueado → va al login
            startActivity(new Intent(this, edu.pmdm.gympro.ui.auth.LoginActivity.class));
        }

        finish(); // Cerramos esta actividad para que no pueda volver atrás
    }
}
