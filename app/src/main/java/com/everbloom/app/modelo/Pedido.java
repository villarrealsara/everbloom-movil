package com.everbloom.app.modelo;

import org.json.JSONObject;

/** Resumen de un pedido, tal como lo devuelve GET /api/pedidos. */
public class Pedido {
    private final int idPedido;
    private final String fecha;
    private final String estado;
    private final double total;

    public Pedido(int idPedido, String fecha, String estado, double total) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
    }

    public static Pedido desdeJson(JSONObject json) {
        String fecha = json.optString("fechaPedido");
        if (fecha.length() >= 10) fecha = fecha.substring(0, 10); // solo AAAA-MM-DD
        return new Pedido(json.optInt("idPedido"), fecha,
                json.optString("estadoPedido"), json.optDouble("total", 0));
    }

    public int getIdPedido() { return idPedido; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public double getTotal() { return total; }
}
