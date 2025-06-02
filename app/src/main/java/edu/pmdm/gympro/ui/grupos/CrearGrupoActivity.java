package edu.pmdm.gympro.ui.grupos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import edu.pmdm.gympro.model.Horario;
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
    private final List<Horario> horarios = new ArrayList<>();
    private List<String> listaNombresMonitores = new ArrayList<>();
    private List<String> listaIdsMonitores = new ArrayList<>();
    private String idMonitorDelGrupo = null;


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

        if (getIntent().hasExtra("modo") && "editar".equals(getIntent().getStringExtra("modo"))) {
            esEdicion = true;
            binding.btnCrearGrupo.setText("Guardar cambios");

            idGrupoEdicion = getIntent().getStringExtra("id_grupo");
            idMonitorDelGrupo = getIntent().getStringExtra("idMonitor");
            binding.etNombreGrupo.setText(getIntent().getStringExtra("nombre"));
            binding.etDescripcionGrupo.setText(getIntent().getStringExtra("descripcion"));
            String foto = getIntent().getStringExtra("foto");


            if (foto != null && !foto.isEmpty() && !foto.equals("logo_por_defecto")) {
                Glide.with(this).load(foto).into(binding.ivFotoGrupo);
                imagenUriSeleccionada = Uri.parse(foto);
            } else {
                binding.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
            }

            FirebaseFirestore.getInstance()
                    .collection("grupos")
                    .document(idGrupoEdicion)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Grupo grupo = documentSnapshot.toObject(Grupo.class);
                        if (grupo != null && grupo.getHorarios() != null) {
                            horarios.addAll(grupo.getHorarios());
                            for (Horario h : grupo.getHorarios()) {
                                agregarVistaHorario(h);
                            }
                        }
                    });
        } else {
            Glide.with(this)
                    .load(R.drawable.logo_gympro_sinfondo)
                    .into(binding.ivFotoGrupo);
        }

        binding.btnAgregarHorario.setOnClickListener(v -> mostrarDialogoHorario());
    }

    private void mostrarDialogoHorario() {
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
        Spinner spinnerDia = dialogView.findViewById(R.id.spinnerDia);
        EditText etHoraInicio = dialogView.findViewById(R.id.etHoraInicio);
        EditText etHoraFin = dialogView.findViewById(R.id.etHoraFin);

        spinnerDia.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dias));

        new AlertDialog.Builder(this)
                .setTitle("Añadir horario")
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String dia = spinnerDia.getSelectedItem().toString();
                    String horaInicio = etHoraInicio.getText().toString().trim();
                    String horaFin = etHoraFin.getText().toString().trim();

                    // Validar formato exacto hh:mm
                    if (!horaInicio.matches("^\\d{2}:\\d{2}$") || !horaFin.matches("^\\d{2}:\\d{2}$")) {
                        Toast.makeText(this, "Formato de hora inválido. Usa hh:mm (ej: 08:30)", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Horario horario = new Horario(dia, horaInicio, horaFin);
                    horarios.add(horario);
                    agregarVistaHorario(horario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void agregarVistaHorario(Horario horario) {
        TextView texto = new TextView(this);
        String textoHorario = horario.getDia() + ": " + horario.getHoraInicio() + " - " + horario.getHoraFin();
        texto.setText(textoHorario);
        texto.setTextColor(getResources().getColor(R.color.blue));
        texto.setPadding(0, 8, 0, 8);
        texto.setBackground(getResources().getDrawable(R.drawable.input_borde_azul, null));

        // Permite eliminar el horario al hacer clic
        texto.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar horario")
                    .setMessage("¿Quieres eliminar este horario?\n" + textoHorario)
                    .setPositiveButton("Sí", (dialog, which) -> {
                        horarios.remove(horario);                     // elimina de la lista
                        binding.layoutHorarios.removeView(texto);     // elimina de la vista
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        binding.layoutHorarios.addView(texto);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_azul,
                listaNombresMonitores
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_azul);
        binding.spinnerMonitores.setAdapter(adapter);

        String uid = auth.getCurrentUser().getUid();

        db.collection("monitores")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaNombresMonitores.clear();
                    listaIdsMonitores.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String nombreCompleto = doc.getString("nombre") + " " + doc.getString("apellidos");
                        String id = doc.getString("idMonitor");

                        listaNombresMonitores.add(nombreCompleto);
                        listaIdsMonitores.add(id);
                    }

                    if (listaNombresMonitores.isEmpty()) {
                        listaNombresMonitores.add("Sin monitores disponibles");
                        listaIdsMonitores.add("sin_monitor");
                        binding.spinnerMonitores.setEnabled(false);
                    } else {
                        binding.spinnerMonitores.setEnabled(true);
                    }

                    adapter.notifyDataSetChanged();

                    // Seleccionar el monitor si es edición
                    if (esEdicion && idMonitorDelGrupo != null) {
                        int posicion = listaIdsMonitores.indexOf(idMonitorDelGrupo);
                        if (posicion >= 0) {
                            binding.spinnerMonitores.setSelection(posicion);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar monitores", Toast.LENGTH_SHORT).show();
                });
    }


    private void crearGrupo() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String descripcion = binding.etDescripcionGrupo.getText().toString().trim();

        int posSeleccionada = binding.spinnerMonitores.getSelectedItemPosition();
        String idMonitorSeleccionado = (posSeleccionada >= 0 && posSeleccionada < listaIdsMonitores.size())
                ? listaIdsMonitores.get(posSeleccionada)
                : "sin_monitor";

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa el nombre y la descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.length() > 30) {
            Toast.makeText(this, "El nombre no puede tener más de 30 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horarios.isEmpty()) {
            Toast.makeText(this, "Debes añadir al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("grupos")
                .whereEqualTo("nombre", nombre)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Ya existe un grupo con ese nombre", Toast.LENGTH_SHORT).show();
                    } else {
                        String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";
                        String idAdministrador = auth.getCurrentUser().getUid();
                        String idgrupo = UUID.randomUUID().toString();

                        Grupo nuevoGrupo = new Grupo(idgrupo, nombre, descripcion, fotoUrl, idMonitorSeleccionado, idAdministrador, horarios);

                        db.collection("grupos").document(idgrupo).set(nuevoGrupo)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Grupo creado correctamente", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al crear grupo", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al comprobar duplicados", Toast.LENGTH_SHORT).show());
    }


    private void editarGrupo() {
        String nombre = binding.etNombreGrupo.getText().toString().trim();
        String descripcion = binding.etDescripcionGrupo.getText().toString().trim();

        int posSeleccionada = binding.spinnerMonitores.getSelectedItemPosition();
        String idMonitorSeleccionado = (posSeleccionada >= 0 && posSeleccionada < listaIdsMonitores.size())
                ? listaIdsMonitores.get(posSeleccionada)
                : "sin_monitor";

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Completa el nombre y la descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.length() > 30) {
            Toast.makeText(this, "El nombre no puede tener más de 30 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (descripcion.length() > 100) {
            Toast.makeText(this, "La descripción no puede tener más de 100 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (horarios.isEmpty()) {
            Toast.makeText(this, "Debes añadir al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        String fotoUrl = (imagenUriSeleccionada != null) ? imagenUriSeleccionada.toString() : "logo_por_defecto";

        db.collection("grupos").document(idGrupoEdicion)
                .update("nombre", nombre,
                        "descripcion", descripcion,
                        "foto", fotoUrl,
                        "idMonitor", idMonitorSeleccionado,
                        "horarios", horarios)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Grupo actualizado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar grupo", Toast.LENGTH_SHORT).show());
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
