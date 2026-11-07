package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.local.ItemDao
import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepositoryImplContractTest {
    @Test fun respuestaInvalidaSeRechazaAntesDeEscribir() = runTest {
        val api = ApiFalsa()
        val dao = DaoDeEscritura()
        val repo = ItemRepositoryImpl(api, dao, StandardTestDispatcher(testScheduler))
        for (respuesta in listOf(ProductosResponseDto(), ProductosResponseDto(listOf(null)),
            ProductosResponseDto(listOf(ProductoDto(id = 1), ProductoDto(id = 1))))) {
            api.respuesta = respuesta
            assertTrue(repo.refrescar().isFailure)
        }
        assertEquals(0, dao.escrituras)
    }

    @Test fun falloDeGuardadoDevuelveFailureConLaCausa() = runTest {
        val dao = DaoDeEscritura()
        val repo = ItemRepositoryImpl(ApiFalsa(), dao, StandardTestDispatcher(testScheduler))
        val resultado = repo.refrescar()
        assertTrue(resultado.isFailure)
        assertSame(dao.fallo, resultado.exceptionOrNull()?.cause)
        assertEquals(1, dao.escrituras)
    }

    @Test fun cancelacionNoSeConvierteEnUnFailureNormal() = runTest {
        val api = ApiFalsa().apply { cancelar = true }
        val dao = DaoDeEscritura()
        val repo = ItemRepositoryImpl(api, dao, StandardTestDispatcher(testScheduler))
        try { repo.refrescar(); fail("Debe propagar cancelación") }
        catch (_: CancellationException) { assertEquals(0, dao.escrituras) }
        try { repo.obtenerDetalle(1); fail("Debe propagar cancelación") }
        catch (_: CancellationException) { assertEquals(listOf(1), api.ids) }
    }

    @Test fun detalleRechazaIdsInvalidosYRespuestasConOtroId() = runTest {
        val api = ApiFalsa()
        val repo = ItemRepositoryImpl(api, DaoDeEscritura(), StandardTestDispatcher(testScheduler))
        assertTrue(repo.obtenerDetalle(0).isFailure)
        assertTrue(repo.obtenerDetalle(-1).isFailure)
        assertTrue(api.ids.isEmpty())
        api.detalle = { ProductoDto(id = 2) }
        assertTrue(repo.obtenerDetalle(1).isFailure)
        api.detalle = { ProductoDto() }
        assertTrue(repo.obtenerDetalle(1).isFailure)
    }

    @Test fun detalle404DevuelveErrorControladoSinConsultarElDao() = runTest {
        val api = ApiFalsa().apply { detalle = { throw HttpException(Response.error<ProductoDto>(404, "{}".toResponseBody())) } }
        val repo = ItemRepositoryImpl(api, DaoDeEscritura(), StandardTestDispatcher(testScheduler))
        val resultado = repo.obtenerDetalle(9)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message.orEmpty().contains("404"))
    }

    private class ApiFalsa : ApiService {
        var cancelar = false
        val ids = mutableListOf<Int>()
        var respuesta = ProductosResponseDto(listOf(ProductoDto(id = 1)))
        var detalle: suspend (Int) -> ProductoDto = { ProductoDto(id = it) }
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            if (cancelar) throw CancellationException("Cancelada")
            return respuesta
        }
        override suspend fun obtenerDetalle(id: Int): ProductoDto {
            ids.add(id)
            if (cancelar) throw CancellationException("Cancelada")
            return detalle(id)
        }
    }

    /** Solo comprueba el contrato de fallo; la persistencia se verifica con Room real en androidTest. */
    private class DaoDeEscritura : ItemDao {
        var escrituras = 0
        val fallo = IllegalStateException("Fallo simulado al guardar")
        override fun observarTodos() = MutableStateFlow<List<ItemEntity>>(emptyList())
        override fun observarFavoritos() = MutableStateFlow<List<ItemEntity>>(emptyList())
        override suspend fun insertarTodos(items: List<ItemEntity>) { escrituras++; throw fallo }
        override suspend fun obtenerPorId(id: Int): ItemEntity? = error("No debe consultar detalle local")
        override suspend fun insertarNuevos(items: List<ItemEntity>): List<Long> = error("No se utiliza")
        override suspend fun actualizarDatos(id: Int, nombre: String, descripcion: String, precio: Double, imagen: String, categoria: String) = Unit
        override suspend fun actualizarFavorito(id: Int, favorito: Boolean) = Unit
        override suspend fun alternarFavorito(id: Int) = Unit
        override suspend fun contarItems(): Int = 0
        override suspend fun contarFavoritos(): Int = 0
        override suspend fun eliminarTodos() = Unit
    }
}
