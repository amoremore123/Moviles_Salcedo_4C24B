package com.tecsup.campusgps.data

import com.tecsup.campusgps.model.Lugar

/**
 * Los 5 lugares del campus, ubicados dentro del terreno de Tecsup Lima (Santa Anita).
 * Mejora 5: las posiciones son ficticias, pero caen dentro del campus real
 * (aprox. lat -12.0459 a -12.0421, lon -76.9545 a -76.9513).
 */
class LugaresRepositoryLocal : LugaresRepository {

    private val lugares = listOf(
        Lugar(1, "Biblioteca Central", "Estudio", -12.0440, -76.9528),
        Lugar(2, "Cafetería Campus", "Comida", -12.0446, -76.9522),
        Lugar(3, "Laboratorio de Cómputo", "Estudio", -12.0434, -76.9535),
        Lugar(4, "Auditorio Principal", "Eventos", -12.0452, -76.9532),
        Lugar(5, "Estacionamiento Norte", "Servicios", -12.0425, -76.9525)
    )

    override fun obtenerLugares(): List<Lugar> = lugares
}
