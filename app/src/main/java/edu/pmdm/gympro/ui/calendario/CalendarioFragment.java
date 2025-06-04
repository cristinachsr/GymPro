package edu.pmdm.gympro.ui.calendario;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.pmdm.gympro.R;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvDescripcion;

    private final List<String> diasSemana = Arrays.asList(
            "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    );

    private Map<Integer, List<String>> clasesPorDiaSemana = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);
        calendarView = view.findViewById(R.id.calendarView);
        tvDescripcion = view.findViewById(R.id.tvDescription);

        cargarHorarios();

        calendarView.setOnDayClickListener(eventDay -> {
            int diaSemana = eventDay.getCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            List<String> clases = clasesPorDiaSemana.get(diaSemana);
            if (clases != null && !clases.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String clase : clases) {
                    sb.append("- ").append(clase).append("\n");
                }
                tvDescripcion.setText(sb.toString());
            } else {
                tvDescripcion.setText("No hay clases este día.");
            }
        });

        return view;
    }

    private void cargarHorarios() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidAdmin = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("grupos")
                .whereEqualTo("idAdministrador", uidAdmin)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<Integer> diasMarcados = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombreGrupo = doc.getString("nombre");
                        List<Map<String, Object>> horarios = (List<Map<String, Object>>) doc.get("horarios");

                        if (horarios != null) {
                            for (Map<String, Object> horario : horarios) {
                                String diaTexto = horario.get("dia").toString();
                                int index = diasSemana.indexOf(diaTexto);
                                if (index != -1) {
                                    String horaInicio = horario.get("horaInicio").toString();
                                    String horaFin = horario.get("horaFin").toString();
                                    String textoClase = nombreGrupo + ": " + horaInicio + " - " + horaFin;

                                    diasMarcados.add(index);
                                    clasesPorDiaSemana
                                            .computeIfAbsent(index, k -> new ArrayList<>())
                                            .add(textoClase);
                                }
                            }
                        }
                    }

                    List<EventDay> eventos = new ArrayList<>();
                    Calendar inicio = Calendar.getInstance();
                    inicio.set(Calendar.DAY_OF_MONTH, 1);
                    Calendar fin = (Calendar) inicio.clone();
                    fin.add(Calendar.MONTH, 2);

                    while (inicio.before(fin)) {
                        int diaActual = inicio.get(Calendar.DAY_OF_WEEK) - 1;
                        if (diasMarcados.contains(diaActual)) {
                            eventos.add(new EventDay((Calendar) inicio.clone(), R.drawable.circle_blue));
                        }
                        inicio.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    calendarView.setEvents(eventos);
                })
                .addOnFailureListener(e -> tvDescripcion.setText("Error al cargar datos."));
    }
}
