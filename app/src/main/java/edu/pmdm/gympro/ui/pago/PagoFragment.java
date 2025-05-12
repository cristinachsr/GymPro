package edu.pmdm.gympro.ui.pago;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.model.Pago;

import java.util.*;

public class PagoFragment extends Fragment {

    private Spinner spinnerClientes, spinnerMes, spinnerAnio;
    private Button btnRegistrarPago;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Cliente> listaClientes = new ArrayList<>();
    private Button btnVerPagos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pago, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        spinnerClientes = view.findViewById(R.id.spinnerCliente);
        spinnerMes = view.findViewById(R.id.spinnerMes);
        spinnerAnio = view.findViewById(R.id.spinnerAnio);
        btnRegistrarPago = view.findViewById(R.id.btnRegistrarPago);

        cargarClientes();
        configurarSpinners();

        btnRegistrarPago.setOnClickListener(v -> registrarPago());

        Button btnVerPagos = view.findViewById(R.id.btnVerPagos);
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

    private void cargarClientes() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("clientes")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> nombres = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Cliente cliente = doc.toObject(Cliente.class);
                        listaClientes.add(cliente);
                        nombres.add(cliente.getNombre() + " " + cliente.getApellidos());
                    }
                    ArrayAdapter<String> adapterClientes = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_azul, nombres);
                    spinnerClientes.setAdapter(adapterClientes);
                });
    }

    private void registrarPago() {
        int posicion = spinnerClientes.getSelectedItemPosition();
        if (posicion < 0) {
            Toast.makeText(getContext(), "Selecciona un cliente", Toast.LENGTH_SHORT).show();
            return;
        }

        Cliente cliente = listaClientes.get(posicion);
        int mes = spinnerMes.getSelectedItemPosition() + 1;
        int año = Integer.parseInt(spinnerAnio.getSelectedItem().toString());

        // Verificar que no sea mes anterior al actual
        Calendar hoy = Calendar.getInstance();
        int mesActual = hoy.get(Calendar.MONTH) + 1;
        int añoActual = hoy.get(Calendar.YEAR);

        if (año < añoActual || (año == añoActual && mes < mesActual)) {
            Toast.makeText(getContext(), "No se pueden registrar pagos en meses pasados", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidAdmin = auth.getCurrentUser().getUid();

        // Verificar si ya existe
        db.collection("pagos")
                .whereEqualTo("idCliente", cliente.getIdCliente())
                .whereEqualTo("mes", mes)
                .whereEqualTo("año", año)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Toast.makeText(getContext(), "Este cliente ya tiene un pago registrado en ese mes", Toast.LENGTH_SHORT).show();
                    } else {
                        String idPago = UUID.randomUUID().toString();
                        Pago nuevoPago = new Pago(idPago, cliente.getIdCliente(), cliente.getNombre() + " " + cliente.getApellidos(),
                                mes, año, true, uidAdmin); // pagado = true

                        db.collection("pagos").document(idPago).set(nuevoPago)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(getContext(), "Pago registrado correctamente", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error al guardar el pago", Toast.LENGTH_SHORT).show());
                    }
                });
    }
}
