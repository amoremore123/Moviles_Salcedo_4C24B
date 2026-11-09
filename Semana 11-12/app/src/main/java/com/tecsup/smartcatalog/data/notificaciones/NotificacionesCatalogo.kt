package com.tecsup.smartcatalog.data.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tecsup.smartcatalog.MainActivity
import com.tecsup.smartcatalog.R

fun interface NotificadorCatalogo {
    fun publicar(cantidad: Int)
}

internal fun permiteAviso(sdk: Int, permiso: Boolean, habilitadas: Boolean): Boolean =
    habilitadas && (sdk < Build.VERSION_CODES.TIRAMISU || permiso)

class NotificacionesCatalogo(context: Context) : NotificadorCatalogo {
    private val app = context.applicationContext
    private val manager = app.getSystemService(NotificationManager::class.java)
    private val compat = NotificationManagerCompat.from(app)
    private val preferencias = app.getSharedPreferences("avisos_catalogo", Context.MODE_PRIVATE)

    fun avisosHabilitados(): Boolean {
        val permiso = ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permiteAviso(Build.VERSION.SDK_INT, permiso, compat.areNotificationsEnabled())) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            manager.getNotificationChannel(CANAL)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun puedeSolicitarPermiso(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED &&
            !preferencias.getBoolean(SOLICITADO, false)

    fun registrarSolicitud() {
        preferencias.edit().putBoolean(SOLICITADO, true).apply()
    }

    internal fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        manager.createNotificationChannel(
            NotificationChannel(CANAL, app.getString(R.string.canal_catalogo), NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = app.getString(R.string.canal_catalogo_descripcion)
            }
        )
    }

    override fun publicar(cantidad: Int) {
        if (cantidad <= 0) return
        try {
            if (!avisosHabilitados()) return
            crearCanal()
            // Una entrada nueva vuelve a verificar la sesión y no recibe una ruta privilegiada.
            val abrir = Intent(app, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val destino = PendingIntent.getActivity(app, 0, abrir, PendingIntent.FLAG_IMMUTABLE)
            val aviso = NotificationCompat.Builder(app, CANAL)
                .setSmallIcon(R.drawable.ic_aviso_catalogo)
                .setContentTitle(app.getString(R.string.aviso_catalogo_titulo))
                .setContentText(app.resources.getQuantityString(R.plurals.aviso_catalogo_productos, cantidad, cantidad))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(destino)
                .setAutoCancel(true)
                .build()
            // El aviso más reciente ocupa una sola posición en la bandeja.
            compat.notify(ID_AVISO, aviso)
        } catch (error: RuntimeException) {
            // El aviso es complementario y no cambia el resultado ya guardado en Room.
            Log.w("NotificacionesCatalogo", "No se pudo publicar el aviso local.", error)
        }
    }

    companion object {
        const val CANAL = "smartcatalog_catalog_updates"
        const val ID_AVISO = 1501
        private const val SOLICITADO = "solicitud_realizada"
    }
}
