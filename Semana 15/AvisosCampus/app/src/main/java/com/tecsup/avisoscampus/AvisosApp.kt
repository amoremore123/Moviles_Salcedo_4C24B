package com.tecsup.avisoscampus

import android.app.Application
import com.tecsup.avisoscampus.notificacion.Notificador

// Se ejecuta una sola vez al arrancar el proceso de la app, antes que MainActivity
class AvisosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}
