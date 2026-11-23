package com.everbloom.app.datos;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP de la API de EVERBLOOM.
 * Usa HttpURLConnection (viene con Android, no necesita librerías extra).
 * 10.0.2.2 es la dirección del computador vista desde el emulador.
 */
public class ApiCliente {

    public static final String URL_BASE = "http://10.0.2.2:3000/api";

    /** Respuesta simple: código HTTP y texto del cuerpo. */
    public static class Respuesta {
        public final int codigo;
        public final String cuerpo;
        Respuesta(int codigo, String cuerpo) { this.codigo = codigo; this.cuerpo = cuerpo; }
        public boolean esExitosa() { return codigo >= 200 && codigo < 300; }

        /** Mensaje de error que manda la API, por ejemplo "Correo o contraseña incorrectos." */
        public String mensajeError() {
            try {
                JSONObject json = new JSONObject(cuerpo);
                String error = json.optString("error", "Ocurrió un error.");
                if (json.has("detalles")) {
                    StringBuilder sb = new StringBuilder(error);
                    for (int i = 0; i < json.getJSONArray("detalles").length(); i++) {
                        sb.append("\n• ").append(json.getJSONArray("detalles").getString(i));
                    }
                    return sb.toString();
                }
                return error;
            } catch (Exception e) {
                return "Ocurrió un error (" + codigo + ").";
            }
        }
    }

    private final String token;

    /** @param token token JWT de la sesión, o null si no hay sesión. */
    public ApiCliente(String token) { this.token = token; }

    public Respuesta get(String ruta) throws Exception { return enviar("GET", ruta, null); }

    public Respuesta post(String ruta, JSONObject cuerpo) throws Exception { return enviar("POST", ruta, cuerpo); }

    private Respuesta enviar(String metodo, String ruta, JSONObject cuerpo) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) new URL(URL_BASE + ruta).openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setConnectTimeout(8000);
        conexion.setReadTimeout(8000);
        conexion.setRequestProperty("Accept", "application/json");
        if (token != null) conexion.setRequestProperty("Authorization", "Bearer " + token);
        if (cuerpo != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        int codigo = conexion.getResponseCode();
        InputStream entrada = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
        String texto = "";
        if (entrada != null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) sb.append(linea);
            }
            texto = sb.toString();
        }
        conexion.disconnect();
        return new Respuesta(codigo, texto);
    }
}
