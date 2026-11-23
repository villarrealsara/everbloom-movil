package com.everbloom.app.ui;

import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import android.widget.EditText;

import com.everbloom.app.R;
import com.everbloom.app.util.Validaciones;

import org.json.JSONObject;

/** Pantalla Crear cuenta: registra una clienta nueva con POST /api/auth/registro. */
public class CrearCuentaActivity extends BaseActivity {
    private EditText txtNombres, txtApellidos, txtCorreo, txtTelefono, txtClave, txtConfirmar;
    private AppCompatButton btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);
        txtNombres = findViewById(R.id.txtNombres);
        txtApellidos = findViewById(R.id.txtApellidos);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtClave = findViewById(R.id.txtContrasena);
        txtConfirmar = findViewById(R.id.txtConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> registrar());
        findViewById(R.id.lnkLogin).setOnClickListener(v -> finish());
    }

    /** Marca el error en el campo y devuelve true si había error. */
    private boolean marcar(EditText campo, String error) {
        campo.setError(error.isEmpty() ? null : error);
        return !error.isEmpty();
    }

    private void registrar() {
        boolean hayError = marcar(txtNombres, Validaciones.obligatorio(txtNombres.getText().toString(), "nombres"));
        hayError |= marcar(txtApellidos, Validaciones.obligatorio(txtApellidos.getText().toString(), "apellidos"));
        hayError |= marcar(txtCorreo, Validaciones.correo(txtCorreo.getText().toString()));
        hayError |= marcar(txtClave, Validaciones.contrasena(txtClave.getText().toString()));
        hayError |= marcar(txtConfirmar, Validaciones.confirmar(txtClave.getText().toString(), txtConfirmar.getText().toString()));
        if (hayError) return;

        btnRegistrar.setEnabled(false);
        llamarApi(api -> {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("nombres", txtNombres.getText().toString().trim());
            cuerpo.put("apellidos", txtApellidos.getText().toString().trim());
            cuerpo.put("correo", txtCorreo.getText().toString().trim());
            cuerpo.put("contrasena", txtClave.getText().toString());
            String tel = txtTelefono.getText().toString().trim();
            if (!tel.isEmpty()) cuerpo.put("telefono", tel);
            return api.post("/auth/registro", cuerpo);
        }, r -> {
            btnRegistrar.setEnabled(true);
            if (!r.esExitosa()) { avisar(r.mensajeError()); return; }
            avisar("Cuenta creada correctamente. Ahora inicia sesión.");
            finish();
        });
    }
}
