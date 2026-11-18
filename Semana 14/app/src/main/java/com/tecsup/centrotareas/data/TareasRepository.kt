package com.tecsup.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    suspend fun contarPrimos(hasta: Int): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>

    // Mejora 1: la tarea simulada emite su avance de 0f a 1f
    fun progresoTarea(): Flow<Float>
}
