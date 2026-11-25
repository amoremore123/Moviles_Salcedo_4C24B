package com.tecsup.avisoscampus.notificacion

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tecsup.avisoscampus.MainActivity
import com.tecsup.avisoscampus.R
import com.tecsup.avisoscampus.model.Aviso

// Todo lo que toca NotificationManager vive aquí, fuera del ViewModel
object Notificador {

    const val CANAL = "avisos"
    const val CANAL_URGENTE = "urgentes"

    // Desde Android 8 (API 26) toda notificación necesita un canal.
    // Crear un canal que ya existe no hace nada, así que es seguro llamarlo siempre.
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL,
                "Avisos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // IMPORTANCE_HIGH: suena y aparece como notificación emergente
            val urgente = NotificationChannel(
                CANAL_URGENTE,
                "Urgentes",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
            manager.createNotificationChannel(urgente)
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

    // Devuelve false (sin error) si no hay permiso; true si la notificación se mostró
    @SuppressLint("MissingPermission") // el permiso ya se comprueba con tienePermiso
    fun mostrar(context: Context, aviso: Aviso): Boolean {
        if (!tienePermiso(context)) return false

        // Al tocar la notificación, Android abre MainActivity en nombre de la app
        val intent = Intent(context, MainActivity::class.java)
        val pendiente = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // nadie puede modificar el intent después
        )

        // Android 8+ usa la importancia del canal; la prioridad sirve en versiones anteriores
        val canal = if (aviso.urgente) CANAL_URGENTE else CANAL
        val prioridad = if (aviso.urgente) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto) // vista colapsada: una línea
            // Vista expandida: el texto completo en varias líneas
            .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.texto))
            .setPriority(prioridad)
            .setContentIntent(pendiente)
            .setAutoCancel(true) // se quita al tocarla
            .build()

        // Cada aviso usa su propio id, así no se reemplazan entre sí
        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}
