package com.tecsup.smartcatalog.ui.admin

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryPrueba
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelAdminViewModelTest {
    private val admin = Sesion("prueba", "vista@example.invalid", Rol.ADMIN)
    private val item = Item(1, "Prueba", "", 1.0, "", "smartphones", true)
    private val base = ItemRepositoryPrueba(listOf(item))
    private val fecha = MutableStateFlow<Long?>(null)
    private lateinit var panel: PanelAdminViewModel

    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun cerrar() {
        if (::panel.isInitialized) panel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun abrir(repo: ItemRepository = base, limpiar: () -> Unit = {}) {
        panel = PanelAdminViewModel(repo, fecha, limpiar)
    }

    @Test fun cuentaProductosYFavoritosYReflejaCambios() = runTest {
        abrir()
        assertEquals(AlmacenamientoCatalogo.Cargando, panel.almacenamiento.value)
        runCurrent()
        assertEquals(AlmacenamientoCatalogo.Disponible(1, 1), panel.almacenamiento.value)
        base.alternarFavorito(1)
        runCurrent()
        assertEquals(AlmacenamientoCatalogo.Disponible(1, 0), panel.almacenamiento.value)
        base.limpiarCache()
        runCurrent()
        assertEquals(AlmacenamientoCatalogo.Disponible(0, 0), panel.almacenamiento.value)
    }

    @Test fun distingueUnFalloDeRoomDeUnCatalogoVacio() = runTest {
        abrir(object : ItemRepository by base {
            override val items = flow<List<Item>> { throw IllegalStateException() }
        })
        runCurrent()
        assertEquals(AlmacenamientoCatalogo.Error, panel.almacenamiento.value)
    }

    @Test fun bloqueaSegundoEnvioYPublicaResultadoReal() = runTest {
        val respuesta = CompletableDeferred<Int>()
        var intentos = 0
        abrir(object : ItemRepository by base {
            override suspend fun refrescar(): Result<Int> {
                intentos++
                return Result.success(respuesta.await())
            }
        })
        panel.sincronizar(admin)
        panel.sincronizar(admin)
        runCurrent()
        assertEquals(1, intentos)
        assertEquals(SincronizacionAdmin.EnCurso, panel.sincronizacion.value)
        respuesta.complete(3)
        runCurrent()
        assertEquals(SincronizacionAdmin.Correcta(3), panel.sincronizacion.value)
    }

    @Test fun distingueFalloDeConexionYErrorGenericoSinPerderDatos() = runTest {
        var error: Exception = IllegalStateException("red", IOException())
        abrir(object : ItemRepository by base {
            override suspend fun refrescar(): Result<Int> = Result.failure(error)
        })
        panel.sincronizar(admin)
        runCurrent()
        assertEquals(SincronizacionAdmin.Fallida(true), panel.sincronizacion.value)
        assertEquals(AlmacenamientoCatalogo.Disponible(1, 1), panel.almacenamiento.value)
        error = IllegalArgumentException()
        panel.sincronizar(admin)
        runCurrent()
        assertEquals(SincronizacionAdmin.Fallida(false), panel.sincronizacion.value)
        assertEquals(AlmacenamientoCatalogo.Disponible(1, 1), panel.almacenamiento.value)
        assertNull(panel.ultimaSincronizacion.value)
    }

    @Test fun lasAccionesRechazanUsuarioYFaltaDeSesion() = runTest {
        var intentos = 0
        abrir(object : ItemRepository by base {
            override suspend fun refrescar(): Result<Int> {
                intentos++
                return Result.success(1)
            }
        }) { intentos++ }
        for (sesion in listOf(null, admin.copy(rol = Rol.USUARIO))) {
            panel.sincronizar(sesion)
            panel.limpiarCache(sesion)
        }
        runCurrent()
        assertEquals(0, intentos)
        assertEquals(ResultadoCache.INICIAL, panel.cache.value)
    }

    @Test fun limpiarSoloInvocaLaCacheDeImagenesYConservaDatos() = runTest {
        var imagenes = 2
        abrir { imagenes = 0 }
        panel.limpiarCache(admin)
        runCurrent()
        assertEquals(0, imagenes)
        assertEquals(ResultadoCache.LIMPIADA, panel.cache.value)
        assertEquals(AlmacenamientoCatalogo.Disponible(1, 1), panel.almacenamiento.value)
    }

    @Test fun informaErrorDeLimpieza() = runTest {
        abrir { throw IllegalStateException() }
        panel.limpiarCache(admin)
        assertEquals(ResultadoCache.ERROR, panel.cache.value)
    }
}
