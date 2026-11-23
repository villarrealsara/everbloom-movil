package com.everbloom.app.modelo;

import org.json.JSONObject;

/** Producto del catálogo, tal como lo devuelve GET /api/productos. */
public class Producto {
    private final int idProducto;
    private final String nombre;
    private final String categoria;
    private final double precio;
    private final int cantidad;

    public Producto(int idProducto, String nombre, String categoria, double precio, int cantidad) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    /** Convierte un objeto JSON de la API en un Producto. */
    public static Producto desdeJson(JSONObject json) {
        return new Producto(
                json.optInt("idProducto"),
                json.optString("nombreProducto"),
                json.optString("nombreCategoria"),
                json.optDouble("precio", 0),
                json.optInt("cantidad", 0));
    }

    public int getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public boolean estaAgotado() { return cantidad <= 0; }
}
