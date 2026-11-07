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
                // Se sustituye la colección únicamente después de una respuesta y mapeo correctos.
                memoria.value = productos
                Result.success(productos.size)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                val mensaje = when (error) {
                    is HttpException -> "El servicio respondió con un error (HTTP ${error.code()}). Inténtalo nuevamente."
                    is IOException -> "No se pudo conectar. Revisa tu conexión a Internet y reintenta."
                    is IllegalArgumentException -> "La respuesta del catálogo no es válida. Inténtalo nuevamente."
                    else -> "No se pudo cargar el catálogo. Vuelve a intentarlo."
                }
                Result.failure(IllegalStateException(mensaje, error))
            }
        }
    }

    /** Solo busca en la colección actual; H6 no implementa una pantalla ni una consulta de detalle. */
    override suspend fun obtenerDetalle(id: Int): Result<Item> =
        memoria.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("El producto no está en la colección en memoria."))

    /** Estado temporal, sin UI de favoritos ni persistencia. Una nueva carga lo sustituye. */
    override suspend fun alternarFavorito(id: Int) {
        memoria.update { lista -> lista.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }
    }

    /** Número de elementos en memoria, no registros guardados en una base de datos. */
    override suspend fun cantidadGuardada(): Int = memoria.value.size
    override suspend fun cantidadFavoritos(): Int = memoria.value.count { it.favorito }

    override suspend fun limpiarCache() {
        memoria.value = emptyList()
    }
}
