package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.mapper.toItemSeguro
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.domain.model.Item
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Productos de la API retenidos solo durante la vida del repositorio. Sin almacenamiento en disco. */
class ItemRepositoryRemoto(
    private val api: ApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ItemRepository {
    private val memoria = MutableStateFlow<List<Item>>(emptyList())
    private val consulta = Mutex()
    override val items: StateFlow<List<Item>> = memoria.asStateFlow()
    override val favoritos: Flow<List<Item>> = memoria.map { lista -> lista.filter { it.favorito } }

    override suspend fun refrescar(): Result<Int> = withContext(dispatcher) {
        consulta.withLock {
            try {
                val recibidos = requireNotNull(api.obtenerSmartphones().products)
                val productos = recibidos.mapNotNull { it?.toItemSeguro()?.getOrNull() }.distinctBy { it.id }
                require(recibidos.isEmpty() || productos.isNotEmpty())
                memoria.value = productos
                Result.success(productos.size)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Result.failure(errorDeConsulta(error))
            }
        }
    }

    /** Consulta el segundo endpoint incluso cuando el producto ya figura en la lista en memoria. */
    override suspend fun obtenerDetalle(id: Int): Result<Item> {
        if (id <= 0) return Result.failure(IllegalArgumentException("El identificador del smartphone no es válido."))
        return withContext(dispatcher) {
            try {
                val producto = api.obtenerDetalle(id).toItemSeguro().getOrThrow()
                require(producto.id == id) { "El ID de la respuesta no coincide con el solicitado." }
                // El detalle no sustituye ni modifica la colección publicada del catálogo.
                Result.success(producto)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Result.failure(errorDeConsulta(error, detalle = true))
            }
        }
    }

    /** Estado temporal, sin UI de favoritos ni persistencia. Una nueva carga lo sustituye. */
    override suspend fun alternarFavorito(id: Int) {
        memoria.update { lista -> lista.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }
    }

    /** Número de elementos en memoria, no registros guardados en una base de datos. */
    override suspend fun cantidadGuardada(): Int = memoria.value.size
    override suspend fun cantidadFavoritos(): Int = memoria.value.count { it.favorito }
    override suspend fun limpiarCache() { memoria.value = emptyList() }

    private fun errorDeConsulta(error: Exception, detalle: Boolean = false): IllegalStateException {
        val mensaje = when (error) {
            is HttpException -> if (detalle && error.code() == 404)
                "El smartphone solicitado no está disponible (HTTP 404)."
            else "El servicio respondió con un error (HTTP ${error.code()}). Inténtalo nuevamente."
            is IOException -> "No se pudo conectar. Revisa tu conexión a Internet y reintenta."
            is IllegalArgumentException -> if (detalle) "La respuesta del detalle no es válida. Inténtalo nuevamente."
                else "La respuesta del catálogo no es válida. Inténtalo nuevamente."
            else -> if (detalle) "No se pudo cargar el detalle. Vuelve a intentarlo."
                else "No se pudo cargar el catálogo. Vuelve a intentarlo."
        }
        return IllegalStateException(mensaje, error)
    }
}
