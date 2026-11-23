package com.everbloom.app.util;

import java.util.regex.Pattern;

/**
 * Reglas de validación de los formularios. No depende de Android,
 * por eso se puede probar con pruebas unitarias normales (JUnit).
 * Son las mismas reglas que valida la API.
 */
public final class Validaciones {
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Validaciones() { }

    /** Devuelve el mensaje de error, o una cadena vacía si el dato es válido. */
    public static String correo(String valor) {
        if (valor == null || valor.trim().isEmpty()) return "Escribe tu correo.";
        if (!CORREO.matcher(valor.trim()).matches()) return "El correo no tiene un formato válido.";
        return "";
    }

    public static String contrasena(String valor) {
        if (valor == null || valor.isEmpty()) return "Escribe tu contraseña.";
        if (valor.length() < 6) return "La contraseña debe tener mínimo 6 caracteres.";
        return "";
    }

    public static String obligatorio(String valor, String campo) {
        return (valor == null || valor.trim().isEmpty()) ? "El campo " + campo + " es obligatorio." : "";
    }

    public static String confirmar(String clave, String confirmacion) {
        return clave != null && clave.equals(confirmacion) ? "" : "Las contraseñas no coinciden.";
    }

    /** La cantidad debe ser un entero entre 1 y lo disponible en inventario. */
    public static String cantidad(String valor, int disponibles) {
        int n;
        try { n = Integer.parseInt(valor == null ? "" : valor.trim()); }
        catch (NumberFormatException e) { return "La cantidad debe ser un número entero."; }
        if (n < 1) return "La cantidad debe ser mayor a cero.";
        if (n > disponibles) return "Solo hay " + disponibles + " disponibles.";
        return "";
    }
}
