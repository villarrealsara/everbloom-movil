package com.everbloom.app.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.everbloom.app.R;
import com.everbloom.app.util.Formato;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Pantalla Confirmar pedido (prototipo app-confirmar-pedido.html).
 * Muestra los datos de la clienta (GET /api/auth/perfil) y el resumen; al confirmar
 * guarda el pedido con POST /api/pedidos y muestra el aviso de pedido confirmado.
 */
public class ConfirmarActivity extends BaseActivity {
    private AppCompatButton btnConfirmar;
    private String correo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar);
        Intent datos = getIntent();
        String nombre = datos.getStringExtra("nombre");
        double precio = datos.getDoubleExtra("precio", 0);
        int cantidad = datos.getIntExtra("cantidad", 1);
        String mensaje = datos.getStringExtra("mensaje");

        ((TextView) findViewById(R.id.lblProducto)).setText(nombre);
        ((TextView) findViewById(R.id.lblDetalle)).setText(cantidad + " × " + Formato.precio(precio)
                + "\nSatín " + datos.getStringExtra("color")
                + (mensaje == null || mensaje.isEmpty() ? "" : "\nDedicatoria: “" + mensaje + "”"));
        ((TextView) findViewById(R.id.lblSubtotal)).setText(Formato.precio(precio * cantidad));
        ((TextView) findViewById(R.id.lblEmpaque)).setText(datos.getStringExtra("empaque"));
        ((TextView) findViewById(R.id.lblTotal)).setText(Formato.precio(precio * cantidad));

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> confirmar());
        cargarCliente();
    }

    private void cargarCliente() {
        llamarApi(api -> api.get("/auth/perfil"), r -> {
            if (!r.esExitosa()) return;
            try {
                JSONObject u = new JSONObject(r.cuerpo).getJSONObject("usuario");
                correo = u.optString("correo");
                String tel = u.optString("telefono", "");
                ((TextView) findViewById(R.id.lblCliente)).setText(u.optString("nombres") + " " + u.optString("apellidos"));
                ((TextView) findViewById(R.id.lblCorreo)).setText(correo);
                ((TextView) findViewById(R.id.lblTelefono)).setText(tel.isEmpty() || tel.equals("null") ? "Sin teléfono" : tel);
            } catch (Exception ignorado) { /* se dejan los puntos */ }
        });
    }

    private void confirmar() {
        Intent datos = getIntent();
        btnConfirmar.setEnabled(false);
        btnConfirmar.setText(R.string.cargando);
        llamarApi(api -> {
            JSONObject item = new JSONObject();
            item.put("idProducto", datos.getIntExtra("idProducto", 0));
            item.put("cantidad", datos.getIntExtra("cantidad", 1));
            int idEmpaque = datos.getIntExtra("idEmpaque", 0);
            if (idEmpaque > 0) item.put("idEmpaque", idEmpaque);
            item.put("color", datos.getStringExtra("color"));
            String mensaje = datos.getStringExtra("mensaje");
            if (mensaje != null && !mensaje.isEmpty()) item.put("mensajeTarjeta", mensaje);
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("items", new JSONArray().put(item));
            return api.post("/pedidos", cuerpo);
        }, r -> {
            btnConfirmar.setEnabled(true);
            btnConfirmar.setText(R.string.confirmar_pedido);
            if (!r.esExitosa()) { avisar(r.mensajeError()); return; }
            int idPedido = 0;
            try { idPedido = new JSONObject(r.cuerpo).optInt("idPedido"); } catch (Exception ignorado) { }
            mostrarConfirmado(idPedido);
        });
    }

    /** Aviso "¡Tu pedido ha sido confirmado!" igual al del prototipo. */
    private void mostrarConfirmado(int idPedido) {
        View vista = getLayoutInflater().inflate(R.layout.dialog_confirmado, null);
        ((TextView) vista.findViewById(R.id.lblNumero)).setText(String.format("#EVB-%05d", idPedido));
        ((TextView) vista.findViewById(R.id.lblMensaje)).setText(
                "Te enviaremos los detalles a\n" + (correo.isEmpty() ? "tu correo" : correo));
        AlertDialog aviso = new AlertDialog.Builder(this).setView(vista).setCancelable(false).create();
        vista.findViewById(R.id.btnVolverCatalogo).setOnClickListener(v -> {
            aviso.dismiss();
            Intent i = new Intent(this, CatalogoActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
        aviso.show();
        if (aviso.getWindow() != null) aviso.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
