package com.tecsup.campusgps.data

import com.tecsup.campusgps.model.Posicion

interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>

    // El ViewModel lo consulta antes de buscar, sin necesitar un Context
    fun playServicesDisponible(): Boolean
}

/** La ubicación está apagada o no se pudo determinar (getCurrentLocation devolvió null). */
class UbicacionNoDisponibleException : Exception("Activa el GPS e intenta de nuevo")
