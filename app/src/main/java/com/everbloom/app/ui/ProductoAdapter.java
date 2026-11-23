package com.everbloom.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.everbloom.app.R;
import com.everbloom.app.modelo.Producto;
import com.everbloom.app.util.Formato;

import java.util.ArrayList;
import java.util.List;

/** Adaptador de la cuadrícula del catálogo: une cada Producto con su tarjeta (item_producto.xml). */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.Tarjeta> {

    public interface AlElegir { void producto(Producto p); }

    /** Las fotos del prototipo; se reparten según el id del producto. */
    private static final int[] FOTOS = { R.drawable.producto1, R.drawable.producto2, R.drawable.producto3, R.drawable.producto4 };

    private final List<Producto> productos = new ArrayList<>();
    private final AlElegir alElegir;

    public ProductoAdapter(AlElegir alElegir) { this.alElegir = alElegir; }

    public static int fotoDe(int idProducto) { return FOTOS[Math.abs(idProducto - 1) % FOTOS.length]; }

    public void actualizar(List<Producto> nuevos) {
        productos.clear();
        productos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Tarjeta onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext()).inflate(R.layout.item_producto, padre, false);
        return new Tarjeta(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Tarjeta t, int posicion) {
        Producto p = productos.get(posicion);
        t.imgProducto.setImageResource(fotoDe(p.getIdProducto()));
        t.lblNombre.setText(p.getNombre());
        t.lblPrecio.setText(Formato.precio(p.getPrecio()) + " · " + p.getCategoria());
        if (p.estaAgotado()) {
            t.lblAgotado.setVisibility(View.VISIBLE);
            t.lblAccion.setText("No disponible");
            t.itemView.setAlpha(0.6f);
            t.itemView.setOnClickListener(null);
        } else {
            t.lblAgotado.setVisibility(View.GONE);
            t.lblAccion.setText("Personalizar →");
            t.itemView.setAlpha(1f);
            t.itemView.setOnClickListener(v -> alElegir.producto(p));
        }
    }

    @Override
    public int getItemCount() { return productos.size(); }

    static class Tarjeta extends RecyclerView.ViewHolder {
        final ImageView imgProducto;
        final TextView lblNombre, lblPrecio, lblAccion, lblAgotado;
        Tarjeta(View v) {
            super(v);
            imgProducto = v.findViewById(R.id.imgProducto);
            lblNombre = v.findViewById(R.id.lblNombre);
            lblPrecio = v.findViewById(R.id.lblPrecio);
            lblAccion = v.findViewById(R.id.lblAccion);
            lblAgotado = v.findViewById(R.id.lblAgotado);
        }
    }
}
