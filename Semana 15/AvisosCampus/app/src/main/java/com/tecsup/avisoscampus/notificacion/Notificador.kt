package com.tecsup.avisoscampus.notificacion

import android.content.Context
import com.tecsup.avisoscampus.model.Aviso

// Todo lo que toca NotificationManager vive aquí, fuera del ViewModel
object Notificador {

    const val CANAL = "avisos"

    fun crearCanal(context: Context) {
        // TODO Paso 1: crear el NotificationChannel CANAL (Android 8+)
    }

    fun mostrar(context: Context, aviso: Aviso) {
        // TODO Paso 8: construir y mostrar la notificación del aviso
    }
}
