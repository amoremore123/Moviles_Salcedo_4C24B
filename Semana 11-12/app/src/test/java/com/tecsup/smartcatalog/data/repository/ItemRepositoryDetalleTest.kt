package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepositoryDetalleTest {
    @Test fun consultaElSegundoEndpointAunqueElIdYaEsteEnLaListaYSinModificarla() = runTest {
        val api = ApiFalsa()
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        repo.refrescar().getOrThrow()
        val listaAnterior = repo.items.value
        api.detalle = { ProductoDto(id = it, title = "Detalle desde el segundo endpoint", price = 350.25) }
        val detalle = repo.obtenerDetalle(121).getOrThrow()
        assertEquals(listOf(121), api.ids)
        assertEquals(1, api.consultasLista)
        assertEquals("Detalle desde el segundo endpoint", detalle.nombre)
        assertEquals(350.25, detalle.precio, 0.0)
        assertEquals(listaAnterior, repo.items.value)
        assertEquals(199.99, repo.items.value.first().precio, 0.0)
    }

    @Test fun unFalloDeConexionNoSeOcultaConElObjetoYaPresenteEnMemoria() = runTest {
        val api = ApiFalsa()
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        repo.refrescar().getOrThrow()
        val anterior = repo.items.value
        api.detalle = { throw IOException("Sin red") }
        assertTrue(repo.obtenerDetalle(121).isFailure)
        assertEquals(listOf(121), api.ids)
        assertEquals(anterior, repo.items.value)
    }

    @Test fun un404DevuelveUnFalloControlado() = runTest {
        val api = ApiFalsa()
        api.detalle = { throw HttpException(Response.error<ProductoDto>(404, "{}".toResponseBody())) }
        val resultado = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler)).obtenerDetalle(9999)
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message.orEmpty().contains("404"))
    }

    @Test fun noEnviaIdentificadoresInvalidosALaApi() = runTest {
        val api = ApiFalsa()
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        assertTrue(repo.obtenerDetalle(0).isFailure)
        assertTrue(repo.obtenerDetalle(-5).isFailure)
        assertTrue(api.ids.isEmpty())
    }

    @Test fun rechazaRespuestaSinIdOConUnIdDiferente() = runTest {
        val api = ApiFalsa()
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        api.detalle = { ProductoDto(id = 122) }
        assertTrue(repo.obtenerDetalle(121).isFailure)
        api.detalle = { ProductoDto() }
        assertTrue(repo.obtenerDetalle(121).isFailure)
        assertTrue(repo.items.value.isEmpty())
    }

    @Test fun propagaLaCancelacionDeLaConsulta() = runTest {
        val api = ApiFalsa()
        api.detalle = { throw CancellationException("Cancelada") }
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        try { repo.obtenerDetalle(121); fail("Debe propagar cancelación") }
        catch (_: CancellationException) { assertTrue(repo.items.value.isEmpty()) }
    }

    private class ApiFalsa : ApiService {
        val ids = mutableListOf<Int>()
        var consultasLista = 0
        var detalle: suspend (Int) -> ProductoDto = { ProductoDto(id = it, title = "Detalle $it", price = 199.99) }
        override suspend fun obtenerDetalle(id: Int): ProductoDto { ids.add(id); return detalle(id) }
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            consultasLista++
            return ProductosResponseDto(products = listOf(ProductoDto(id = 121, title = "Objeto de lista", price = 199.99)))
        }
    }
}
