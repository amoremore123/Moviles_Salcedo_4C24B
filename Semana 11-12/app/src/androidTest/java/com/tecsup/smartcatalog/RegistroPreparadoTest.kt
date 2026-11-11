package com.tecsup.smartcatalog

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import com.tecsup.smartcatalog.data.auth.AlmacenRegistroPendiente
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistroPreparadoTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun abreFormularioInactivoYRegresaAlLogin() {
        val app = ApplicationProvider.getApplicationContext<SmartCatalogTestApplication>()
        val sesionAnterior = app.auth.actual
        val intentosAnteriores = app.auth.intentos.get()
        app.auth.actual = null
        try {
            ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java)).use {
                compose.waitUntil(20_000) {
                    runCatching {
                        compose.onAllNodesWithText("Ver formulario de registro").fetchSemanticsNodes().isNotEmpty()
                    }.getOrDefault(false)
                }
                compose.onNodeWithText("Ver formulario de registro").performScrollTo().performClick()
                compose.onNodeWithText("Crear cuenta").assertIsDisplayed()
                compose.onNodeWithText("Registro aún no disponible.", substring = true).assertIsDisplayed()
                compose.onNodeWithText("Nombre completo").assertExists()
                compose.onNodeWithText("Correo electrónico").assertExists()
                compose.onNodeWithText("Confirmar contraseña").assertExists()
                compose.onNodeWithText("Validar formulario").assertIsNotEnabled()
                val registro = InstrumentationRegistry.getInstrumentation()
                if (InstrumentationRegistry.getArguments().getString("capturarM1") == "true") {
                    val captura = requireNotNull(registro.uiAutomation.takeScreenshot())
                    File(app.cacheDir, "m1-registro.png").outputStream().use { captura.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
                compose.onNodeWithText("Nombre completo").performScrollTo().performTextInput("Perfil de prueba")
                compose.onNodeWithText("Correo electrónico").performScrollTo().performTextInput("vista@example.invalid")
                compose.onNodeWithText("Contraseña", substring = false).performScrollTo().performTextInput("prueba1")
                compose.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("prueba1")
                compose.onNodeWithText("Validar formulario").performScrollTo().performClick()
                compose.onNodeWithText("El registro se habilitará cuando el servidor esté disponible.").performScrollTo().assertIsDisplayed()
                assertNull(app.auth.actual)
                assertEquals(intentosAnteriores, app.auth.intentos.get())
                compose.onNodeWithText("Volver al inicio de sesión").performScrollTo().performClick()
                compose.onNodeWithText("Iniciar sesión").assertExists()
            }
        } finally {
            app.auth.actual = sesionAnterior
        }
    }

    @Test fun reabreElAlmacenYSoloRecuperaElPerfilDelUidCorrecto() {
        val app = ApplicationProvider.getApplicationContext<SmartCatalogTestApplication>()
        val almacen = AlmacenRegistroPendiente(app)
        try {
            almacen.guardar("uid-prueba", "Perfil de prueba")
            val reabierto = AlmacenRegistroPendiente(app)
            assertEquals("Perfil de prueba", reabierto.nombrePara("uid-prueba"))
            assertNull(reabierto.nombrePara("otro-uid"))
            assertNull(reabierto.nombrePara(null))
            reabierto.limpiar()
            assertNull(AlmacenRegistroPendiente(app).nombrePara("uid-prueba"))
        } finally {
            almacen.limpiar()
        }
    }
}
