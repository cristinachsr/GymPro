package edu.pmdm.gympro.ui.monitores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Monitor;

public class MonitorAdapter extends RecyclerView.Adapter<MonitorAdapter.MonitorViewHolder> {

    private final List<Monitor> listaOriginal;
    private final List<Monitor> listaFiltrada;
    private final OnMonitorClickListener listener;

    public interface OnMonitorClickListener {
        void onMonitorClick(Monitor monitor);
    }

    public MonitorAdapter(List<Monitor> monitores, OnMonitorClickListener listener) {
        this.listaOriginal = monitores;
        this.listaFiltrada = new ArrayList<>(monitores);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monitor, parent, false);
        return new MonitorViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MonitorViewHolder holder, int position) {
        Monitor monitor = listaFiltrada.get(position);
        holder.tvNombreMonitor.setText(monitor.getNombre() + " " + monitor.getApellidos());

        holder.itemView.setOnClickListener(v -> listener.onMonitorClick(monitor)); // <-- CLICK
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void filtrar(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            for (Monitor m : listaOriginal) {
                if ((m.getNombre() + " " + m.getApellidos()).toLowerCase().contains(texto.toLowerCase())) {
                    listaFiltrada.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void actualizarLista(List<Monitor> nuevosMonitores) {
        listaOriginal.clear();
        listaOriginal.addAll(nuevosMonitores);

        listaFiltrada.clear();
        listaFiltrada.addAll(nuevosMonitores);

        notifyDataSetChanged();
    }

    static class MonitorViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreMonitor;

        public MonitorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreMonitor = itemView.findViewById(R.id.tvNombreMonitor);
        }
    }
}
