package com.everbloom.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everbloom.app.R;
import com.everbloom.app.modelo.Pedido;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Pantalla Mis pedidos: lista los pedidos de la clienta con GET /api/pedidos. */
public class MisPedidosActivity extends BaseActivity {
    private PedidoAdapter adaptador;
    private TextView lblVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pedidos);
        lblVacio = findViewById(R.id.lblVacio);
        RecyclerView lista = findViewById(R.id.listaPedidos);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new PedidoAdapter();
        lista.setAdapter(adaptador);
        configurarPestanas(PEDIDOS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        llamarApi(api -> api.get("/pedidos"), r -> {
            if (!r.esExitosa()) { avisar(r.mensajeError()); return; }
            try {
                JSONArray arreglo = new JSONArray(r.cuerpo);
                List<Pedido> pedidos = new ArrayList<>();
                for (int i = 0; i < arreglo.length(); i++) pedidos.add(Pedido.desdeJson(arreglo.getJSONObject(i)));
                adaptador.actualizar(pedidos);
                lblVacio.setText(R.string.sin_pedidos);
                lblVacio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
            } catch (Exception e) { avisar("No se pudieron leer los pedidos."); }
        });
    }
}
