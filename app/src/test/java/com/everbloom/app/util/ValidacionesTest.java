package com.everbloom.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Pruebas unitarias de las reglas de validación de los formularios de la app. */
public class ValidacionesTest {

    @Test
    public void correoValidoNoTieneError() {
        assertEquals("", Validaciones.correo("sara@everbloom.com"));
    }

    @Test
    public void correoVacioEsObligatorio() {
        assertEquals("Escribe tu correo.", Validaciones.correo("  "));
    }

    @Test
    public void correoSinArrobaNoEsValido() {
        assertEquals("El correo no tiene un formato válido.", Validaciones.correo("esto-no-es-un-correo"));
    }

    @Test
    public void contrasenaCortaNoEsValida() {
        assertEquals("La contraseña debe tener mínimo 6 caracteres.", Validaciones.contrasena("123"));
        assertEquals("", Validaciones.contrasena("clave123"));
    }

    @Test
    public void confirmarContrasenaDebeCoincidir() {
        assertEquals("", Validaciones.confirmar("clave123", "clave123"));
        assertEquals("Las contraseñas no coinciden.", Validaciones.confirmar("clave123", "otra"));
    }

    @Test
    public void campoObligatorioVacio() {
        assertEquals("El campo nombres es obligatorio.", Validaciones.obligatorio("", "nombres"));
    }

    @Test
    public void cantidadDentroDelInventario() {
        assertEquals("", Validaciones.cantidad("2", 20));
    }

    @Test
    public void cantidadMayorAlInventario() {
        assertEquals("Solo hay 20 disponibles.", Validaciones.cantidad("22", 20));
    }

    @Test
    public void cantidadCeroOTextoNoSonValidos() {
        assertEquals("La cantidad debe ser mayor a cero.", Validaciones.cantidad("0", 20));
        assertEquals("La cantidad debe ser un número entero.", Validaciones.cantidad("dos", 20));
    }

    @Test
    public void precioEnPesosColombianos() {
        assertTrue(Formato.precio(55000).matches("\\$ 55[.\\u00a0 ]000"));
    }
}
