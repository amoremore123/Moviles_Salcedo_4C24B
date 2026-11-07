package com.tecsup.smartcatalog.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ItemDao

    @Before fun crearBaseExclusivamenteEnMemoria() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.itemDao()
    }

    @After fun cerrarBaseDePrueba() { database.close() }

    private fun item(id: Int, favorito: Boolean = false) = ItemEntity(
        id, "Modelo $id", "Descripción $id", id * 100.25, "imagen-$id", "smartphones", favorito
    )

    @Test fun insertarUnProductoYRecuperarlo() = runBlocking {
        val producto = item(1)
        dao.insertarTodos(listOf(producto))
        assertEquals(producto, dao.observarTodos().first().single())
    }

    @Test fun insertarVariosProductosMantieneTodosLosRegistros() = runBlocking {
        dao.insertarTodos(listOf(item(3), item(1), item(2)))
        assertEquals(listOf(1, 2, 3), dao.observarTodos().first().map { it.id })
    }

    @Test fun buscarPorIdDistingueExistenteEInexistente() = runBlocking {
        dao.insertarTodos(listOf(item(1), item(2)))
        assertEquals(item(2), dao.obtenerPorId(2))
        assertNull(dao.obtenerPorId(99))
    }

    @Test fun actualizarElEstadoFavorito() = runBlocking {
        dao.insertarTodos(listOf(item(1)))
        dao.actualizarFavorito(1, true)
        assertTrue(requireNotNull(dao.obtenerPorId(1)).favorito)
        dao.actualizarFavorito(1, false)
        assertFalse(requireNotNull(dao.obtenerPorId(1)).favorito)
    }

    @Test fun consultarSoloLosFavoritos() = runBlocking {
        dao.insertarTodos(listOf(item(1, true), item(2), item(3, true)))
        assertEquals(listOf(1, 3), dao.observarFavoritos().first().map { it.id })
    }

    @Test fun contarProductosYFavoritos() = runBlocking {
        dao.insertarTodos(listOf(item(1, true), item(2), item(3, true)))
        assertEquals(3, dao.contarItems())
        assertEquals(2, dao.contarFavoritos())
    }

    @Test fun eliminarTodosLosRegistros() = runBlocking {
        dao.insertarTodos(listOf(item(1, true), item(2)))
        dao.eliminarTodos()
        assertEquals(0, dao.contarItems())
        assertEquals(0, dao.contarFavoritos())
        assertTrue(dao.observarTodos().first().isEmpty())
    }

    @Test fun elFlowNotificaInsercionCambioDeFavoritoYEliminacion() = runBlocking {
        val cambios = Channel<List<ItemEntity>>(Channel.UNLIMITED)
        val observador = launch(Dispatchers.Default) { dao.observarTodos().collect { cambios.send(it) } }
        try {
            assertTrue(siguiente(cambios) { it.isEmpty() }.isEmpty())
            dao.insertarTodos(listOf(item(1)))
            assertEquals(item(1), siguiente(cambios) { it.size == 1 }.single())
            dao.actualizarFavorito(1, true)
            assertTrue(siguiente(cambios) { it.singleOrNull()?.favorito == true }.single().favorito)
            dao.eliminarTodos()
            assertTrue(siguiente(cambios) { it.isEmpty() }.isEmpty())
        } finally {
            observador.cancelAndJoin()
            cambios.close()
        }
    }

    @Test fun actualizarDatosDeUnIdExistenteConservaSuFavorito() = runBlocking {
        dao.insertarTodos(listOf(item(1, true)))
        val nuevosDatos = item(1).copy(nombre = "Nombre actualizado", descripcion = "Nueva descripción", precio = 450.75, imagen = "imagen-nueva")
        dao.insertarTodos(listOf(nuevosDatos))
        assertEquals(nuevosDatos.copy(favorito = true), dao.obtenerPorId(1))
        assertEquals(1, dao.contarItems())
        assertEquals(1, dao.contarFavoritos())
    }

    @Test fun insertarTodosNoImponeElFavoritoEntranteSobreUnaFilaExistente() = runBlocking {
        dao.insertarTodos(listOf(item(1, false)))
        dao.insertarTodos(listOf(item(1, true).copy(nombre = "Actualizado"), item(2, true)))
        assertFalse(requireNotNull(dao.obtenerPorId(1)).favorito)
        assertTrue(requireNotNull(dao.obtenerPorId(2)).favorito)
        assertEquals("Actualizado", dao.obtenerPorId(1)?.nombre)
        assertEquals(2, dao.contarItems())
        assertEquals(1, dao.contarFavoritos())
    }

    private suspend fun siguiente(canal: Channel<List<ItemEntity>>, condicion: (List<ItemEntity>) -> Boolean): List<ItemEntity> = withTimeout(15_000) {
        var valor = canal.receive()
        while (!condicion(valor)) valor = canal.receive()
        valor
    }
}
