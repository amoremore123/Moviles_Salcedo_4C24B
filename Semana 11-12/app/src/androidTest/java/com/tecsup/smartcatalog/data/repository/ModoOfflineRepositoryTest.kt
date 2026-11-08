package com.tecsup.smartcatalog.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModoOfflineRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var api: ApiFalsa
    private lateinit var repo: ItemRepositoryImpl
    @Before fun preparar() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java).build()
        api = ApiFalsa()
        repo = ItemRepositoryImpl(api, db.itemDao())
    }
    @After fun cerrar() { db.close() }
    private suspend fun guardar() { db.itemDao().insertarTodos(listOf(ItemEntity(1, "Guardado", "Descripción local", 100.0, "", "smartphones", true))) }

    @Test fun detalleExitosoUsaSegundoEndpointYOrigenRemoto() = runBlocking {
        guardar()
        val detalle = repo.obtenerDetalleConRespaldo(1).getOrThrow()
        assertEquals(OrigenDetalle.REMOTO, detalle.origen)
        assertEquals("Remoto", detalle.item.nombre)
        assertEquals(listOf(1), api.ids)
        assertEquals("Guardado", repo.items.first().single().nombre)
    }

    @Test fun falloDelEndpointDevuelveRoomConOrigenLocalYFavorito() = runBlocking {
        guardar(); api.sinRed = true
        val detalle = repo.obtenerDetalleConRespaldo(1).getOrThrow()
        assertEquals(OrigenDetalle.LOCAL, detalle.origen)
        assertEquals("Guardado", detalle.item.nombre)
        assertTrue(detalle.item.favorito)
        assertEquals(listOf(1), api.ids)
        api.sinRed = false
        assertEquals(OrigenDetalle.REMOTO, repo.obtenerDetalleConRespaldo(1).getOrThrow().origen)
        assertEquals(listOf(1, 1), api.ids)
    }

    @Test fun falloSinRespaldoNoInventaInformacion() = runBlocking {
        api.sinRed = true
        assertTrue(repo.obtenerDetalleConRespaldo(999).isFailure)
        assertTrue(repo.items.first().isEmpty())
        assertEquals(listOf(999), api.ids)
    }

    @Test fun favoritosPuedenQuitarseSinRedYLaRecuperacionConservaElCambio() = runBlocking {
        guardar(); api.sinRed = true
        assertTrue(repo.refrescar().isFailure)
        assertEquals(1, repo.favoritos.first().size)
        repo.alternarFavorito(1)
        assertTrue(repo.favoritos.first().isEmpty())
        assertEquals(1, api.consultasLista)
        assertTrue(api.ids.isEmpty())
        assertEquals(1, repo.cantidadGuardada())
        api.sinRed = false; repo.refrescar().getOrThrow()
        assertFalse(repo.items.first().single().favorito)
        assertEquals(1, repo.cantidadGuardada())
    }

    @Test fun cancelacionNoSeConvierteEnDetalleLocal() = runBlocking {
        guardar(); api.cancelar = true
        try { repo.obtenerDetalleConRespaldo(1); fail("Debe propagar cancelación") }
        catch (_: CancellationException) { assertEquals(1, repo.cantidadFavoritos()) }
    }

    private class ApiFalsa : ApiService {
        var sinRed = false
        var cancelar = false
        var consultasLista = 0
        val ids = mutableListOf<Int>()
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            consultasLista++
            if (sinRed) throw IOException("Sin red simulada")
            return ProductosResponseDto(listOf(ProductoDto(id = 1, title = "Actualizado", category = "smartphones")))
        }
        override suspend fun obtenerDetalle(id: Int): ProductoDto {
            ids.add(id)
            if (cancelar) throw CancellationException("Cancelada")
            if (sinRed) throw IOException("Sin red simulada")
            return ProductoDto(id = id, title = "Remoto", category = "smartphones")
        }
    }
}
