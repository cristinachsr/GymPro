package edu.pmdm.gympro.ui.grupos;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.databinding.ActivityListaClientesGrupoBinding;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.ui.clientes.ClienteGrupoAdapter;

public class ListaClientesGrupoActivity extends AppCompatActivity {

    private ActivityListaClientesGrupoBinding binding;
    private List<Cliente> listaClientes = new ArrayList<>();
    private ClienteGrupoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaClientesGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarClientesGrupo);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String idGrupo = getIntent().getStringExtra("id_grupo");

        adapter = new ClienteGrupoAdapter(this, listaClientes);
        binding.recyclerClientesGrupo.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerClientesGrupo.addItemDecoration(new SpaceItemDecoration(24));
        binding.recyclerClientesGrupo.setAdapter(adapter);

        FirebaseFirestore.getInstance()
                .collection("clientes")
                .whereArrayContains("gruposSeleccionados", idGrupo)
                .get()
                .addOnSuccessListener(query -> {
                    listaClientes.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Cliente cliente = doc.toObject(Cliente.class);

                        if (cliente != null && cliente.getCorreo() != null) {
                            try {
                                cliente.setCorreo(edu.pmdm.gympro.CryptoUtils.decrypt(cliente.getCorreo()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        listaClientes.add(cliente);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar clientes", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}