package com.tecsup.smartcatalog.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import com.tecsup.smartcatalog.domain.model.Item
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritosPersistenciaTest {
    private lateinit var context: Context
    private lateinit var nombre: String
    private lateinit var db: AppDatabase
    private fun abrir() = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()

    @Before fun prepararBaseExclusivaDePrueba() {
        context = ApplicationProvider.getApplicationContext()
        nombre = "h10-prueba-${UUID.randomUUID()}.db"
        db = abrir()
    }

    @After fun cerrarYEliminarSoloLaBaseDeEstaPrueba() {
        db.close()
        context.deleteDatabase(nombre)
    }

    @Test fun favoritosSobrevivenARecrearRepositorioReabrirBaseYSincronizar() = runBlocking {
        val api = ApiContada()
        var repo = ItemRepositoryImpl(api, db.itemDao())
        repo.refrescar().getOrThrow()
        repo.alternarFavorito(1)
        repo = ItemRepositoryImpl(api, db.itemDao())
        assertEquals(listOf(1), repo.favoritos.first().map { it.id })
        db.close()
        db = abrir()
        val nuevaApi = ApiContada()
        repo = ItemRepositoryImpl(nuevaApi, db.itemDao())
        assertEquals(listOf(1), repo.favoritos.first().map { it.id })
        assertEquals(0, nuevaApi.consultasLista)
        assertEquals(0, nuevaApi.consultasDetalle)
        nuevaApi.nombre = "Datos actualizados"
        repo.refrescar().getOrThrow()
        assertEquals(listOf(1), repo.favoritos.first().map { it.id })
        assertEquals("Datos actualizados", repo.favoritos.first().single().nombre)
        assertEquals(2, repo.cantidadGuardada())
        assertEquals(1, repo.cantidadFavoritos())
    }

    @Test fun alternanciasRapidasDeDosRepositoriosSonAtomicasYSinHttp() = runBlocking {
        val api = ApiContada()
        val primero = ItemRepositoryImpl(api, db.itemDao())
        val segundo = ItemRepositoryImpl(api, db.itemDao())
        primero.refrescar().getOrThrow()
        coroutineScope {
            (1..51).map { i -> async { (if (i % 2 == 0) primero else segundo).alternarFavorito(1) } }.awaitAll()
        }
        assertTrue(primero.items.first().first().favorito)
        assertEquals(listOf(1), segundo.favoritos.first().map { it.id })
        segundo.alternarFavorito(1)
        assertTrue(primero.favoritos.first().isEmpty())
        segundo.alternarFavorito(999)
        assertEquals(2, primero.cantidadGuardada())
        assertEquals(1, api.consultasLista)
        assertEquals(0, api.consultasDetalle)
    }

    @Test fun flowDeFavoritosNotificaMarcarYQuitarSinRecargaHttp() = runBlocking {
        val api = ApiContada()
        val repo = ItemRepositoryImpl(api, db.itemDao())
        repo.refrescar().getOrThrow()
        val emisiones = Channel<List<Item>>(Channel.UNLIMITED)
        val observador = launch(Dispatchers.Default) { repo.favoritos.collect { emisiones.send(it) } }
        try {
            assertTrue(siguiente(emisiones) { it.isEmpty() }.isEmpty())
            repo.alternarFavorito(2)
            assertEquals(listOf(2), siguiente(emisiones) { it.map { item -> item.id } == listOf(2) }.map { it.id })
            assertTrue(repo.items.first().first { it.id == 2 }.favorito)
            repo.alternarFavorito(2)
            assertTrue(siguiente(emisiones) { it.isEmpty() }.isEmpty())
            assertFalse(repo.items.first().first { it.id == 2 }.favorito)
            assertEquals(1, api.consultasLista)
            assertEquals(0, api.consultasDetalle)
        } finally { observador.cancelAndJoin(); emisiones.close() }
    }

    private suspend fun siguiente(canal: Channel<List<Item>>, condicion: (List<Item>) -> Boolean): List<Item> = withTimeout(15_000) {
        var valor = canal.receive()
        while (!condicion(valor)) valor = canal.receive()
        valor
    }

    private class ApiContada : ApiService {
        var consultasLista = 0
        var consultasDetalle = 0
        var nombre = "Modelo"
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            consultasLista++
            return ProductosResponseDto(listOf(ProductoDto(id = 1, title = nombre), ProductoDto(id = 2, title = "Segundo")))
        }
        override suspend fun obtenerDetalle(id: Int): ProductoDto { consultasDetalle++; return ProductoDto(id = id, title = nombre) }
    }
}
