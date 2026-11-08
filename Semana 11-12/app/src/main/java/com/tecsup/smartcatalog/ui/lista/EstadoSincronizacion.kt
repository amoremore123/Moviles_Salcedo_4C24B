package com.tecsup.smartcatalog.ui.lista

/** Describe una consulta real; no infiere disponibilidad del servidor a partir de la red del dispositivo. */
sealed interface EstadoSincronizacion {
    data object Inicial : EstadoSincronizacion
    data class Actualizando(val falloAnterior: String? = null) : EstadoSincronizacion
    data object Correcta : EstadoSincronizacion
    data class Fallo(val mensaje: String) : EstadoSincronizacion
}
