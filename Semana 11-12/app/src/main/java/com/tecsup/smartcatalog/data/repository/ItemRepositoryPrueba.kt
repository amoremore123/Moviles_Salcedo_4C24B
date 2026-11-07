package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.DatosPrueba
import com.tecsup.smartcatalog.domain.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Colección ficticia en memoria. No realiza sincronización ni persistencia. */
class ItemRepositoryPrueba(datosIniciales: List<Item> = DatosPrueba.smartphones) : ItemRepository {
    private val ejemplos = datosIniciales.toList()
    private val elementos = MutableStateFlow(ejemplos)

    override val items: Flow<List<Item>> = elementos.asStateFlow()
    override val favoritos: Flow<List<Item>> = elementos.map { lista -> lista.filter { it.favorito } }

    /** Restaura los ejemplos en memoria; el resultado es su cantidad, no una descarga. */
    override suspend fun refrescar(): Result<Int> {
        elementos.value = ejemplos
        return Result.success(ejemplos.size)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Item> =
        elementos.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("No se encontró el producto de ejemplo."))

    // Cumple el contrato solo en memoria; H4 no expone una acción de favoritos en la interfaz.
    override suspend fun alternarFavorito(id: Int) {
        elementos.update { lista ->
            lista.map { item -> if (item.id == id) item.copy(favorito = !item.favorito) else item }
        }
    }

    override suspend fun cantidadGuardada(): Int = elementos.value.size
    override suspend fun cantidadFavoritos(): Int = elementos.value.count { it.favorito }

    /** Vacía únicamente la colección en memoria; no existe una caché en disco. */
    override suspend fun limpiarCache() {
        elementos.value = emptyList()
    }
}
