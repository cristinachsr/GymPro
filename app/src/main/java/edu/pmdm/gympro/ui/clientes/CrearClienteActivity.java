package edu.pmdm.gympro.ui.clientes;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityCrearClienteBinding;
import edu.pmdm.gympro.model.Cliente;
import edu.pmdm.gympro.CryptoUtils;

public class CrearClienteActivity extends AppCompatActivity {

    private ActivityCrearClienteBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imagenUriSeleccionada;
    private Uri imagenUriCamara;
    private final int REQUEST_CAMERA_PERMISSION = 101;
    private boolean modoEdicion = false;
    private String idClienteEdicion;

    private List<String> gruposSeleccionados = new ArrayList<>();
    private List<String> nombresGruposSeleccionados = new ArrayList<>();
    private GrupoSeleccionadoAdapter grupoSeleccionadoAdapter;

    private final ActivityResultLauncher<Intent> launcherGaleria =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    Glide.with(this).load(imagenUriSeleccionada).into(binding.ivFotoCliente);
                }
            });

    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imagenUriCamara != null) {
                    imagenUriSeleccionada = imagenUriCamara;
                    Glide.with(this).load(imagenUriCamara).into(binding.ivFotoCliente);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        modoEdicion = getIntent().getBooleanExtra("modoEdicion", false);

        if (modoEdicion) {
            binding.toolbar.setTitle("Editar cliente");
            binding.btnCrearCliente.setText("Guardar cambios");

            idClienteEdicion = getIntent().getStringExtra("idCliente");

            binding.etNombreCliente.setText(getIntent().getStringExtra("nombre"));
            binding.etApellidosCliente.setText(getIntent().getStringExtra("apellidos"));
            binding.etDniCliente.setText(getIntent().getStringExtra("dni"));
            binding.etFechaNacimientoCliente.setText(getIntent().getStringExtra("fechaNacimiento"));
            String telefonoCompleto = getIntent().getStringExtra("telefono");
            if (telefonoCompleto != null && telefonoCompleto.startsWith("+")) {
                // Asignar el número local sin el prefijo
                binding.countryCodePickerCliente.setFullNumber(telefonoCompleto.replace("+", ""));
                String numeroSinPrefijo = telefonoCompleto.replace("+" + binding.countryCodePickerCliente.getSelectedCountryCode(), "");
                binding.etTelefonoCliente.setText(numeroSinPrefijo);
            }
            binding.etCorreoCliente.setText(getIntent().getStringExtra("correo"));

            String foto = getIntent().getStringExtra("foto");
            if (foto != null && !foto.equals("logo_por_defecto")) {
                imagenUriSeleccionada = Uri.parse(foto);
                Glide.with(this).load(imagenUriSeleccionada).into(binding.ivFotoCliente);
            } else {
                Glide.with(this).load(R.drawable.logo_gympro_sinfondo).into(binding.ivFotoCliente);
            }


        } else {
            Glide.with(this).load(R.drawable.logo_gympro_sinfondo).into(binding.ivFotoCliente);
        }

        binding.btnSeleccionarFotoCliente.setOnClickListener(v -> mostrarOpcionesFoto());
        binding.btnCancelarCliente.setOnClickListener(v -> finish());
        binding.btnCrearCliente.setOnClickListener(v -> guardarCliente());

        binding.countryCodePickerCliente.registerCarrierNumberEditText(binding.etTelefonoCliente);

        grupoSeleccionadoAdapter = new GrupoSeleccionadoAdapter(nombresGruposSeleccionados);
        binding.rvGruposSeleccionados.setAdapter(grupoSeleccionadoAdapter);

        binding.btnSeleccionarGrupos.setOnClickListener(v -> mostrarDialogoSeleccionGrupos());

        String uidAdmin = auth.getCurrentUser().getUid();

        List<String> gruposIntent = getIntent().getStringArrayListExtra("gruposSeleccionados");
        if (gruposIntent != null) {
            gruposSeleccionados.addAll(gruposIntent);

            db.collection("grupos")
                    .whereEqualTo("idAdministrador", uidAdmin)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (var doc : snapshot) {
                            if (gruposSeleccionados.contains(doc.getId())) {
                                String nombreGrupo = doc.getString("nombre");
                                if (nombreGrupo != null) {
                                    nombresGruposSeleccionados.add(nombreGrupo);
                                }
                            }
                        }
                        grupoSeleccionadoAdapter.notifyDataSetChanged();
                        binding.tvGruposSeleccionados.setText(String.join(", ", nombresGruposSeleccionados));
                    });
        }
    }

    private void mostrarDialogoSeleccionGrupos() {
        String uidAdmin = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("grupos")
                .whereEqualTo("idAdministrador", uidAdmin) // Solo grupos del admin actual
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> nombresGrupos = new ArrayList<>();
                    List<String> idsGrupos = new ArrayList<>();

                    for (var doc : snapshot) {
                        nombresGrupos.add(doc.getString("nombre"));
                        idsGrupos.add(doc.getId());
                    }

                    boolean[] seleccionados = new boolean[nombresGrupos.size()];
                    for (int i = 0; i < nombresGrupos.size(); i++) {
                        seleccionados[i] = gruposSeleccionados.contains(idsGrupos.get(i));
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Seleccionar grupos")
                            .setMultiChoiceItems(nombresGrupos.toArray(new String[0]), seleccionados, (dialog, which, isChecked) -> {
                                if (isChecked) {
                                    gruposSeleccionados.add(idsGrupos.get(which));
                                    nombresGruposSeleccionados.add(nombresGrupos.get(which));
                                } else {
                                    gruposSeleccionados.remove(idsGrupos.get(which));
                                    nombresGruposSeleccionados.remove(nombresGrupos.get(which));
                                }
                            })
                            .setPositiveButton("Aceptar", (dialog, which) -> {
                                grupoSeleccionadoAdapter.notifyDataSetChanged();

                                if (nombresGruposSeleccionados.isEmpty()) {
                                    binding.tvGruposSeleccionados.setText("Sin grupos seleccionados");
                                } else {
                                    binding.tvGruposSeleccionados.setText(String.join(", ", nombresGruposSeleccionados));
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                });
    }

    private void guardarCliente() {
        String nombre = binding.etNombreCliente.getText().toString().trim();
        String apellidos = binding.etApellidosCliente.getText().toString().trim();
        String dni = binding.etDniCliente.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimientoCliente.getText().toString().trim();
        String telefono = binding.countryCodePickerCliente.getFullNumberWithPlus().trim();
        String correo = binding.etCorreoCliente.getText().toString().trim();
        String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";

        if (!validarCampos(nombre, apellidos, dni, fechaNacimiento, telefono, correo)) return;

        if (modoEdicion) {
            actualizarCliente(nombre, apellidos, dni, fechaNacimiento, telefono, correo, fotoUrl);
        } else {
            crearNuevoCliente(nombre, apellidos, dni, fechaNacimiento, telefono, correo, fotoUrl);
        }
    }

    private void crearNuevoCliente(String nombre, String apellidos, String dni, String fechaNacimiento,
                                   String telefono, String correo, String fotoUrl) {

        // Ciframos los datos ANTES de comprobar duplicados
        String dniCifrado = CryptoUtils.encrypt(dni);
        String telefonoCifrado = CryptoUtils.encrypt(telefono);
        String correoCifrado = CryptoUtils.encrypt(correo);
        String fechaCifrada = CryptoUtils.encrypt(fechaNacimiento);

        db.collection("clientes").whereEqualTo("dni", dniCifrado).get().addOnSuccessListener(snapshotDni -> {
            if (!snapshotDni.isEmpty()) {
                Toast.makeText(this, "Ya existe un cliente con ese DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("clientes").whereEqualTo("telefono", telefonoCifrado).get().addOnSuccessListener(snapshotTel -> {
                if (!snapshotTel.isEmpty()) {
                    Toast.makeText(this, "Ya existe un cliente con ese número", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("clientes").whereEqualTo("correo", correoCifrado).get().addOnSuccessListener(snapshotCorreo -> {
                    if (!snapshotCorreo.isEmpty()) {
                        Toast.makeText(this, "Ya existe un cliente con ese correo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String idAdministrador = auth.getCurrentUser().getUid();
                    String idCliente = UUID.randomUUID().toString();

                    Cliente cliente = new Cliente(
                            idCliente,
                            nombre,
                            apellidos,
                            dniCifrado,
                            fechaCifrada,
                            telefonoCifrado,
                            correoCifrado,
                            fotoUrl,
                            idAdministrador,
                            gruposSeleccionados
                    );

                    db.collection("clientes").document(idCliente).set(cliente)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Cliente creado correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar cliente", Toast.LENGTH_SHORT).show());
                });
            });
        });
    }

    private void actualizarCliente(String nombre, String apellidos, String dni, String fechaNacimiento,
                                   String telefono, String correo, String fotoUrl) {

        String idAdministrador = auth.getCurrentUser().getUid();

        // Ciframos los datos que vamos a comparar
        String dniCifrado = CryptoUtils.encrypt(dni);
        String telefonoCifrado = CryptoUtils.encrypt(telefono);
        String correoCifrado = CryptoUtils.encrypt(correo);
        String fechaCifrada = CryptoUtils.encrypt(fechaNacimiento);

        db.collection("clientes").get().addOnSuccessListener(snapshot -> {
            boolean dniDuplicado = false;
            boolean telDuplicado = false;
            boolean correoDuplicado = false;

            for (var doc : snapshot) {
                String id = doc.getId();
                if (id.equals(idClienteEdicion)) continue; // Saltar al cliente que estamos editando

                if (dniCifrado.equals(doc.getString("dni"))) dniDuplicado = true;
                if (telefonoCifrado.equals(doc.getString("telefono"))) telDuplicado = true;
                if (correoCifrado.equals(doc.getString("correo"))) correoDuplicado = true;
            }

            if (dniDuplicado) {
                Toast.makeText(this, "Ya existe otro cliente con ese DNI", Toast.LENGTH_SHORT).show();
                return;
            }
            if (telDuplicado) {
                Toast.makeText(this, "Ya existe otro cliente con ese teléfono", Toast.LENGTH_SHORT).show();
                return;
            }
            if (correoDuplicado) {
                Toast.makeText(this, "Ya existe otro cliente con ese correo", Toast.LENGTH_SHORT).show();
                return;
            }

            Cliente clienteActualizado = new Cliente(
                    idClienteEdicion,
                    nombre,
                    apellidos,
                    dniCifrado,
                    fechaCifrada,
                    telefonoCifrado,
                    correoCifrado,
                    fotoUrl,
                    idAdministrador,
                    gruposSeleccionados
            );

            db.collection("clientes").document(idClienteEdicion).set(clienteActualizado)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar cliente", Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(this, "Error al verificar duplicados", Toast.LENGTH_SHORT).show());
    }


    private void mostrarOpcionesFoto() {
        String[] opciones = {"Galería", "Cámara"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirGaleria();
                    else abrirCamara();
                }).show();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcherGaleria.launch(intent);
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            File foto = crearArchivoTemporalImagen();
            if (foto != null) {
                imagenUriCamara = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", foto);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
                launcherCamara.launch(intent);
            }
        }
    }

    private File crearArchivoTemporalImagen() {
        try {
            File storageDir = getExternalFilesDir(null);
            return File.createTempFile("foto_cliente_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private boolean validarCampos(String nombre, String apellidos, String dni, String fechaNacimiento, String telefono, String correo) {
        if (nombre.trim().isEmpty() || apellidos.trim().isEmpty() || dni.trim().isEmpty() ||
                fechaNacimiento.trim().isEmpty() || telefono.trim().isEmpty() || correo.trim().isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!nombre.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Toast.makeText(this, "El nombre solo puede contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!apellidos.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            Toast.makeText(this, "Los apellidos solo pueden contener letras y espacios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!dni.matches("\\d{8}[A-Z]")) {
            Toast.makeText(this, "DNI inválido (ejemplo: 12345678A)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!binding.countryCodePickerCliente.isValidFullNumber()) {
            Toast.makeText(this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!correo.matches("^[a-zA-Z0-9._%+-]+@(gmail|hotmail)\\.(com|es)$") || correo.length() > 30) {
            Toast.makeText(this, "Correo inválido. Escribe un correo válido como ejemplo@gmail.com o ejemplo@hotmail.es", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (fechaNacimiento.trim().isEmpty()) {
            Toast.makeText(this, "Fecha de nacimiento obligatoria", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!fechaValida(fechaNacimiento)) {
            Toast.makeText(this, "Fecha inválida. Asegúrate de escribir un día, mes y año reales en formato dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private boolean fechaValida(String fecha) {
        // Validar formato con regex antes de parsear
        if (!fecha.matches("^\\d{2}/\\d{2}/\\d{4}$")) return false;

        String[] partes = fecha.split("/");
        int dia = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int año = Integer.parseInt(partes[2]);

        // Validar valores lógicos de día, mes y año
        if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || año < 1900) return false;

        // Validación estricta usando SimpleDateFormat
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
