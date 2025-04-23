package edu.pmdm.gympro.ui.grupos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.databinding.ActivityCrearGrupoBinding;
import edu.pmdm.gympro.model.Grupo;

public class CrearGrupoActivity extends AppCompatActivity {

    private ActivityCrearGrupoBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri imagenUriSeleccionada;
    private Uri imagenUriCamara;
    private final int REQUEST_CAMERA_PERMISSION = 100;
    private String idGrupoEdicion = null;
    private boolean esEdicion = false;

    private final ActivityResultLauncher<Intent> launcherGaleria =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    binding.ivFotoGrupo.setImageURI(imagenUriSeleccionada);
                }
            });

    private final ActivityResultLauncher<Intent> launcherCamara =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && imagenUriCamara != null) {
                    imagenUriSeleccionada = imagenUriCamara;
                    binding.ivFotoGrupo.setImageURI(imagenUriSeleccionada);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrearGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cargarMonitores();

        binding.btnSeleccionarFoto.setOnClickListener(v -> mostrarOpcionesFoto());
        binding.btnCrearGrupo.setOnClickListener(v -> {
            if (esEdicion) editarGrupo();
            else crearGrupo();
        });
        binding.btnCancelar.setOnClickListener(v -> finish());

        // Modo edición
        if (getIntent().hasExtra("modo") && "editar".equals(getIntent().getStringExtra("modo"))) {
            esEdicion = true;
            binding.btnCrearGrupo.setText("Guardar cambios");

            idGrupoEdicion = getIntent().getStringExtra("id_grupo");
            binding.etNombreGrupo.setText(getIntent().getStringExtra("nombre"));
            binding.etDescripcionGrupo.setText(getIntent().getStringExtra("descripcion"));
            String foto = getIntent().getStringExtra("foto");

            if (foto != null && !foto.isEmpty() && !foto.equals("logo_por_defecto")) {
                Glide.with(this).load(foto).into(binding.ivFotoGrupo);
                imagenUriSeleccionada = Uri.parse(foto);
            } else {
                binding.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
            }

            // Si quieres preseleccionar el monitor, puedes hacerlo aquí si tienes un mapa nombre-posición
        } else {
            Glide.with(this)
                    .load(R.drawable.logo_gympro_sinfondo)
                    .into(binding.ivFotoGrupo);
        }
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
            return File.createTempFile("foto_grupo_", ".jpg", storageDir);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void cargarMonitores() {
        List<String> listaMonitores = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_azul,
                listaMonitores
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMonitores.setAdapter(adapter);

        db.collection("empleados")
                .whereEqualTo("rol", "monitor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String nombre = doc.getString("nombre") + " " + doc.getString("apellidos");
                        listaMonitores.add(nombre);
                    }
                    if (listaMonitores.isEmpty()) {
                        listaMonitores.add("Sin monitor asignado");
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void crearGrupo() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String descripcion = binding.etDescripcionGrupo.getText().toString().trim();
        String monitor = binding.spinnerMonitores.getSelectedItem() != null ? binding.spinnerMonitores.getSelectedItem().toString() : "Sin monitor asignado";

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa el nombre y la descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monitor.equals("Sin monitores disponibles")) {
            monitor = "Sin monitor asignado";
        }

        String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";
        String idAdministrador = auth.getCurrentUser().getUid();
        String idgrupo = UUID.randomUUID().toString();

        Grupo nuevoGrupo = new Grupo(idgrupo, nombre, descripcion, fotoUrl, monitor, idAdministrador);

        db.collection("grupos").document(idgrupo).set(nuevoGrupo)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Grupo creado correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al crear grupo", Toast.LENGTH_SHORT).show());
    }

    private void editarGrupo() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String descripcion = binding.etDescripcionGrupo.getText().toString().trim();
        String monitor = binding.spinnerMonitores.getSelectedItem() != null ? binding.spinnerMonitores.getSelectedItem().toString() : "Sin monitor asignado";

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa el nombre y la descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (monitor.equals("Sin monitores disponibles")) {
            monitor = "Sin monitor asignado";
        }

        String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";

        db.collection("grupos").document(idGrupoEdicion)
                .update("nombre", nombre,
                        "descripcion", descripcion,
                        "photo", fotoUrl,
                        "id_empleado", monitor)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Grupo actualizado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // 👈 Notificamos al fragmento que hubo cambios
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar grupo", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
