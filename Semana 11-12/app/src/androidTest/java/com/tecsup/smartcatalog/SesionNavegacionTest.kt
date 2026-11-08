package com.tecsup.smartcatalog

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.data.auth.ErrorAutenticacion
import com.tecsup.smartcatalog.data.auth.TipoErrorAutenticacion
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import com.tecsup.smartcatalog.ui.navigation.DestinoConPermiso
import com.tecsup.smartcatalog.ui.navigation.DestinoProtegido
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI y navegación con proveedores simulados; estas pruebas no representan autenticación Firebase real. */
@RunWith(AndroidJUnit4::class)
class SesionNavegacionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var app: SmartCatalogTestApplication
    private var escenario: ActivityScenario<MainActivity>? = null
    @Before fun preparar() {
        app = ApplicationProvider.getApplicationContext()
        app.auth.reiniciar()
        app.api.consultasLista.set(0); app.api.consultasDetalle.set(0)
        app.container.database.clearAllTables()
    }
    @After fun limpiar() {
        app.auth.pendiente?.complete(Unit)
        escenario?.close()
        app.auth.reiniciar()
        app.auth.actual = Sesion("uid-simulado", "vista@example.invalid", Rol.USUARIO)
        app.api.consultasLista.set(0); app.api.consultasDetalle.set(0)
    }
    private fun abrir() { escenario = ActivityScenario.launch(Intent(app, MainActivity::class.java)) }
    private fun esperar(texto: String) {
        compose.waitUntil(20_000) { runCatching { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText(texto).assertIsDisplayed()
    }
    private fun login() {
        esperar("Iniciar sesión")
        compose.onNodeWithTag("correo").performTextInput("vista@example.invalid")
        compose.onNodeWithTag("clave").performTextInput("valor-exclusivo-de-prueba")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
    }

    @Test fun verificaSesionSinDestellosDeCatalogoNiSolicitudesDeProductos() {
        app.auth.pendiente = CompletableDeferred()
        abrir(); esperar("Verificando tu sesión...")
        compose.onNodeWithText("Nuestra colección").assertDoesNotExist()
        compose.onNodeWithText("Iniciar sesión").assertDoesNotExist()
        assertEquals(0, app.api.consultasLista.get())
        app.auth.pendiente!!.complete(Unit); esperar("Iniciar sesión")
    }

    @Test fun validaVaciosYCorreoYPermiteMostrarOcultarClave() {
        abrir(); esperar("Iniciar sesión")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        esperar("Ingresa tu correo electrónico.")
        compose.onNodeWithText("Ingresa tu contraseña.").assertExists()
        compose.onNodeWithTag("correo").performTextInput("sin-arroba")
        compose.onNodeWithTag("clave").performTextInput("valor-exclusivo-de-prueba")
        compose.onNodeWithText("Iniciar sesión").performScrollTo().performClick()
        esperar("Ingresa un correo electrónico válido.")
        assertEquals(0, app.auth.intentos.get())
        compose.onNodeWithTag("clave").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        compose.onNodeWithText("Mostrar").performClick()
        compose.onNodeWithTag("clave").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
        compose.onNodeWithText("Ocultar").performClick()
        compose.onNodeWithTag("clave").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
    }

    @Test fun credencialesIncorrectasMantienenLoginSinAccesoProtegido() {
        app.auth.error = ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
        abrir(); login(); esperar("No se pudo iniciar sesión. Verifica tus credenciales.")
        compose.onNodeWithText("Nuestra colección").assertDoesNotExist()
        assertEquals(0, app.api.consultasLista.get())
    }

    @Test fun usuarioNavegaConFavoritosYLogoutEliminaBackstackSinBorrarRoom() {
        abrir(); login(); esperar("Nuestra colección")
        compose.onNodeWithText("Administración").assertDoesNotExist()
        compose.onNodeWithContentDescription("Agregar a favoritos: Modelo de prueba").performClick()
        compose.onNodeWithText("Mis favoritos").performClick()
        esperar("Guardados en este dispositivo · compartidos entre cuentas")
        compose.onNodeWithText("Modelo de prueba").performClick()
        esperar("Detalle del smartphone")
        compose.onNodeWithText("Cerrar sesión").performClick()
        esperar("Iniciar sesión")
        assertNull(app.auth.actual)
        runBlocking { assertTrue(app.container.itemDao.obtenerPorId(1)?.favorito == true) }
        escenario!!.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(10_000) { escenario!!.state == Lifecycle.State.DESTROYED }
        assertEquals(Lifecycle.State.DESTROYED, escenario!!.state)
        escenario!!.close(); abrir(); esperar("Iniciar sesión")
        compose.onNodeWithText("Detalle del smartphone").assertDoesNotExist()
        compose.onNodeWithText("Nuestra colección").assertDoesNotExist()
    }

    @Test fun adminEntraABienvenidaYPuedeVolverDesdeCatalogo() {
        app.auth.rolLogin = Rol.ADMIN
        abrir(); login(); esperar("Bienvenido, administrador")
        compose.onNodeWithText("Explorar catálogo").performClick(); esperar("Nuestra colección")
        compose.onNodeWithText("Administración").performClick(); esperar("Bienvenido, administrador")
    }

    @Test fun recreacionConservaViewModelYNuevoInicioVuelveAConsultarSesion() {
        abrir(); login(); esperar("Nuestra colección")
        val consultas = app.auth.consultas.get()
        escenario!!.recreate(); esperar("Nuestra colección")
        assertEquals(consultas, app.auth.consultas.get())
        escenario!!.close(); abrir(); esperar("Nuestra colección")
        assertEquals(consultas + 1, app.auth.consultas.get())
    }

    @Test fun rutaAdministrativaInvocadaDirectamenteDeniegaUsuario() {
        abrir(); esperar("Iniciar sesión")
        escenario!!.onActivity {
            it.setContent {
                DestinoConPermiso(Sesion("uid-simulado", "vista@example.invalid", Rol.USUARIO), DestinoProtegido.ADMIN) {
                    Text("Contenido administrativo exclusivo")
                }
            }
        }
        esperar("Acceso no autorizado")
        compose.onNodeWithText("Contenido administrativo exclusivo").assertDoesNotExist()
    }
}
