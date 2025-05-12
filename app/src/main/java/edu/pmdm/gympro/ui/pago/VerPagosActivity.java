package edu.pmdm.gympro.ui.pago;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.SimpleItemSelectedListener;
import edu.pmdm.gympro.model.Pago;

public class VerPagosActivity extends AppCompatActivity {

    private Spinner spinnerMes, spinnerAnio;
    private ListView listViewPagos;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pago);

        // Configurar Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerMes = findViewById(R.id.spinnerMesFiltro);
        spinnerAnio = findViewById(R.id.spinnerAnioFiltro);
        listViewPagos = findViewById(R.id.listViewPagos);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        configurarSpinners();
        cargarPagos();

        spinnerMes.setOnItemSelectedListener(new SimpleItemSelectedListener(this::cargarPagos));
        spinnerAnio.setOnItemSelectedListener(new SimpleItemSelectedListener(this::cargarPagos));
    }

    private void configurarSpinners() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(this, R.layout.spinner_item_azul, meses);
        spinnerMes.setAdapter(adapterMes);

        int añoActual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> años = new ArrayList<>();
        for (int i = añoActual - 2; i <= añoActual + 2; i++) {
            años.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapterAnio = new ArrayAdapter<>(this, R.layout.spinner_item_azul, años);
        spinnerAnio.setAdapter(adapterAnio);

        spinnerMes.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        spinnerAnio.setSelection(años.indexOf(String.valueOf(añoActual)));
    }

    private void cargarPagos() {
        int mes = spinnerMes.getSelectedItemPosition() + 1;
        int año = Integer.parseInt(spinnerAnio.getSelectedItem().toString());
        String uid = auth.getCurrentUser().getUid();

        db.collection("pagos")
                .whereEqualTo("mes", mes)
                .whereEqualTo("año", año)
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> pagosTexto = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Pago pago = doc.toObject(Pago.class);
                        pagosTexto.add(pago.getNombreCliente() + " - " + (pago.isPagado() ? "Pagado" : "No pagado"));
                    }

                    if (pagosTexto.isEmpty()) {
                        pagosTexto.add("No hay pagos registrados para este mes.");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_azul, pagosTexto);
                    listViewPagos.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar pagos", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

