package com.tecsup.centrotareas.data

import com.tecsup.centrotareas.model.ResultadoPrimos
import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    // Mejora 2: alAvanzar recibe el avance del cálculo (0f a 1f)
    suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora 1: la tarea simulada emite su avance de 0f a 1f
    fun progresoTarea(): Flow<Float>

    // Mejora 5: cuenta y suma los primos de 2 a hasta en una sola pasada
    suspend fun contarYSumarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): ResultadoPrimos
}
