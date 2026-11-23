package com.everbloom.app.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.everbloom.app.R;
import com.everbloom.app.datos.ApiCliente;
import com.everbloom.app.datos.SesionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase base de las pantallas: reúne lo que todas usan (sesión, cliente de la API
 * y la forma de hacer peticiones en segundo plano). Así no se repite código.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Tarea que se ejecuta en segundo plano y devuelve una respuesta de la API. */
    protected interface Tarea { ApiCliente.Respuesta ejecutar(ApiCliente api) throws Exception; }
    /** Qué hacer con la respuesta, ya de vuelta en la pantalla. */
    protected interface AlTerminar { void con(ApiCliente.Respuesta respuesta); }

    private final ExecutorService hilos = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());

    /** Permiso de red local que exige Android 17 para conectarse a la API del computador. */
    private static final String PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 37
                && ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ PERMISO_RED_LOCAL }, 37);
        }
    }

    protected SesionManager sesion() { return new SesionManager(this); }

    /** Android no deja usar la red en el hilo de la pantalla, por eso se usa otro hilo. */
    protected void llamarApi(Tarea tarea, AlTerminar alTerminar) {
        ApiCliente api = new ApiCliente(sesion().getToken());
        hilos.execute(() -> {
            ApiCliente.Respuesta r;
            try { r = tarea.ejecutar(api); }
            catch (Exception e) { r = null; }
            final ApiCliente.Respuesta respuesta = r;
            principal.post(() -> {
                if (respuesta == null) { avisar(getString(R.string.error_conexion)); return; }
                if (respuesta.codigo == 401 && sesion().haySesion()) { // token vencido
                    sesion().cerrar();
                    avisar("Tu sesión terminó. Inicia sesión de nuevo.");
                    irA(LoginActivity.class, true);
                    return;
                }
                alTerminar.con(respuesta);
            });
        });
    }

    protected void avisar(String mensaje) { Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show(); }

    protected void irA(Class<?> pantalla, boolean cerrarEsta) {
        Intent intent = new Intent(this, pantalla);
        if (cerrarEsta) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (cerrarEsta) finish();
    }

    /** Pestañas de la barra inferior (barra_pestanas.xml). */
    protected static final int INICIO = 0, TIENDA = 1, PEDIDOS = 2, PERFIL = 3;

    /** Activa la barra inferior y marca la pestaña de la pantalla actual. */
    protected void configurarPestanas(int activa) {
        int[] ids = { R.id.tabInicio, R.id.tabTienda, R.id.tabPedidos, R.id.tabPerfil };
        int[] textos = { R.id.tabInicioTxt, R.id.tabTiendaTxt, R.id.tabPedidosTxt, R.id.tabPerfilTxt };
        for (int i = 0; i < ids.length; i++) {
            View tab = findViewById(ids[i]);
            TextView texto = findViewById(textos[i]);
            boolean esActiva = i == activa;
            tab.setBackgroundResource(esActiva ? R.drawable.tab_activa : 0);
            texto.setTextColor(getColor(esActiva ? R.color.eb_primary_dark : R.color.eb_text_light));
            final int pestana = i;
            tab.setOnClickListener(v -> alTocarPestana(pestana));
        }
    }

    /** Qué pasa al tocar una pestaña. Catálogo lo sobrescribe para Inicio y Tienda. */
    protected void alTocarPestana(int pestana) {
        if (pestana == INICIO || pestana == TIENDA) {
            Intent i = new Intent(this, CatalogoActivity.class);
            i.putExtra("verTodo", pestana == TIENDA);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        } else if (pestana == PEDIDOS) {
            if (!(this instanceof MisPedidosActivity)) irA(MisPedidosActivity.class, false);
        } else {
            mostrarPerfil();
        }
    }

    /** Perfil: muestra quién inició sesión y permite cerrar la sesión. */
    protected void mostrarPerfil() {
        new AlertDialog.Builder(this)
                .setTitle("Mi perfil")
                .setMessage("Hola, " + sesion().getNombres() + "\nRol: " + sesion().getRol())
                .setPositiveButton("Cerrar sesión", (d, w) -> {
                    sesion().cerrar();
                    avisar("Cerraste sesión. ¡Vuelve pronto!");
                    irA(LoginActivity.class, true);
                })
                .setNegativeButton("Seguir comprando", null)
                .show();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); hilos.shutdown(); }
}
