package edu.pmdm.gympro.ui.clientes;

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
import edu.pmdm.gympro.model.Cliente;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {

    private final List<Cliente> listaOriginal;
    private final List<Cliente> listaFiltrada;
    private final OnClienteClickListener listener;

    public interface OnClienteClickListener {
        void onClienteClick(Cliente cliente);
    }

    public ClienteAdapter(List<Cliente> clientes, OnClienteClickListener listener) {
        this.listaOriginal = clientes;
        this.listaFiltrada = new ArrayList<>(clientes);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = listaFiltrada.get(position);
        holder.tvNombre.setText(cliente.getNombre() + " " + cliente.getApellidos());
        holder.tvCorreo.setText(cliente.getCorreo());

        // Cargar imagen
        if (cliente.getFoto() != null && !cliente.getFoto().equals("logo_por_defecto")) {
            Glide.with(holder.itemView.getContext())
                    .load(cliente.getFoto())
                    .placeholder(R.drawable.logo_gympro_sinfondo)
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.logo_gympro_sinfondo);
        }

        holder.itemView.setOnClickListener(v -> listener.onClienteClick(cliente));
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void actualizarLista(List<Cliente> nuevosClientes) {
        listaOriginal.clear();
        listaOriginal.addAll(nuevosClientes);

        listaFiltrada.clear();
        listaFiltrada.addAll(nuevosClientes);

        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            for (Cliente c : listaOriginal) {
                String nombreCompleto = (c.getNombre() + " " + c.getApellidos()).toLowerCase();
                if (nombreCompleto.contains(texto.toLowerCase())) {
                    listaFiltrada.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo;
        ImageView ivFoto;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreCliente);
            tvCorreo = itemView.findViewById(R.id.tvCorreoCliente);
            ivFoto = itemView.findViewById(R.id.ivFotoCliente);
        }
    }
}
