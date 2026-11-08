package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.DetalleConOrigen
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    val items: Flow<List<Item>>
    val favoritos: Flow<List<Item>>

    suspend fun refrescar(): Result<Int>
    suspend fun obtenerDetalle(id: Int): Result<Item>
    // Conserva el contrato remoto anterior para implementaciones de prueba y otros consumidores.
    suspend fun obtenerDetalleConRespaldo(id: Int): Result<DetalleConOrigen> =
        obtenerDetalle(id).map { DetalleConOrigen(it, OrigenDetalle.REMOTO) }
    suspend fun alternarFavorito(id: Int)
    suspend fun cantidadGuardada(): Int
    suspend fun cantidadFavoritos(): Int
    suspend fun limpiarCache()
}
