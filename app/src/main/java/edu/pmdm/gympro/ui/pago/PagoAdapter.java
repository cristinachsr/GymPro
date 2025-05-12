package edu.pmdm.gympro.ui.pago;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.pmdm.gympro.model.Pago;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoViewHolder> {
    private List<Pago> listaPagos;

    public PagoAdapter(List<Pago> listaPagos) {
        this.listaPagos = listaPagos;
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
        holder.tvPago.setText("✓ " + pago.getNombreCliente());
    }

    @Override
    public int getItemCount() {
        return listaPagos.size();
    }

    public void actualizarLista(List<Pago> nuevaLista) {
        this.listaPagos = nuevaLista;
        notifyDataSetChanged();
    }
}