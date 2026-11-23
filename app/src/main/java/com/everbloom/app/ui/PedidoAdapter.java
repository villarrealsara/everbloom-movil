package com.everbloom.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.everbloom.app.R;
import com.everbloom.app.modelo.Pedido;
import com.everbloom.app.util.Formato;

import java.util.ArrayList;
import java.util.List;

/** Adaptador de la lista de pedidos: une cada Pedido con su tarjeta (item_pedido.xml). */
public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.Tarjeta> {
    private final List<Pedido> pedidos = new ArrayList<>();

    public void actualizar(List<Pedido> nuevos) {
        pedidos.clear();
        pedidos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Tarjeta onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        return new Tarjeta(LayoutInflater.from(padre.getContext()).inflate(R.layout.item_pedido, padre, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Tarjeta t, int posicion) {
        Pedido p = pedidos.get(posicion);
        t.lblNumero.setText(String.format("#EVB-%05d", p.getIdPedido()));
        t.lblFecha.setText(p.getFecha());
        t.lblTotal.setText(Formato.precio(p.getTotal()));
        t.lblEstado.setText(p.getEstado());
    }

    @Override
    public int getItemCount() { return pedidos.size(); }

    static class Tarjeta extends RecyclerView.ViewHolder {
        final TextView lblNumero, lblFecha, lblTotal, lblEstado;
        Tarjeta(View v) {
            super(v);
            lblNumero = v.findViewById(R.id.lblNumero);
            lblFecha = v.findViewById(R.id.lblFecha);
            lblTotal = v.findViewById(R.id.lblTotal);
            lblEstado = v.findViewById(R.id.lblEstado);
        }
    }
}
