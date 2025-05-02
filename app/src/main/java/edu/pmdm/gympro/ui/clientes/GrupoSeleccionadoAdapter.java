package edu.pmdm.gympro.ui.clientes;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.pmdm.gympro.R;

public class GrupoSeleccionadoAdapter extends RecyclerView.Adapter<GrupoSeleccionadoAdapter.ViewHolder> {

    private final List<String> listaGrupos;

    public GrupoSeleccionadoAdapter(List<String> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    @NonNull
    @Override
    public GrupoSeleccionadoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grupo_seleccionado, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvNombreGrupo.setText(listaGrupos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreGrupo;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombreGrupo = itemView.findViewById(R.id.tvNombreGrupoSeleccionado);
        }
    }
}
