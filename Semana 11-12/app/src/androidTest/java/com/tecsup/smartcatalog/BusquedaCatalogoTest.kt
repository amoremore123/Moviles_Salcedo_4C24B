package com.tecsup.smartcatalog

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import com.tecsup.smartcatalog.testing.capturarMejora
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusquedaCatalogoTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun combinaCriteriosConRoomAunqueLaApiNoTengaConexion() {
        val app = ApplicationProvider.getApplicationContext<SmartCatalogTestApplication>()
        val sesionAnterior = app.auth.actual
        val errorAnterior = app.api.errorLista
        val consultasIniciales = app.api.consultasLista.get()
        app.auth.actual = Sesion("uid-busqueda", "busqueda@example.invalid", Rol.USUARIO)
        app.api.errorLista = IOException("Sin conexión")
        app.container.database.clearAllTables()
        runBlocking {
            app.container.itemDao.insertarTodos(
                listOf(
                    producto(31, "Samsung Galaxy A", 240.0, true, "costa"),
                    producto(32, "Samsung Galaxy B", 325.0, false, "aurora"),
                    producto(33, "Samsung Galaxy C", 390.0, true, "andino"),
                    producto(34, "Pixel de prueba", 230.0, true, "sierra")
                )
            )
        }
        try {
            ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java)).use {
                esperar("No se pudo actualizar el catálogo. Mostrando productos guardados.")
                compose.onNodeWithTag("buscar-catalogo").performTextInput("  SAMSUNG  ")
                pulsar("Filtros")
                compose.onNodeWithTag("precio-minimo").performTextInput("200")
                compose.onNodeWithTag("precio-maximo").performTextInput("350")
                pulsar("Solo favoritos")
                pulsar("Orden original")
                compose.onNodeWithText("Precio: mayor a menor").performClick()
                esperar("1 coincidencia")
                compose.capturarMejora("m3", "filtros-offline")
                pulsar("Filtros")
                compose.onNodeWithTag("lista-catalogo").performScrollToNode(hasText("Samsung Galaxy A"))
                compose.onNodeWithText("Samsung Galaxy A").assertIsDisplayed()
                compose.onNodeWithText("Samsung Galaxy B").assertDoesNotExist()
                compose.capturarMejora("m3", "resultado-offline")

                compose.onNodeWithTag("lista-catalogo").performScrollToIndex(0)
                compose.onNodeWithTag("buscar-catalogo").performTextReplacement("inexistente")
                esperar("No hay productos que coincidan con tus filtros.")
                pulsar("Limpiar filtros")
                esperar("4 coincidencias")
                assertEquals(consultasIniciales + 1, app.api.consultasLista.get())
                runBlocking {
                    assertEquals(4, app.container.itemDao.contarItems())
                    assertTrue(app.container.itemDao.obtenerPorId(31)?.favorito == true)
                }
            }
        } finally {
            app.auth.actual = sesionAnterior
            app.api.errorLista = errorAnterior
            app.container.database.clearAllTables()
        }
    }

    private fun pulsar(texto: String) {
        compose.onNodeWithTag("lista-catalogo").performScrollToNode(hasText(texto))
        compose.onNodeWithText(texto).performClick()
    }

    private fun esperar(texto: String) {
        compose.waitUntil(20_000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun producto(id: Int, nombre: String, precio: Double, favorito: Boolean, imagen: String) =
        ItemEntity(id, nombre, "Producto de prueba almacenado en Room.", precio, "smartphone_$imagen", "smartphones", favorito)
}
