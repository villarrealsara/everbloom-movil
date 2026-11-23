package com.everbloom.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everbloom.app.R;
import com.everbloom.app.modelo.Producto;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla Catálogo: banner, buscador, filtro por color y cuadrícula de dos columnas
 * con los productos de GET /api/productos (igual al prototipo app-catalogo.html).
 */
public class CatalogoActivity extends BaseActivity {
    private ProductoAdapter adaptador;
    private TextView lblVacio, lblTituloLista;
    private EditText txtBuscar;
    private NestedScrollView scroll;
    private View chipActivo;
    private String busquedaActual = "";
    private String colorElegido; // null = sin color escogido

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        TextView lblSaludo = findViewById(R.id.lblSaludo);
        lblSaludo.setText("Hola, " + sesion().getNombres());
        lblVacio = findViewById(R.id.lblVacio);
        lblTituloLista = findViewById(R.id.lblTituloLista);
        scroll = findViewById(R.id.scrollCatalogo);

        RecyclerView lista = findViewById(R.id.listaProductos);
        lista.setLayoutManager(new GridLayoutManager(this, 2)); // dos columnas, como el prototipo
        adaptador = new ProductoAdapter(p -> {
            Intent intent = new Intent(this, PedidoActivity.class);
            intent.putExtra("idProducto", p.getIdProducto());
            if (colorElegido != null) intent.putExtra("color", colorElegido);
            startActivity(intent);
        });
        lista.setAdapter(adaptador);

        // Buscar al presionar la lupa del teclado
        txtBuscar = findViewById(R.id.txtBuscar);
        txtBuscar.setOnEditorActionListener((v, accion, e) -> {
            if (accion == EditorInfo.IME_ACTION_SEARCH) { filtrar(null, txtBuscar.getText().toString().trim()); return true; }
            return false;
        });

        // Filtro por color: todos los arreglos se hacen en cualquier color de satín,
        // así que el chip deja el color escogido para la pantalla de personalizar.
        findViewById(R.id.chipRosa).setOnClickListener(v -> elegirColor(v, "Rosa Coral"));
        findViewById(R.id.chipRojo).setOnClickListener(v -> elegirColor(v, "Rojo Pasión"));
        findViewById(R.id.chipBlanco).setOnClickListener(v -> elegirColor(v, "Blanco Nube"));
        findViewById(R.id.lnkVerTodo).setOnClickListener(v -> verTodo());
        findViewById(R.id.btnVerColeccion).setOnClickListener(v -> verTodo());

        configurarPestanas(getIntent().getBooleanExtra("verTodo", false) ? TIENDA : INICIO);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean tienda = intent.getBooleanExtra("verTodo", false);
        configurarPestanas(tienda ? TIENDA : INICIO);
        if (tienda) verTodo(); else scroll.smoothScrollTo(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar(busquedaActual); // al volver de un pedido se recarga el inventario
    }

    @Override
    protected void alTocarPestana(int pestana) {
        if (pestana == INICIO) { configurarPestanas(INICIO); scroll.smoothScrollTo(0, 0); }
        else if (pestana == TIENDA) { configurarPestanas(TIENDA); verTodo(); }
        else super.alTocarPestana(pestana);
    }

    private void verTodo() {
        txtBuscar.setText("");
        filtrar(null, "");
        scroll.post(() -> scroll.smoothScrollTo(0, lblTituloLista.getTop()));
    }

    /** Marca el chip del color y muestra todos los arreglos en ese color. */
    private void elegirColor(View chip, String color) {
        if (chipActivo != null) chipActivo.setBackgroundResource(R.drawable.chip_color);
        chipActivo = chip;
        chip.setBackgroundResource(R.drawable.chip_color_activo);
        colorElegido = color;
        txtBuscar.setText("");
        busquedaActual = "";
        lblTituloLista.setText("Arreglos en satín " + color);
        cargar("");
        scroll.post(() -> scroll.smoothScrollTo(0, lblTituloLista.getTop()));
    }

    /** Busca por nombre del producto (GET /api/productos?busqueda=). */
    private void filtrar(View chip, String texto) {
        if (chipActivo != null) chipActivo.setBackgroundResource(R.drawable.chip_color);
        chipActivo = null;
        colorElegido = null;
        lblTituloLista.setText(texto.isEmpty() ? "Nuestros arreglos" : "Resultados de \u201C" + texto + "\u201D");
        busquedaActual = texto;
        cargar(texto);
    }

    private void cargar(String busqueda) {
        llamarApi(api -> api.get(busqueda.isEmpty() ? "/productos"
                : "/productos?busqueda=" + URLEncoder.encode(busqueda, "UTF-8")), r -> {
            if (!r.esExitosa()) { avisar(r.mensajeError()); return; }
            try {
                JSONArray arreglo = new JSONArray(r.cuerpo);
                List<Producto> productos = new ArrayList<>();
                for (int i = 0; i < arreglo.length(); i++) productos.add(Producto.desdeJson(arreglo.getJSONObject(i)));
                adaptador.actualizar(productos);
                lblVacio.setText(R.string.sin_productos);
                lblVacio.setVisibility(productos.isEmpty() ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                avisar("No se pudo leer el catálogo.");
            }
        });
    }
}
