package com.tecsup.campusgps.data

import kotlinx.coroutines.flow.Flow

/** Mejora 3: recuerda el último lugar que el usuario seleccionó. */
interface PreferenciasRepository {
    // Emite el id guardado, o null si todavía no hay ninguno
    val ultimoLugar: Flow<Int?>

    suspend fun guardarUltimoLugar(id: Int)
}
