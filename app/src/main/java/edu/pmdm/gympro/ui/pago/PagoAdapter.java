package edu.pmdm.gympro.ui.pago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import edu.pmdm.gympro.model.Pago;
import edu.pmdm.gympro.R;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoViewHolder> {
    private List<Pago> listaPagos;
    private Map<String, String> nombresClientes;

    public PagoAdapter(List<Pago> listaPagos, Map<String, String> nombresClientes) {
        this.listaPagos = listaPagos;
        this.nombresClientes = nombresClientes;
    }

    public static class PagoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPago;

        public PagoViewHolder(View itemView) {
            super(itemView);
            tvPago = itemView.findViewById(android.R.id.text1);
        }
    }

    @NonNull
    @Override
    public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new PagoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
        Pago pago = listaPagos.get(position);
        String nombre = nombresClientes.getOrDefault(pago.getIdCliente(), "Desconocido");
        holder.tvPago.setText("✓ " + nombre);
    }

    @Override
    public int getItemCount() {
        return listaPagos.size();
    }

    public void actualizarDatos(List<Pago> nuevaLista, Map<String, String> nuevosNombres) {
        this.listaPagos = nuevaLista;
        this.nombresClientes = nuevosNombres;
        notifyDataSetChanged();
    }
}
