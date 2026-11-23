package com.everbloom.app.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Da formato a los precios en pesos colombianos, por ejemplo $ 55.000. */
public final class Formato {
    private Formato() { }

    public static String precio(double valor) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
        return "$ " + nf.format(Math.round(valor)).replace(',', '.');
    }
}
