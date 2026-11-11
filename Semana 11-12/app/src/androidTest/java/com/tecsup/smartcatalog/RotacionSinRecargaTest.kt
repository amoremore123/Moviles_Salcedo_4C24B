package com.tecsup.smartcatalog

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tecsup.smartcatalog.testing.SmartCatalogTestApplication
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RotacionSinRecargaTest {
    @Test fun recrearActividadConConsultaPendienteConservaViewModelYNoDuplicaHttp() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<SmartCatalogTestApplication>()
        app.container.database.clearAllTables()
        app.api.consultasLista.set(0)
        app.api.consultasDetalle.set(0)
        val pendiente = CompletableDeferred<Unit>()
        app.api.pendiente = pendiente
        val escenario = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java))
        try {
            withTimeout(20_000) { while (app.api.consultasLista.get() == 0) delay(20) }
            lateinit var original: ListaViewModel
            escenario.onActivity { original = ViewModelProvider(it, app.container.listaViewModelFactory)[ListaViewModel::class.java] }
            assertTrue(original.actualizando.value)
            escenario.recreate()
            escenario.onActivity {
                val retenido = ViewModelProvider(it, app.container.listaViewModelFactory)[ListaViewModel::class.java]
                assertSame(original, retenido)
            }
            assertEquals(1, app.api.consultasLista.get())
            pendiente.complete(Unit)
            withTimeout(20_000) { original.estado.first { it is UiState.Exito } }
            app.container.itemRepository.alternarFavorito(1)
            withTimeout(20_000) {
                original.estado.first { it is UiState.Exito && it.datos.any { item -> item.favorito } }
            }
            assertEquals(1, app.api.consultasLista.get()) // La recomposición por Room tampoco vuelve a consultar.
            assertEquals(0, app.api.consultasDetalle.get())
        } finally {
            pendiente.complete(Unit)
            app.api.pendiente = null
            escenario.close()
        }
    }
}
