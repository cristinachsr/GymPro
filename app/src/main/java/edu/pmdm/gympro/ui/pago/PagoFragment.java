package edu.pmdm.gympro.ui.pago;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.model.Pago;

public class PagoFragment extends Fragment {

    private static final int REQUEST_SELECCIONAR_CLIENTE = 100;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView tvClienteSeleccionado;
    private Button btnSeleccionarCliente;
    private Spinner spinnerMes, spinnerAnio;
    private Button btnRegistrarPago, btnVerPagos;

    private Cliente clienteSeleccionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pago, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvClienteSeleccionado = view.findViewById(R.id.tvClienteSeleccionado);
        btnSeleccionarCliente = view.findViewById(R.id.btnSeleccionarCliente);
        spinnerMes = view.findViewById(R.id.spinnerMes);
        spinnerAnio = view.findViewById(R.id.spinnerAnio);
        btnRegistrarPago = view.findViewById(R.id.btnRegistrarPago);
        btnVerPagos = view.findViewById(R.id.btnVerPagos);

        configurarSpinners();

        btnSeleccionarCliente.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SeleccionarClienteActivity.class);
            startActivityForResult(intent, REQUEST_SELECCIONAR_CLIENTE);
        });

        btnRegistrarPago.setOnClickListener(v -> registrarPago());

        btnVerPagos.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), VerPagosActivity.class))
        );

        return view;
    }

    private void configurarSpinners() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        ArrayAdapter<String> adapterMes = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_azul, meses);
        spinnerMes.setAdapter(adapterMes);

        int añoActual = Calendar.getInstance().get(Calendar.YEAR);
        List<String> años = new ArrayList<>();
        for (int i = añoActual; i <= añoActual + 2; i++) {
            años.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapterAnio = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_azul, años);
        spinnerAnio.setAdapter(adapterAnio);
        spinnerAnio.setSelection(0);
    }

    private void registrarPago() {
        if (clienteSeleccionado == null) {
            Toast.makeText(getContext(), "Selecciona un cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        int mes = spinnerMes.getSelectedItemPosition() + 1;
        int año = Integer.parseInt(spinnerAnio.getSelectedItem().toString());

        Calendar hoy = Calendar.getInstance();
        int mesActual = hoy.get(Calendar.MONTH) + 1;
        int añoActual = hoy.get(Calendar.YEAR);

        if (año < añoActual || (año == añoActual && mes < mesActual)) {
            Toast.makeText(getContext(), "No se pueden registrar pagos en meses pasados", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidAdmin = auth.getCurrentUser().getUid();

        db.collection("pagos")
                .whereEqualTo("idCliente", clienteSeleccionado.getIdCliente())
                .whereEqualTo("mes", mes)
                .whereEqualTo("año", año)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(getContext(), "Este cliente ya tiene un pago registrado en ese mes", Toast.LENGTH_SHORT).show();
                    } else {
                        String idPago = UUID.randomUUID().toString();
                        Pago nuevoPago = new Pago(idPago, clienteSeleccionado.getIdCliente(), mes, año, true, uidAdmin);

                        db.collection("pagos").document(idPago).set(nuevoPago)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(getContext(), "Pago registrado correctamente", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error al guardar el pago", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECCIONAR_CLIENTE && resultCode == Activity.RESULT_OK && data != null) {
            String idCliente = data.getStringExtra("clienteId");
            String nombreCliente = data.getStringExtra("clienteNombre");

            clienteSeleccionado = new Cliente();
            clienteSeleccionado.setIdCliente(idCliente);

            tvClienteSeleccionado.setText(nombreCliente);
        }
    }
}
