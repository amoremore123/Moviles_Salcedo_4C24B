package com.tecsup.avisoscampus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Escucha el anuncio del sistema "cambió el modo avión".
// onReceive debe ser corto: solo lee el estado y lo entrega a quien lo registró.
class ModoAvionReceiver(
    private val alCambiar: (Boolean) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            // "state" = true si el modo avión quedó activado
            alCambiar(intent.getBooleanExtra("state", false))
        }
    }
}
