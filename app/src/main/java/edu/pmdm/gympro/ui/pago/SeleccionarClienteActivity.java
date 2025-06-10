package edu.pmdm.gympro.ui.pago;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.CryptoUtils;
import edu.pmdm.gympro.R;
import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.ui.clientes.ClienteAdapter;

public class SeleccionarClienteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewClientes;
    private List<Cliente> clientes = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_cliente);

        recyclerViewClientes = findViewById(R.id.recyclerViewClientes);
        recyclerViewClientes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClientes.addItemDecoration(new SpaceItemDecoration(24));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String uid = auth.getCurrentUser().getUid();
        db.collection("clientes")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Cliente cliente = doc.toObject(Cliente.class);

                        cliente.setCorreo(CryptoUtils.decrypt(cliente.getCorreo()));
                        cliente.setDni(CryptoUtils.decrypt(cliente.getDni()));
                        cliente.setTelefono(CryptoUtils.decrypt(cliente.getTelefono()));
                        cliente.setFechaNacimiento(CryptoUtils.decrypt(cliente.getFechaNacimiento()));

                        clientes.add(cliente);
                    }

                    ClienteAdapter adapter = new ClienteAdapter(clientes, cliente -> {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("clienteId", cliente.getIdCliente());
                        resultIntent.putExtra("clienteNombre", cliente.getNombre() + " " + cliente.getApellidos());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    });

                    recyclerViewClientes.setAdapter(adapter);
                });
    }
}
