package com.tecsup.avisoscampus.notificacion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.tecsup.avisoscampus.model.Aviso

// Todo lo que toca NotificationManager vive aquí, fuera del ViewModel
object Notificador {

    const val CANAL = "avisos"

    // Desde Android 8 (API 26) toda notificación necesita un canal.
    // Crear un canal que ya existe no hace nada, así que es seguro llamarlo siempre.
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL,
                "Avisos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    // Antes de Android 13 el permiso se concede al instalar; desde la API 33 hay que pedirlo
    fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun mostrar(context: Context, aviso: Aviso) {
        // TODO Paso 8: construir y mostrar la notificación del aviso
    }
}
