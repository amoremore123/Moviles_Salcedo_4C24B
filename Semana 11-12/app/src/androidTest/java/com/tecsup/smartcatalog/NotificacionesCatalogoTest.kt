package com.tecsup.smartcatalog

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.data.notificaciones.NotificacionesCatalogo
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificacionesCatalogoTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var app: SmartCatalogTestApplication
    private lateinit var manager: NotificationManager
    private var escenario: ActivityScenario<MainActivity>? = null
    private val admin = Sesion("uid-aviso-prueba", "vista@example.invalid", Rol.ADMIN)

    @Before fun preparar() {
        app = ApplicationProvider.getApplicationContext()
        manager = requireNotNull(app.getSystemService(NotificationManager::class.java))
        manager.cancel(NotificacionesCatalogo.ID_AVISO)
        app.auth.reiniciar()
        app.auth.actual = admin
        app.api.consultasLista.set(0)
        runBlocking {
            app.container.itemDao.eliminarTodos()
            app.container.itemDao.insertarTodos(listOf(ItemEntity(1, "Guardado", "", 1.0, "", "smartphones", true)))
        }
    }

    @After fun cerrar() {
        escenario?.close()
        manager.cancel(NotificacionesCatalogo.ID_AVISO)
        app.api.errorLista = null
        app.api.consultasLista.set(0)
        app.auth.reiniciar()
        app.auth.actual = admin.copy(rol = Rol.USUARIO)
    }

    private fun abrir() {
        escenario = ActivityScenario.launch(Intent(app, MainActivity::class.java))
        esperar("Panel de administrador")
    }

    private fun esperar(texto: String) {
        compose.waitUntil(20_000) {
            runCatching { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
        }
    }

    private fun pulsar(texto: String) {
        compose.onNodeWithTag("panel-admin").performScrollToNode(hasText(texto))
        compose.onNodeWithText(texto).performClick()
    }

    private fun aviso() = manager.activeNotifications.singleOrNull { it.id == NotificacionesCatalogo.ID_AVISO }

    @Test fun canalEsEstableYConImportanciaPredeterminada() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        repeat(2) { app.container.notificacionesCatalogo.crearCanal() }
        val canales = manager.notificationChannels.filter { it.id == NotificacionesCatalogo.CANAL }
        assertEquals(1, canales.size)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, canales.single().importance)
        assertEquals(app.getString(R.string.canal_catalogo), canales.single().name.toString())
    }

    @Test fun sincronizacionPublicaCantidadGuardadaYRespetaRolAlAbrir() {
        assumeTrue(app.container.notificacionesCatalogo.avisosHabilitados())
        abrir()
        pulsar("Sincronizar catálogo")
        esperar("Sincronización completada. Productos recibidos: 1.")
        compose.waitUntil(10_000) { aviso() != null }
        val notificacion = requireNotNull(aviso()).notification
        assertEquals("Catálogo actualizado", notificacion.extras.getString(Notification.EXTRA_TITLE))
        assertEquals("Se guardó 1 producto correctamente.", notificacion.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
        runBlocking {
            val producto = requireNotNull(app.container.itemDao.obtenerPorId(1))
            assertEquals("Modelo de prueba", producto.nombre)
            assertTrue(producto.favorito)
        }
        assertEquals(admin, app.auth.actual)
        assertEquals(1, app.api.consultasLista.get())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) assertTrue(notificacion.contentIntent.isImmutable)
        app.auth.actual = admin.copy(rol = Rol.USUARIO)
        notificacion.contentIntent.send()
        esperar("Nuestra colección")
        compose.onNodeWithText("Administración").assertDoesNotExist()
        compose.onNodeWithText("Panel de administrador").assertDoesNotExist()
        assertEquals(Rol.USUARIO, app.auth.actual?.rol)
    }

    @Test fun falloDeRedNoPublicaYConservaRoom() {
        app.api.errorLista = IOException("sin red de prueba")
        abrir()
        pulsar("Sincronizar catálogo")
        esperar("Sin conexión. Se conservaron los productos y favoritos locales.")
        assertNull(aviso())
        runBlocking {
            assertEquals(1, app.container.itemDao.contarItems())
            assertEquals(1, app.container.itemDao.contarFavoritos())
        }
    }

    @Test fun permisoDenegadoNoImpideGuardarProductos() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        assumeTrue(ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        abrir()
        pulsar("Sincronizar catálogo")
        esperar("Sincronización completada. Productos recibidos: 1.")
        assertNull(aviso())
        runBlocking { assertEquals("Modelo de prueba", app.container.itemDao.obtenerPorId(1)?.nombre) }
        assertEquals(admin, app.auth.actual)
    }

    @Test fun solicitudSoloDesdeAccionExplicitaYUnaVez() {
        assumeTrue(app.container.notificacionesCatalogo.puedeSolicitarPermiso())
        val automatizacion = InstrumentationRegistry.getInstrumentation().uiAutomation
        try {
            abrir()
            assertEquals(app.packageName, automatizacion.rootInActiveWindow?.packageName?.toString())
            pulsar("Activar avisos")
            val fin = SystemClock.elapsedRealtime() + 12_000
            while (SystemClock.elapsedRealtime() < fin &&
                automatizacion.rootInActiveWindow?.packageName?.toString()?.endsWith("permissioncontroller") != true
            ) SystemClock.sleep(100)
            assertTrue(automatizacion.rootInActiveWindow?.packageName?.toString()?.endsWith("permissioncontroller") == true)
            automatizacion.executeShellCommand("input keyevent KEYCODE_BACK").close()
            compose.onNodeWithTag("panel-admin").performScrollToNode(hasText("Ajustes de notificaciones"))
            compose.onNodeWithText("Activar avisos").assertDoesNotExist()
            assertFalse(app.container.notificacionesCatalogo.puedeSolicitarPermiso())
        } finally {
            app.getSharedPreferences("avisos_catalogo", Context.MODE_PRIVATE)
                .edit().remove("solicitud_realizada").commit()
        }
    }
}
