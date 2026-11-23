package com.everbloom.app.modelo;

import org.json.JSONObject;

/** Empaque de regalo que se puede escoger en el pedido. */
public class Empaque {
    private final int idEmpaque;
    private final String nombre;
    private final double precioExtra;

    public Empaque(int idEmpaque, String nombre, double precioExtra) {
        this.idEmpaque = idEmpaque;
        this.nombre = nombre;
        this.precioExtra = precioExtra;
    }

    public static Empaque desdeJson(JSONObject json) {
        return new Empaque(json.optInt("idEmpaque"), json.optString("nombreEmpaque"),
                json.optDouble("precioExtra", 0));
    }

    public int getIdEmpaque() { return idEmpaque; }
    public String getNombre() { return nombre; }
    public double getPrecioExtra() { return precioExtra; }
}
