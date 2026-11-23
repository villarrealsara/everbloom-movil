package com.everbloom.app.datos;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda el token y los datos básicos del usuario en el celular (SharedPreferences). */
public class SesionManager {
    private static final String ARCHIVO = "eb_sesion";
    private final SharedPreferences prefs;

    public SesionManager(Context contexto) {
        prefs = contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public void guardar(String token, String nombres, String rol) {
        prefs.edit().putString("token", token).putString("nombres", nombres).putString("rol", rol).apply();
    }

    public String getToken() { return prefs.getString("token", null); }
    public String getNombres() { return prefs.getString("nombres", ""); }
    public String getRol() { return prefs.getString("rol", ""); }
    public boolean haySesion() { return getToken() != null; }
    public void cerrar() { prefs.edit().clear().apply(); }
}
