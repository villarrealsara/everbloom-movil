package com.everbloom.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.everbloom.app.R;
import com.everbloom.app.modelo.Empaque;
import com.everbloom.app.modelo.Producto;
import com.everbloom.app.util.Formato;
import com.everbloom.app.util.Validaciones;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla Personaliza tu arreglo (prototipo app-personalizar.html): la clienta escoge
 * cantidad, color del satín, empaque y dedicatoria. Al tocar "Continuar" pasa a Confirmar.
 * El producto y los empaques vienen de GET /api/productos/{id} y GET /api/empaques.
 */
public class PedidoActivity extends BaseActivity {
    private Producto producto;
    private final List<Empaque> empaques = new ArrayList<>();
    private int cantidad = 1;
    private String color = "Rosa Coral";

    private TextView lblProducto, lblDisponibles, lblCantidad, lblPrecioSeleccion, lblResumen, lblTotal, lblContador;
    private EditText txtMensaje;
    private Spinner selEmpaque;
    private final int[] satines = { R.id.satinRosa, R.id.satinRojo, R.id.satinBlanco, R.id.satinNude };
    private final String[] nombresSatin = { "Rosa Coral", "Rojo Pasión", "Blanco Nube", "Nude Eterno" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);
        lblProducto = findViewById(R.id.lblProducto);
        lblDisponibles = findViewById(R.id.lblDisponibles);
        lblCantidad = findViewById(R.id.lblCantidad);
        lblPrecioSeleccion = findViewById(R.id.lblPrecioSeleccion);
        lblResumen = findViewById(R.id.lblResumen);
        lblTotal = findViewById(R.id.lblTotal);
        lblContador = findViewById(R.id.lblContador);
        txtMensaje = findViewById(R.id.txtMensaje);
        selEmpaque = findViewById(R.id.selEmpaque);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        findViewById(R.id.btnMenos).setOnClickListener(v -> cambiarCantidad(-1));
        findViewById(R.id.btnMas).setOnClickListener(v -> cambiarCantidad(+1));
        findViewById(R.id.btnContinuar).setOnClickListener(v -> continuar());

        // Color del satín: si viene escogido desde el catálogo se respeta
        String desdeCatalogo = getIntent().getStringExtra("color");
        if (desdeCatalogo != null) color = desdeCatalogo;
        for (int i = 0; i < satines.length; i++) {
            final String nombre = nombresSatin[i];
            findViewById(satines[i]).setOnClickListener(v -> { color = nombre; pintarSatines(); actualizar(); });
        }
        pintarSatines();

        txtMensaje.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { lblContador.setText(s.length() + " / 200"); }
        });
        selEmpaque.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { actualizar(); }
            public void onNothingSelected(AdapterView<?> p) { }
        });

        cargarProducto(getIntent().getIntExtra("idProducto", 0));
        cargarEmpaques();
    }

    private void cargarProducto(int id) {
        llamarApi(api -> api.get("/productos/" + id), r -> {
            if (!r.esExitosa()) { avisar(r.mensajeError()); finish(); return; }
            try {
                producto = Producto.desdeJson(new JSONObject(r.cuerpo));
                lblProducto.setText(producto.getNombre() + " · " + Formato.precio(producto.getPrecio()) + " c/u");
                lblDisponibles.setText("Disponibles: " + producto.getCantidad());
                actualizar();
            } catch (Exception e) { avisar("No se pudo leer el producto."); }
        });
    }

    private void cargarEmpaques() {
        llamarApi(api -> api.get("/empaques"), r -> {
            List<String> opciones = new ArrayList<>();
            opciones.add("Sin empaque");
            if (r.esExitosa()) {
                try {
                    JSONArray arreglo = new JSONArray(r.cuerpo);
                    for (int i = 0; i < arreglo.length(); i++) {
                        Empaque e = Empaque.desdeJson(arreglo.getJSONObject(i));
                        empaques.add(e);
                        opciones.add(e.getNombre());
                    }
                } catch (Exception ignorado) { /* se deja solo "Sin empaque" */ }
            }
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selEmpaque.setAdapter(a);
        });
    }

    /** Botones − y +: la cantidad no baja de 1 ni pasa de lo que hay en inventario. */
    private void cambiarCantidad(int cambio) {
        if (producto == null) return;
        int nueva = cantidad + cambio;
        if (nueva < 1) return;
        if (nueva > producto.getCantidad()) { avisar("Solo hay " + producto.getCantidad() + " disponibles."); return; }
        cantidad = nueva;
        actualizar();
    }

    /** Resalta el satín escogido con borde coral. */
    private void pintarSatines() {
        for (int i = 0; i < satines.length; i++) {
            findViewById(satines[i]).setBackgroundResource(
                    nombresSatin[i].equals(color) ? R.drawable.satin_activo : R.drawable.satin);
        }
    }

    private String empaqueElegido() {
        int pos = selEmpaque.getSelectedItemPosition();
        return pos > 0 ? empaques.get(pos - 1).getNombre() : "Sin empaque";
    }

    /** Recalcula precio, resumen y total cada vez que algo cambia. */
    private void actualizar() {
        if (producto == null) return;
        double total = producto.getPrecio() * cantidad;
        lblCantidad.setText(String.valueOf(cantidad));
        lblPrecioSeleccion.setText(Formato.precio(total));
        lblTotal.setText(Formato.precio(total));
        lblResumen.setText(cantidad + " × " + producto.getNombre() + "\nSatín " + color + " · " + empaqueElegido());
    }

    /** Valida y pasa a la pantalla Confirmar pedido con lo que se escogió. */
    private void continuar() {
        if (producto == null) return;
        String error = Validaciones.cantidad(String.valueOf(cantidad), producto.getCantidad());
        if (!error.isEmpty()) { avisar(error); return; }
        Intent i = new Intent(this, ConfirmarActivity.class);
        i.putExtra("idProducto", producto.getIdProducto());
        i.putExtra("nombre", producto.getNombre());
        i.putExtra("precio", producto.getPrecio());
        i.putExtra("cantidad", cantidad);
        i.putExtra("color", color);
        int pos = selEmpaque.getSelectedItemPosition();
        i.putExtra("idEmpaque", pos > 0 ? empaques.get(pos - 1).getIdEmpaque() : 0);
        i.putExtra("empaque", empaqueElegido());
        i.putExtra("mensaje", txtMensaje.getText().toString().trim());
        startActivity(i);
    }
}
