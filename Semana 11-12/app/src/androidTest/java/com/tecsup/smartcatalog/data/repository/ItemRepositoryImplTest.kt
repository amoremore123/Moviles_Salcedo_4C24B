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
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response

/** Integración real con Room en memoria; la API falsa nunca utiliza Internet. */
@RunWith(AndroidJUnit4::class)
class ItemRepositoryImplTest {
    private lateinit var db: AppDatabase
    private lateinit var api: ApiFalsa
    private lateinit var repo: ItemRepositoryImpl

    @Before fun preparar() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java).build()
        api = ApiFalsa()
        repo = ItemRepositoryImpl(api, db.itemDao())
    }

    @After fun cerrar() { db.close() }

    private fun dto(id: Int, nombre: String = "Modelo $id") = ProductoDto(id, nombre, "Descripción", 199.99, "imagen", "smartphones")
    private suspend fun cargarDos() {
        api.lista = { ProductosResponseDto(listOf(dto(1), dto(2))) }
        assertEquals(2, repo.refrescar().getOrThrow())
    }

    @Test fun sincronizaEnRoomYPublicaElFlowLocal() = runBlocking {
        cargarDos()
        assertEquals(2, db.itemDao().contarItems())
        assertEquals(listOf(1, 2), repo.items.first().map { it.id })
        assertEquals("Modelo 1", db.itemDao().obtenerPorId(1)?.nombre)
    }

    @Test fun segundaSincronizacionActualizaSinDuplicarYConservaFavoritos() = runBlocking {
        cargarDos()
        repo.alternarFavorito(1)
        api.lista = { ProductosResponseDto(listOf(dto(1, "Actualizado"), dto(2, "También actualizado"))) }
        assertEquals(2, repo.refrescar().getOrThrow())
        assertEquals(2, repo.cantidadGuardada())
        assertEquals("Actualizado", db.itemDao().obtenerPorId(1)?.nombre)
        assertTrue(requireNotNull(db.itemDao().obtenerPorId(1)).favorito)
        assertFalse(requireNotNull(db.itemDao().obtenerPorId(2)).favorito)
    }

    @Test fun errorHttpNoEliminaNiModificaLosDatosLocales() = runBlocking {
        cargarDos()
        repo.alternarFavorito(1)
        val anterior = repo.items.first()
        api.lista = { throw HttpException(Response.error<ProductosResponseDto>(503, "{}".toResponseBody())) }
        assertTrue(repo.refrescar().isFailure)
        assertEquals(anterior, repo.items.first())
    }

    @Test fun respuestaVaciaEsExitoSinBorrarDatosPrevios() = runBlocking {
        assertEquals(0, repo.refrescar().getOrThrow())
        assertTrue(repo.items.first().isEmpty())
        cargarDos()
        api.lista = { ProductosResponseDto(emptyList()) }
        assertEquals(0, repo.refrescar().getOrThrow())
        assertEquals(2, repo.cantidadGuardada())
    }

    @Test fun favoritosConteosYLimpiarCacheOperanSobreLosRegistrosReales() = runBlocking {
        cargarDos()
        repo.alternarFavorito(2)
        assertEquals(listOf(2), repo.favoritos.first().map { it.id })
        assertEquals(1, repo.cantidadFavoritos())
        repo.alternarFavorito(2)
        assertEquals(0, repo.cantidadFavoritos())
        repo.alternarFavorito(999) // Un ID inexistente no crea registros.
        assertEquals(2, repo.cantidadGuardada())
        repo.limpiarCache()
        assertEquals(0, repo.cantidadGuardada())
        assertEquals(0, repo.cantidadFavoritos())
        assertTrue(repo.items.first().isEmpty())
    }

    @Test fun elFlowReflejaEscriturasDelDaoSinRespuestaHttp() = runBlocking {
        db.itemDao().insertarTodos(listOf(ItemEntity(77, "Solo local", "", 25.0, "", "smartphones", true)))
        assertEquals("Solo local", repo.items.first().single().nombre)
        assertEquals(77, repo.favoritos.first().single().id)
        assertEquals(0, api.consultas)
    }

    @Test fun loteConUnDtoInvalidoNoGuardaNiSuParteValida() = runBlocking {
        cargarDos()
        val anterior = repo.items.first()
        api.lista = { ProductosResponseDto(listOf(dto(3), ProductoDto(id = -1))) }
        assertTrue(repo.refrescar().isFailure)
        assertEquals(anterior, repo.items.first())
        assertNull(db.itemDao().obtenerPorId(3))
    }

    @Test fun falloDuranteLaEscrituraRevierteTodaLaTransaccion() = runBlocking {
        cargarDos()
        val anterior = repo.items.first()
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER impedir_cuatro BEFORE INSERT ON items WHEN NEW.id = 4 BEGIN SELECT RAISE(ABORT, 'Fallo de prueba'); END")
        api.lista = { ProductosResponseDto(listOf(dto(3), dto(4))) }
        assertTrue(repo.refrescar().isFailure)
        assertNull(db.itemDao().obtenerPorId(3))
        assertEquals(anterior, repo.items.first())
    }

    @Test fun detalleUsaElSegundoEndpointYNoOcultaUnFalloConRoom() = runBlocking {
        cargarDos()
        api.detalle = { dto(it, "Detalle remoto") }
        assertEquals("Detalle remoto", repo.obtenerDetalle(1).getOrThrow().nombre)
        assertEquals("Modelo 1", repo.items.first().first().nombre)
        api.detalle = { throw IOException("Sin red") }
        assertTrue(repo.obtenerDetalle(1).isFailure)
        assertEquals(listOf(1, 1), api.ids)
        assertEquals(1, api.consultas)
    }

    @Test fun alternanciasConcurrentesNoPierdenActualizaciones() = runBlocking {
        cargarDos()
        coroutineScope { (1..21).map { async { repo.alternarFavorito(1) } }.awaitAll() }
        assertTrue(requireNotNull(db.itemDao().obtenerPorId(1)).favorito)
        assertEquals(1, repo.cantidadFavoritos())
    }

    private class ApiFalsa : ApiService {
        var consultas = 0
        val ids = mutableListOf<Int>()
        var lista: suspend () -> ProductosResponseDto = { ProductosResponseDto(emptyList()) }
        var detalle: suspend (Int) -> ProductoDto = { ProductoDto(id = it) }
        override suspend fun obtenerSmartphones(): ProductosResponseDto { consultas++; return lista() }
        override suspend fun obtenerDetalle(id: Int): ProductoDto { ids.add(id); return detalle(id) }
    }
}
