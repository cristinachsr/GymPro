package edu.pmdm.gympro;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class CambiarIdiomaActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_idioma);

        // 1. Establecer el toolbar como ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. Habilitar flecha atrás
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 3. Resto de lógica
        radioGroup = findViewById(R.id.radioGroupIdioma);
        btnGuardar = findViewById(R.id.btnGuardarIdioma);

        btnGuardar.setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            String idioma = "es";
            if (checkedId == R.id.radioEng) idioma = "en";

            Locale nueva = new Locale(idioma);
            Locale.setDefault(nueva);

            Configuration config = new Configuration();
            config.locale = nueva;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            SharedPreferences.Editor editor = getSharedPreferences("prefs", MODE_PRIVATE).edit();
            editor.putString("idioma", idioma);
            editor.apply();

            recreate(); // o puedes reiniciar MainActivity
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
