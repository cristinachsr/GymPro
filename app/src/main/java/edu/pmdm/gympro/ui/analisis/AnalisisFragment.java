package edu.pmdm.gympro.ui.analisis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
                                    List<String> clasesSeleccionadas = (List<String>) clienteDoc.get("clasesSeleccionadas");
                                    if (clasesSeleccionadas != null) {
                                        for (String idGrupo : clasesSeleccionadas) {
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
                                binding.pieChartClientes.getLegend().setEnabled(false); // ❌ Ocultar leyenda
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
                    Map<String, String> idToNombreMonitor = new HashMap<>();
                    Map<String, Integer> monitorConteo = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String nombreCompleto = doc.getString("id_empleado"); // contiene nombre y apellidos

                        // Extraer solo el primer nombre
                        String soloNombre = (nombreCompleto != null && !nombreCompleto.isEmpty())
                                ? nombreCompleto.split(" ")[0]
                                : "Sin asignar";

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            monitorConteo.put(soloNombre, monitorConteo.getOrDefault(soloNombre, 0) + 1);
                        }
                    }

                    List<IBarDataSet> dataSets = new ArrayList<>();
                    int index = 0;

                    for (Map.Entry<String, Integer> entry : monitorConteo.entrySet()) {
                        List<BarEntry> barEntries = new ArrayList<>();
                        barEntries.add(new BarEntry(index, entry.getValue()));

                        BarDataSet set = new BarDataSet(barEntries, entry.getKey()); // solo nombre
                        set.setColor(ColorTemplate.COLORFUL_COLORS[index % ColorTemplate.COLORFUL_COLORS.length]);
                        set.setValueTextSize(10f);
                        dataSets.add(set);
                        index++;
                    }

                    BarData barData = new BarData(dataSets);
                    barData.setBarWidth(0.5f); // barras más finas
                    binding.barChartGruposPorMonitor.setData(barData);

                    // Configurar leyenda (abajo, alineada izquierda y pequeña)
                    Legend legend = binding.barChartGruposPorMonitor.getLegend();
                    legend.setEnabled(true);
                    legend.setForm(Legend.LegendForm.SQUARE);
                    legend.setTextSize(10f);
                    legend.setFormSize(10f);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false);

                    // Eje X sin etiquetas (porque están en leyenda)
                    binding.barChartGruposPorMonitor.getXAxis().setDrawLabels(false);
                    binding.barChartGruposPorMonitor.getXAxis().setGranularity(1f);
                    binding.barChartGruposPorMonitor.getXAxis().setGranularityEnabled(true);

                    binding.barChartGruposPorMonitor.setFitBars(true);
                    binding.barChartGruposPorMonitor.setDrawValueAboveBar(true);
                    binding.barChartGruposPorMonitor.getDescription().setEnabled(false);

                    binding.barChartGruposPorMonitor.invalidate();
                });
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
                                    List<String> clasesSeleccionadas = (List<String>) clienteDoc.get("clasesSeleccionadas");

                                    if (clasesSeleccionadas != null) {
                                        for (String idGrupo : clasesSeleccionadas) {
                                            if (conteoPorGrupoId.containsKey(idGrupo)) {
                                                conteoPorGrupoId.put(idGrupo, conteoPorGrupoId.get(idGrupo) + 1);
                                            }
                                        }
                                    }
                                }

                                // Crear archivo CSV
                                StringBuilder csvBuilder = new StringBuilder();
                                csvBuilder.append("Grupo,Inscripciones\n");
                                for (Map.Entry<String, Integer> entry : conteoPorGrupoId.entrySet()) {
                                    String nombreGrupo = idToNombreGrupo.get(entry.getKey());
                                    int cantidad = entry.getValue();
                                    if (cantidad > 0) {
                                        csvBuilder.append(nombreGrupo).append(",").append(cantidad).append("\n");
                                    }
                                }

                                try {
                                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "inscripciones_por_grupo.csv");
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(csvBuilder.toString().getBytes());
                                    fos.close();
                                    Toast.makeText(getContext(), "CSV exportado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), "Error al guardar CSV", Toast.LENGTH_SHORT).show();
                                }
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
                        String nombreCompleto = doc.getString("id_empleado");

                        String soloNombre = (nombreCompleto != null && !nombreCompleto.isEmpty())
                                ? nombreCompleto.split(" ")[0]
                                : "Sin asignar";

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            monitorConteo.put(soloNombre, monitorConteo.getOrDefault(soloNombre, 0) + 1);
                        }
                    }

                    // Construir CSV
                    StringBuilder csvBuilder = new StringBuilder();
                    csvBuilder.append("Monitor,Grupos asignados\n");
                    for (Map.Entry<String, Integer> entry : monitorConteo.entrySet()) {
                        csvBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
                    }

                    try {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "grupos_por_monitor.csv");
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(csvBuilder.toString().getBytes());
                        fos.close();
                        Toast.makeText(getContext(), "CSV exportado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error al guardar CSV", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
