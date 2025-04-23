package edu.pmdm.gympro.ui.grupos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Grupo;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<Grupo> listaOriginal;
    private List<Grupo> listaFiltrada;
    private final OnGrupoClickListener listener;

    public interface OnGrupoClickListener {
        void onGrupoClick(Grupo grupo);
    }

    public GrupoAdapter(List<Grupo> grupos, OnGrupoClickListener listener) {
        this.listaOriginal = grupos;
        this.listaFiltrada = new ArrayList<>(grupos);
        this.listener = listener;
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo, parent, false);
        return new GrupoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        Grupo grupo = listaFiltrada.get(position);
        holder.tvNombreGrupo.setText(grupo.getNombre());

        holder.itemView.setOnClickListener(v -> listener.onGrupoClick(grupo));
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
            for (Grupo g : listaOriginal) {
                if (g.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    listaFiltrada.add(g);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void actualizarLista(List<Grupo> nuevosGrupos) {
        listaOriginal.clear();
        listaOriginal.addAll(nuevosGrupos);
        listaFiltrada.clear();
        listaFiltrada.addAll(nuevosGrupos);
        notifyDataSetChanged();
    }

    static class GrupoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreGrupo = itemView.findViewById(R.id.tvNombreGrupo);
        }
    }
}
