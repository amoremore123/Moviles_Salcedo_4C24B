package com.tecsup.campusgps.util

import java.util.Locale

/**
 * Función pura: menos de 1000 m -> metros enteros ("350 m");
 * desde 1000 m -> kilómetros con un decimal ("1.5 km").
 */
fun formatearDistancia(metros: Float): String =
    if (metros < 1000f) {
        "${metros.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", metros / 1000f)
    }
