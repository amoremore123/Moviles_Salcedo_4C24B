package com.tecsup.smartcatalog.data.repository

import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ItemRepositoryRemotoTest {
    private fun productos() = listOf(
        ProductoDto(id = 41, title = "Modelo remoto A", price = 125.50, category = "smartphones"),
        ProductoDto(id = 73, title = "Modelo remoto B", price = 350.25, category = "smartphones")
    )

    @Test fun consultaMapeaYCalculaLaCantidadSinFijarla() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = productos()))
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        assertEquals(2, repo.refrescar().getOrThrow())
        assertEquals(1, api.llamadas)
        assertEquals(listOf(41, 73), repo.items.value.map { it.id })
        assertEquals("Modelo remoto A", repo.items.value.first().nombre)
        assertEquals(125.50, repo.items.value.first().precio, 0.0)
    }

    @Test fun unaListaVaciaEsUnaRespuestaCorrecta() = runTest {
        val repo = ItemRepositoryRemoto(ApiFalsa(ProductosResponseDto(products = emptyList())), StandardTestDispatcher(testScheduler))
        assertEquals(0, repo.refrescar().getOrThrow())
        assertTrue(repo.items.value.isEmpty())
    }

    @Test fun errorHttpDeActualizacionNoBorraLosProductosAnteriores() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = productos()))
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        repo.refrescar().getOrThrow()
        val anterior = repo.items.value
        api.accion = { throw HttpException(Response.error<ProductosResponseDto>(503, "{}".toResponseBody())) }
        val resultado = repo.refrescar()
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message.orEmpty().contains("HTTP 503"))
        assertEquals(anterior, repo.items.value)
    }

    @Test fun errorDeConexionDevuelveUnResultadoSeguro() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = productos()))
        api.accion = { throw IOException("DNS no disponible") }
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        val resultado = repo.refrescar()
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull()?.message.orEmpty().contains("Internet"))
        assertTrue(repo.items.value.isEmpty())
    }

    @Test fun filtraIdsInvalidosYDuplicadosSinConfundirUnContenedorInvalidoConVacio() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = listOf(null, ProductoDto(), productos()[0], productos()[0])))
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        assertEquals(1, repo.refrescar().getOrThrow())
        val anterior = repo.items.value
        api.respuesta = ProductosResponseDto(products = null)
        assertTrue(repo.refrescar().isFailure)
        assertEquals(anterior, repo.items.value)
        api.respuesta = ProductosResponseDto(products = listOf(ProductoDto(id = 0)))
        assertTrue(repo.refrescar().isFailure)
        assertEquals(anterior, repo.items.value)
    }

    @Test fun losMetodosDelContratoOperanSoloEnMemoria() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = productos()))
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repo = ItemRepositoryRemoto(api, dispatcher)
        repo.refrescar().getOrThrow()
        repo.alternarFavorito(41)
        assertEquals(1, repo.cantidadFavoritos())
        assertEquals(1, repo.favoritos.first().size)
        assertTrue(repo.obtenerDetalle(41).isSuccess)
        assertTrue(repo.obtenerDetalle(-1).isFailure)
        assertTrue(ItemRepositoryRemoto(api, dispatcher).items.value.isEmpty())
        repo.limpiarCache()
        assertEquals(0, repo.cantidadGuardada())
        assertEquals(0, repo.cantidadFavoritos())
    }

    @Test fun laCancelacionNoSeConvierteEnErrorDeRed() = runTest {
        val api = ApiFalsa(ProductosResponseDto(products = productos()))
        api.accion = { throw CancellationException("Cancelada") }
        val repo = ItemRepositoryRemoto(api, StandardTestDispatcher(testScheduler))
        try {
            repo.refrescar()
            fail("Debe propagar la cancelación")
        } catch (_: CancellationException) {
            assertTrue(repo.items.value.isEmpty())
        }
    }

    private class ApiFalsa(var respuesta: ProductosResponseDto) : ApiService {
        var llamadas = 0
        var accion: suspend () -> ProductosResponseDto = { respuesta }
        override suspend fun obtenerSmartphones(): ProductosResponseDto { llamadas++; return accion() }
        override suspend fun obtenerDetalle(id: Int): ProductoDto = respuesta.products.orEmpty().filterNotNull().firstOrNull { it.id == id } ?: ProductoDto(id = id)
    }
}
