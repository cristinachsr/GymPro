package edu.pmdm.gympro.ui.grupos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

        // Mostrar descripción del grupo
        holder.tvDescripcionGrupo.setText(grupo.getDescripcion() != null ? grupo.getDescripcion() : "Sin descripción");

        // Mostrar foto del grupo
        if (grupo.getPhoto() != null && !grupo.getPhoto().isEmpty() && !grupo.getPhoto().equals("logo_por_defecto")) {
            Glide.with(holder.itemView.getContext())
                    .load(grupo.getPhoto())
                    .placeholder(R.drawable.logo_gympro_sinfondo)
                    .into(holder.ivFotoGrupo);
        } else {
            holder.ivFotoGrupo.setImageResource(R.drawable.logo_gympro_sinfondo);
        }

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
        TextView tvNombreGrupo, tvDescripcionGrupo;
        ImageView ivFotoGrupo;

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreGrupo = itemView.findViewById(R.id.tvNombreGrupo);
            tvDescripcionGrupo = itemView.findViewById(R.id.tvDescripcionGrupo);
            ivFotoGrupo = itemView.findViewById(R.id.ivFotoGrupo);
        }
    }
}
