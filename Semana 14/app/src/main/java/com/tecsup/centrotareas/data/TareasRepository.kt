package com.tecsup.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    // Mejora 2: alAvanzar recibe el avance del cálculo (0f a 1f)
    suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora 1: la tarea simulada emite su avance de 0f a 1f
    fun progresoTarea(): Flow<Float>
}
