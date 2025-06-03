package edu.pmdm.gympro.ui.clientes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.SpaceItemDecoration;
import edu.pmdm.gympro.databinding.FragmentClientesBinding;
import edu.pmdm.gympro.model.Cliente;import edu.pmdm.gympro.CryptoUtils;

public class ClientesFragment extends Fragment {

    private FragmentClientesBinding binding;
    private FirebaseFirestore db;
    private ClienteAdapter clienteAdapter;
    private List<Cliente> listaClientes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentClientesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        cargarClientes();

        binding.btnCrearCliente.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CrearClienteActivity.class)));

        binding.etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clienteAdapter.filtrar(s.toString());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private void setupRecyclerView() {
        clienteAdapter = new ClienteAdapter(listaClientes, cliente -> {
            Intent intent = new Intent(requireContext(), DetalleClienteActivity.class);
            intent.putExtra("idCliente", cliente.getIdCliente());
            intent.putExtra("nombre", cliente.getNombre());
            intent.putExtra("apellidos", cliente.getApellidos());
            intent.putExtra("dni", cliente.getDni());
            intent.putExtra("fechaNacimiento", cliente.getFechaNacimiento());
            intent.putExtra("telefono", cliente.getTelefono());
            intent.putExtra("correo", cliente.getCorreo());
            intent.putExtra("foto", cliente.getFoto());
            intent.putStringArrayListExtra("gruposSeleccionados", new ArrayList<>(cliente.getGruposSeleccionados()));
            startActivity(intent);
        });

        binding.rvClientes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvClientes.setAdapter(clienteAdapter);
        binding.rvClientes.addItemDecoration(new SpaceItemDecoration(24));
    }

    private void cargarClientes() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("CLIENTES_FRAGMENT", "UID del administrador: " + uid);

        db.collection("clientes")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Cliente> clientesCargados = new ArrayList<>();
                    Log.d("CLIENTES_FRAGMENT", "Número de documentos recibidos: " + snapshot.size());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Cliente cliente = doc.toObject(Cliente.class);

                        // Descifrado con logs
                        try {
                            Log.d("CLIENTES_FRAGMENT", "Cliente obtenido (cifrado): " + cliente.getNombre());
                            cliente.setDni(CryptoUtils.decrypt(cliente.getDni()));
                            cliente.setCorreo(CryptoUtils.decrypt(cliente.getCorreo()));
                            cliente.setTelefono(CryptoUtils.decrypt(cliente.getTelefono()));
                            cliente.setFechaNacimiento(CryptoUtils.decrypt(cliente.getFechaNacimiento()));
                            Log.d("CLIENTES_FRAGMENT", "Cliente descifrado: " + cliente.getNombre() + " - " + cliente.getCorreo());
                        } catch (Exception e) {
                            Log.e("DECRYPT_ERROR", "Error al descifrar cliente: " + e.getMessage());
                            continue; // omitir cliente corrupto
                        }

                        clientesCargados.add(cliente);
                    }

                    Log.d("CLIENTES_FRAGMENT", "Clientes cargados y descifrados: " + clientesCargados.size());
                    clienteAdapter.actualizarLista(clientesCargados);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE_CLIENTES", "Error: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al cargar clientes", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarClientes();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
