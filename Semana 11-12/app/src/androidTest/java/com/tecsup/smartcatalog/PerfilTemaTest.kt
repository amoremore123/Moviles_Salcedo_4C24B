package com.tecsup.smartcatalog

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import com.tecsup.smartcatalog.testing.capturarMejora
import com.tecsup.smartcatalog.ui.theme.PreferenciasTema
import com.tecsup.smartcatalog.ui.theme.TemaSmartCatalog
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerfilTemaTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun perfilMuestraDatosDeLaSesionYConservaTemaAlReabrirYCambiarCuenta() {
        val app = ApplicationProvider.getApplicationContext<SmartCatalogTestApplication>()
        val preferencias = app.container.preferenciasTema
        val temaAnterior = preferencias.tema.value
        val sesionAnterior = app.auth.actual
        val usuario = Sesion("uid-perfil-usuario", "perfil@example.invalid", Rol.USUARIO, "Nombre de prueba")
        val admin = Sesion("uid-perfil-admin", "administracion@example.invalid", Rol.ADMIN)
        try {
            preferencias.elegir(TemaSmartCatalog.SISTEMA)
            app.auth.actual = usuario
            abrir(app).use {
                verPerfil()
                compose.onNodeWithText("Nombre de prueba").assertIsDisplayed()
                compose.onNodeWithText(usuario.correo).assertIsDisplayed()
                compose.onNodeWithText("Usuario").assertIsDisplayed()
                compose.onNodeWithText("Editar nombre").assertIsNotEnabled()
                compose.onNodeWithText("Seguir sistema").assertIsSelected()
                seleccionar("Claro")
                compose.capturarMejora("m4", "perfil-claro")
                seleccionar("Oscuro")
                compose.capturarMejora("m4", "perfil-oscuro")
                assertEquals(TemaSmartCatalog.OSCURO, PreferenciasTema(app).tema.value)
            }
            abrir(app).use {
                verPerfil()
                compose.onNodeWithText("Oscuro").assertIsSelected()
                compose.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
                esperar("Encuentra tu próximo smartphone")
                assertNull(app.auth.actual)
                compose.onNodeWithText("Mi perfil").assertDoesNotExist()
            }
            app.auth.actual = admin
            abrir(app).use {
                esperar("Panel de administrador")
                compose.capturarMejora("m4", "panel-oscuro")
                verPerfil()
                compose.onNodeWithText("Administrador").assertIsDisplayed()
                compose.onNodeWithText(admin.correo).assertIsDisplayed()
                compose.onNodeWithText("Sin nombre registrado").assertIsDisplayed()
                compose.onNodeWithText(usuario.correo).assertDoesNotExist()
                compose.onNodeWithText("Oscuro").assertIsSelected()
                seleccionar("Seguir sistema")
                assertEquals(TemaSmartCatalog.SISTEMA, PreferenciasTema(app).tema.value)
                compose.capturarMejora("m4", "perfil-seguir-sistema")
                compose.onNodeWithContentDescription("Volver desde el perfil").performClick()
                esperar("Panel de administrador")
            }
        } finally {
            preferencias.elegir(temaAnterior)
            app.auth.actual = sesionAnterior
        }
    }

    private fun abrir(app: SmartCatalogTestApplication): ActivityScenario<MainActivity> =
        ActivityScenario.launch(Intent(app, MainActivity::class.java))

    private fun verPerfil() {
        esperar("Perfil")
        compose.onNodeWithText("Perfil").performClick()
        esperar("Mi perfil")
    }

    private fun seleccionar(etiqueta: String) {
        compose.onNodeWithText(etiqueta).performScrollTo().performClick()
        compose.onNodeWithText(etiqueta).assertIsSelected()
    }

    private fun esperar(texto: String) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
}
