package edu.pmdm.gympro.ui.clientes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.pmdm.gympro.R;
import edu.pmdm.gympro.model.Cliente;

public class ClienteGrupoAdapter extends RecyclerView.Adapter<ClienteGrupoAdapter.ClienteViewHolder> {

    private Context context;
    private List<Cliente> listaClientes;

    public ClienteGrupoAdapter(Context context, List<Cliente> listaClientes) {
        this.context = context;
        this.listaClientes = listaClientes;
    }

    @NonNull
    @Override
    public ClienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cliente, parent, false);
        return new ClienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteViewHolder holder, int position) {
        Cliente cliente = listaClientes.get(position);
        holder.tvNombreCliente.setText(cliente.getNombre() + " " + cliente.getApellidos());
        holder.tvCorreoCliente.setText(cliente.getCorreo());

        // Cargar imagen con Glide si la URL de la foto no es nula ni vacía
        if (cliente.getFoto() != null && !cliente.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(cliente.getFoto())
                    .placeholder(R.drawable.logo_gympro_sinfondo)
                    .into(holder.ivFotoCliente);
        } else {
            holder.ivFotoCliente.setImageResource(R.drawable.logo_gympro_sinfondo);
        }
    }

    @Override
    public int getItemCount() {
        return listaClientes.size();
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCliente, tvCorreoCliente;
        ImageView ivFotoCliente;

        public ClienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreCliente = itemView.findViewById(R.id.tvNombreCliente);
            tvCorreoCliente = itemView.findViewById(R.id.tvCorreoCliente);
            ivFotoCliente = itemView.findViewById(R.id.ivFotoCliente);
        }
    }
}