package edu.pmdm.gympro.ui.analisis;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.pmdm.gympro.databinding.FragmentAnalisisBinding;

public class AnalisisFragment extends Fragment {

    private FragmentAnalisisBinding binding;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAnalisisBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        cargarGraficoClientesPorGrupo();
        cargarGraficoGruposPorMonitor();

        binding.btnExportarGrafico.setOnClickListener(v ->
                exportarGraficoComoImagen(binding.pieChartClientes, "clientes_por_grupo")
        );

        binding.btnExportarGrafico2.setOnClickListener(v ->
                exportarGraficoComoImagen(binding.barChartGruposPorMonitor, "grupos_por_monitor")
        );

        binding.btnExportarCsv.setOnClickListener(v -> exportarDatosClientesPorGrupoComoCSV());

        binding.btnExportarCsvGruposPorMonitor.setOnClickListener(v -> exportarDatosGruposPorMonitorComoCSV());


        return binding.getRoot();
    }

    private void cargarGraficoClientesPorGrupo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("grupos")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(gruposSnapshot -> {
                    Map<String, String> idToNombreGrupo = new HashMap<>();
                    Map<String, Integer> conteoPorGrupoId = new HashMap<>();

                    for (QueryDocumentSnapshot grupoDoc : gruposSnapshot) {
                        String idGrupo = grupoDoc.getId();
                        String nombreGrupo = grupoDoc.getString("nombre");
                        if (nombreGrupo != null) {
                            idToNombreGrupo.put(idGrupo, nombreGrupo);
                            conteoPorGrupoId.put(idGrupo, 0);
                        }
                    }

                    db.collection("clientes")
                            .whereEqualTo("idAdministrador", uid)
                            .get()
                            .addOnSuccessListener(clientesSnapshot -> {
                                for (QueryDocumentSnapshot clienteDoc : clientesSnapshot) {
                                    List<String> gruposSeleccionados = (List<String>) clienteDoc.get("gruposSeleccionados");
                                    if (gruposSeleccionados != null) {
                                        for (String idGrupo : gruposSeleccionados) {
                                            if (conteoPorGrupoId.containsKey(idGrupo)) {
                                                conteoPorGrupoId.put(idGrupo, conteoPorGrupoId.get(idGrupo) + 1);
                                            }
                                        }
                                    }
                                }

                                List<PieEntry> entries = new ArrayList<>();
                                for (Map.Entry<String, Integer> entry : conteoPorGrupoId.entrySet()) {
                                    int cantidad = entry.getValue();
                                    if (cantidad > 0) {
                                        String nombreGrupo = idToNombreGrupo.get(entry.getKey());
                                        entries.add(new PieEntry(cantidad, nombreGrupo));
                                    }
                                }

                                if (entries.isEmpty()) {
                                    Toast.makeText(getContext(), "No hay inscripciones para mostrar", Toast.LENGTH_SHORT).show();
                                }

                                PieDataSet dataSet = new PieDataSet(entries, "");
                                dataSet.setColors(Arrays.asList(
                                        Color.parseColor("#1976D2"), // azul
                                        Color.parseColor("#F48FB1"), // Rosa claro (Light Pink)
                                        Color.parseColor("#388E3C"), // verde oscuro
                                        Color.parseColor("#F57C00"), // naranja
                                        Color.parseColor("#D32F2F"), // rojo
                                        Color.parseColor("#7B1FA2"), // morado
                                        Color.parseColor("#FBC02D"), // amarillo oscuro
                                        Color.parseColor("#0288D1"), // azul claro
                                        Color.parseColor("#00796B"), // verde azulado
                                        Color.parseColor("#5D4037"), // marrón
                                        Color.parseColor("#C2185B")  // rosa oscuro
                                ));
                                dataSet.setValueTextSize(14f);
                                dataSet.setValueTextColor(Color.BLACK);
                                dataSet.setSliceSpace(2f);

                                PieData pieData = new PieData(dataSet);
                                pieData.setValueFormatter(new PercentFormatter(binding.pieChartClientes));

                                binding.pieChartClientes.setUsePercentValues(true);
                                binding.pieChartClientes.setData(pieData);
                                binding.pieChartClientes.setDrawEntryLabels(true);
                                binding.pieChartClientes.setEntryLabelColor(Color.BLACK);
                                binding.pieChartClientes.setEntryLabelTextSize(12f);
                                binding.pieChartClientes.setDrawCenterText(false);
                                binding.pieChartClientes.getLegend().setEnabled(false);
                                binding.pieChartClientes.getDescription().setEnabled(false);
                                binding.pieChartClientes.invalidate();
                            });
                });
    }

    private void cargarGraficoGruposPorMonitor() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("grupos")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Integer> conteoPorMonitorId = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String idMonitor = doc.getString("idMonitor");

                        if (idMonitor != null && !idMonitor.isEmpty()) {
                            conteoPorMonitorId.put(idMonitor, conteoPorMonitorId.getOrDefault(idMonitor, 0) + 1);
                        }
                    }

                    List<String> idsMonitorConGrupos = new ArrayList<>(conteoPorMonitorId.keySet());

                    if (idsMonitorConGrupos.isEmpty()) {
                        Toast.makeText(getContext(), "No hay monitores con grupos asignados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("monitores")
                            .whereIn("idMonitor", idsMonitorConGrupos)
                            .get()
                            .addOnSuccessListener(monitoresSnapshot -> {
                                Map<String, String> idToNombre = new HashMap<>();

                                for (QueryDocumentSnapshot monitorDoc : monitoresSnapshot) {
                                    String id = monitorDoc.getString("idMonitor");
                                    String nombre = monitorDoc.getString("nombre") + " " + monitorDoc.getString("apellidos");
                                    idToNombre.put(id, nombre);
                                }

                                mostrarGraficoGruposPorMonitor(idToNombre, conteoPorMonitorId);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error al cargar monitores", Toast.LENGTH_SHORT).show());
                });
    }

    private void mostrarGraficoGruposPorMonitor(Map<String, String> idToNombre, Map<String, Integer> conteo) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> nombresMonitores = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            String idMonitor = entry.getKey();
            int cantidad = entry.getValue();

            if (!idToNombre.containsKey(idMonitor)) continue;

            String nombreCompleto = idToNombre.get(idMonitor);
            String nombreCorto = nombreCompleto.split(" ")[0] + " " + nombreCompleto.split(" ")[1].charAt(0) + ".";

            entries.add(new BarEntry(index, cantidad));
            nombresMonitores.add(nombreCorto);
            index++;
        }

        if (entries.isEmpty()) {
            Toast.makeText(getContext(), "No hay datos para mostrar en el gráfico", Toast.LENGTH_SHORT).show();
            binding.barChartGruposPorMonitor.clear();
            binding.barChartGruposPorMonitor.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Grupos por monitor");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);


        binding.barChartGruposPorMonitor.setData(barData);
        binding.barChartGruposPorMonitor.setFitBars(true);
        binding.barChartGruposPorMonitor.setDrawValueAboveBar(true);
        binding.barChartGruposPorMonitor.getDescription().setEnabled(false);

        Legend legend = binding.barChartGruposPorMonitor.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = binding.barChartGruposPorMonitor.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < nombresMonitores.size()) ? nombresMonitores.get(i) : "";
            }
        });
        binding.barChartGruposPorMonitor.setExtraBottomOffset(32f);
        binding.barChartGruposPorMonitor.invalidate();
    }

    private void exportarGraficoComoImagen(Chart<?> chart, String nombreBase) {
        chart.setDrawingCacheEnabled(true);
        Bitmap bitmap = chart.getChartBitmap();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nombreArchivo = nombreBase + "_" + timeStamp + ".png";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(getContext(), "Gráfico guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al guardar gráfico", Toast.LENGTH_SHORT).show();
        }
    }


    private void exportarDatosClientesPorGrupoComoCSV() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("grupos")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(gruposSnapshot -> {
                    Map<String, String> idToNombreGrupo = new HashMap<>();
                    Map<String, Integer> conteoPorGrupoId = new HashMap<>();

                    for (QueryDocumentSnapshot grupoDoc : gruposSnapshot) {
                        String idGrupo = grupoDoc.getId();
                        String nombreGrupo = grupoDoc.getString("nombre");
                        if (nombreGrupo != null) {
                            idToNombreGrupo.put(idGrupo, nombreGrupo);
                            conteoPorGrupoId.put(idGrupo, 0);
                        }
                    }

                    db.collection("clientes")
                            .whereEqualTo("idAdministrador", uid)
                            .get()
                            .addOnSuccessListener(clientesSnapshot -> {
                                for (QueryDocumentSnapshot clienteDoc : clientesSnapshot) {
                                    List<String> gruposSeleccionados = (List<String>) clienteDoc.get("gruposSeleccionados");

                                    if (gruposSeleccionados != null) {
                                        for (String idGrupo : gruposSeleccionados) {
                                            if (conteoPorGrupoId.containsKey(idGrupo)) {
                                                conteoPorGrupoId.put(idGrupo, conteoPorGrupoId.get(idGrupo) + 1);
                                            }
                                        }
                                    }
                                }

                                StringBuilder csvBuilder = new StringBuilder();
                                csvBuilder.append("Grupo,Inscripciones\n");
                                for (Map.Entry<String, Integer> entry : conteoPorGrupoId.entrySet()) {
                                    String nombreGrupo = idToNombreGrupo.get(entry.getKey());
                                    int cantidad = entry.getValue();
                                    if (cantidad > 0) {
                                        csvBuilder.append(nombreGrupo).append(",").append(cantidad).append("\n");
                                    }
                                }

                                guardarCSVEnDescargas("inscripciones_por_grupo.csv", csvBuilder.toString());
                            });
                });
    }

    private void exportarDatosGruposPorMonitorComoCSV() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("grupos")
                .whereEqualTo("idAdministrador", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Integer> monitorConteo = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String nombreCompleto = doc.getString("idMonitor");
                        String soloNombre = (nombreCompleto != null && !nombreCompleto.isEmpty())
                                ? nombreCompleto.split(" ")[0]
                                : "Sin asignar";

                        monitorConteo.put(soloNombre, monitorConteo.getOrDefault(soloNombre, 0) + 1);
                    }

                    StringBuilder csvBuilder = new StringBuilder();
                    csvBuilder.append("Monitor,Grupos asignados\n");
                    for (Map.Entry<String, Integer> entry : monitorConteo.entrySet()) {
                        csvBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
                    }

                    guardarCSVEnDescargas("grupos_por_monitor.csv", csvBuilder.toString());
                });
    }


    private void guardarCSVEnDescargas(String nombreArchivo, String contenido) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    out.write(contenido.getBytes());
                    Toast.makeText(getContext(), "CSV guardado correctamente", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error al guardar CSV", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(contenido.getBytes());
                Toast.makeText(getContext(), "CSV exportado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error al guardar CSV", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
