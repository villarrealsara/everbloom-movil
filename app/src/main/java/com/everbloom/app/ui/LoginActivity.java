package com.everbloom.app.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import android.widget.EditText;
import android.widget.TextView;

import com.everbloom.app.R;
import com.everbloom.app.util.Validaciones;

import org.json.JSONObject;

/** Pantalla Iniciar sesión: valida los datos y los envía a POST /api/auth/login. */
public class LoginActivity extends BaseActivity {
    private EditText txtCorreo, txtContrasena;
    private AppCompatButton btnAcceder;
    private TextView lblError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtContrasena = findViewById(R.id.txtContrasena);
        btnAcceder = findViewById(R.id.btnAcceder);
        lblError = findViewById(R.id.lblError);
        TextView lnkCrear = findViewById(R.id.lnkCrearCuenta);

        btnAcceder.setOnClickListener(v -> iniciarSesion());
        lnkCrear.setOnClickListener(v -> irA(CrearCuentaActivity.class, false));
    }

    private void iniciarSesion() {
        String correo = txtCorreo.getText().toString().trim();
        String clave = txtContrasena.getText().toString();
        String errCorreo = Validaciones.correo(correo);
        String errClave = clave.isEmpty() ? "Escribe tu contraseña." : "";
        txtCorreo.setError(errCorreo.isEmpty() ? null : errCorreo);
        txtContrasena.setError(errClave.isEmpty() ? null : errClave);
        if (!errCorreo.isEmpty() || !errClave.isEmpty()) return;

        lblError.setVisibility(View.GONE);
        btnAcceder.setEnabled(false);
        btnAcceder.setText(R.string.cargando);
        llamarApi(api -> {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("correo", correo);
            cuerpo.put("contrasena", clave);
            return api.post("/auth/login", cuerpo);
        }, r -> {
            btnAcceder.setEnabled(true);
            btnAcceder.setText(R.string.acceder);
            if (!r.esExitosa()) { // el aviso rojo de arriba, como en el prototipo
                lblError.setText(r.mensajeError());
                lblError.setVisibility(View.VISIBLE);
                return;
            }
            try {
                JSONObject json = new JSONObject(r.cuerpo);
                JSONObject usuario = json.getJSONObject("usuario");
                sesion().guardar(json.getString("token"), usuario.optString("nombres"), usuario.optString("rol"));
                avisar("¡Bienvenida, " + usuario.optString("nombres") + "!");
                irA(CatalogoActivity.class, true);
            } catch (Exception e) {
                avisar("La respuesta del servidor no es válida.");
            }
        });
    }
}
