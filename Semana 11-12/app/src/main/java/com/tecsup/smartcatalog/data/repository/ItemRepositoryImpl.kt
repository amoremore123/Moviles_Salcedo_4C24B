package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.local.ItemDao
import com.tecsup.smartcatalog.data.mapper.toEntity
import com.tecsup.smartcatalog.data.mapper.toItem
import com.tecsup.smartcatalog.data.mapper.toItemSeguro
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.DetalleConOrigen
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Room es la única fuente de la colección; la respuesta HTTP solo se escribe mediante el DAO. */
class ItemRepositoryImpl(
    private val api: ApiService,
    private val dao: ItemDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ItemRepository {
    private val sincronizacion = Mutex()
    private val ultimaSincronizacionMutable = MutableStateFlow<Long?>(null)
    val ultimaSincronizacion = ultimaSincronizacionMutable.asStateFlow()
    override val items: Flow<List<Item>> = dao.observarTodos().map { lista -> lista.map { it.toItem() } }
    override val favoritos: Flow<List<Item>> = dao.observarFavoritos().map { lista -> lista.map { it.toItem() } }

    override suspend fun refrescar(): Result<Int> = withContext(dispatcher) {
        sincronizacion.withLock {
            try {
                val respuesta = requireNotNull(api.obtenerSmartphones().products)
                // Validar el lote completo antes de escribir: un DTO inválido impide toda la carga.
                val productos = respuesta.map { requireNotNull(it).toItemSeguro().getOrThrow() }
                require(productos.map { it.id }.distinct().size == productos.size) { "IDs duplicados en la respuesta." }
                // Una respuesta vacía es un éxito sin cambios. No se eliminan IDs ausentes de la API.
                if (productos.isNotEmpty()) dao.insertarTodos(productos.map { it.toEntity() })
                // insertarTodos es transaccional y conserva favorito de los IDs ya almacenados.
                ultimaSincronizacionMutable.value = System.currentTimeMillis()
                Result.success(productos.size)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Result.failure(errorDeConsulta(error))
            }
        }
    }

    /** Siempre consulta products/{id}; no escribe el catálogo ni oculta fallos con datos locales. */
    override suspend fun obtenerDetalle(id: Int): Result<Item> {
        if (id <= 0) return Result.failure(IllegalArgumentException("El identificador del smartphone no es válido."))
        return withContext(dispatcher) {
            try {
                val producto = api.obtenerDetalle(id).toItemSeguro().getOrThrow()
                require(producto.id == id) { "El ID de la respuesta no coincide con el solicitado." }
                Result.success(producto)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Result.failure(errorDeConsulta(error, detalle = true))
            }
        }
    }

    override suspend fun alternarFavorito(id: Int) = withContext(dispatcher) { dao.alternarFavorito(id) }

    override suspend fun obtenerDetalleConRespaldo(id: Int): Result<DetalleConOrigen> {
        if (id <= 0) return Result.failure(IllegalArgumentException("El identificador del smartphone no es válido."))
        val remoto = obtenerDetalle(id) // Siempre intenta primero el segundo endpoint, con su validación existente.
        remoto.getOrNull()?.let { return Result.success(DetalleConOrigen(it, OrigenDetalle.REMOTO)) }
        return withContext(dispatcher) {
            try {
                val local = dao.obtenerPorId(id)
                if (local != null) Result.success(DetalleConOrigen(local.toItem(), OrigenDetalle.LOCAL))
                else Result.failure(requireNotNull(remoto.exceptionOrNull()))
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                Result.failure(IllegalStateException("No se pudo consultar el detalle ni su información guardada. Inténtalo nuevamente.", error))
            }
        }
    }
    override suspend fun cantidadGuardada(): Int = withContext(dispatcher) { dao.contarItems() }
    override suspend fun cantidadFavoritos(): Int = withContext(dispatcher) { dao.contarFavoritos() }
    override suspend fun limpiarCache() = withContext(dispatcher) {
        sincronizacion.withLock { dao.eliminarTodos() }
    }

    private fun errorDeConsulta(error: Exception, detalle: Boolean = false): IllegalStateException {
        val mensaje = when (error) {
            is HttpException -> if (detalle && error.code() == 404)
                "El smartphone solicitado no está disponible (HTTP 404)."
            else "El servicio respondió con un error (HTTP ${error.code()}). Inténtalo nuevamente."
            is IOException -> "No se pudo conectar. Revisa tu conexión a Internet y reintenta."
            is IllegalArgumentException -> if (detalle) "La respuesta del detalle no es válida. Inténtalo nuevamente."
                else "La respuesta del catálogo no es válida. Inténtalo nuevamente."
            else -> if (detalle) "No se pudo cargar el detalle. Vuelve a intentarlo."
                else "No se pudo sincronizar el catálogo. Vuelve a intentarlo."
        }
        return IllegalStateException(mensaje, error)
    }
}
