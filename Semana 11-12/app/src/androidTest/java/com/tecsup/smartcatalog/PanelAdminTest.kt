package com.tecsup.smartcatalog

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.memory.MemoryCache
import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PanelAdminTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var app: SmartCatalogTestApplication
    private var escenario: ActivityScenario<MainActivity>? = null
    private val admin = Sesion("uid-panel", "panel@example.invalid", Rol.ADMIN)

    @Before fun preparar() {
        app = ApplicationProvider.getApplicationContext()
        app.auth.reiniciar()
        app.auth.actual = admin
        app.api.consultasLista.set(0)
        runBlocking {
            app.container.itemDao.eliminarTodos()
            app.container.itemDao.insertarTodos(listOf(ItemEntity(1, "Modelo guardado", "", 1.0, "", "smartphones", true)))
        }
    }

    @After fun cerrar() {
        escenario?.close()
        app.auth.reiniciar()
        app.auth.actual = admin.copy(rol = Rol.USUARIO)
        app.api.consultasLista.set(0)
    }

    private fun abrir() {
        escenario = ActivityScenario.launch(Intent(app, MainActivity::class.java))
    }

    private fun esperar(texto: String) {
        compose.waitUntil(20_000) {
            runCatching { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }

    private fun pulsarMantenimiento(texto: String) {
        compose.onNodeWithTag("panel-admin").performScrollToNode(hasText(texto))
        compose.onNodeWithText(texto).performClick()
    }

    @Test fun panelSincronizaRoomYConservaElFavorito() {
        abrir()
        esperar("Panel de administrador")
        esperar("Productos almacenados")
        compose.onAllNodesWithText("1").assertCountEquals(2)
        pulsarMantenimiento("Sincronizar catálogo")
        esperar("Sincronización completada. Productos recibidos: 1.")
        compose.onNodeWithText("Última sincronización correcta:", substring = true).assertExists()
        runBlocking {
            val item = requireNotNull(app.container.itemDao.obtenerPorId(1))
            assertEquals("Modelo de prueba", item.nombre)
            assertTrue(item.favorito)
        }
        assertEquals(1, app.api.consultasLista.get())
    }

    @Test fun cancelarYConfirmarLimpiezaAfectanSoloCacheCoil() {
        val cache = requireNotNull(app.container.imageLoader.memoryCache)
        val clave = MemoryCache.Key("imagen-h14-prueba")
        cache[clave] = MemoryCache.Value(Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888))
        abrir()
        esperar("Panel de administrador")
        pulsarMantenimiento("Limpiar caché")
        compose.onNodeWithText("Limpiar caché de imágenes").assertIsDisplayed()
        compose.onNodeWithText("Cancelar").performClick()
        assertNotNull(cache[clave])
        pulsarMantenimiento("Limpiar caché")
        compose.onNodeWithText("Confirmar limpieza").performClick()
        esperar("Caché de imágenes limpiada.")
        assertNull(cache[clave])
        runBlocking {
            assertEquals(1, app.container.itemDao.contarItems())
            assertEquals(1, app.container.itemDao.contarFavoritos())
        }
        assertEquals(admin, app.auth.actual)
    }

    @Test fun adminVuelveAlPanelYElCierreLoRetiraDeAtras() {
        abrir()
        esperar("Panel de administrador")
        compose.onNodeWithText("Explorar catálogo").performClick()
        esperar("Nuestra colección")
        compose.onNodeWithText("Administración").performClick()
        esperar("Panel de administrador")
        compose.onNodeWithText("Cerrar sesión").performClick()
        esperar("Iniciar sesión")
        escenario!!.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(10_000) { escenario!!.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
        assertNull(app.auth.actual)
    }

    @Test fun usuarioRecuperadoEntraAlCatalogoSinPanel() {
        app.auth.actual = admin.copy(rol = Rol.USUARIO)
        abrir()
        esperar("Nuestra colección")
        compose.onNodeWithText("Administración").assertDoesNotExist()
        compose.onNodeWithText("Panel de administrador").assertDoesNotExist()
        compose.onNodeWithText("Mis favoritos").performClick()
        esperar("Modelo de prueba")
        compose.onNodeWithContentDescription("Quitar de favoritos: Modelo de prueba").assertExists()
    }
}
