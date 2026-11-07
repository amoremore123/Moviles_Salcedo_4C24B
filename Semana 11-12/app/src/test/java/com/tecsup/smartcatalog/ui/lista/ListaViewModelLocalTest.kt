package com.tecsup.smartcatalog.ui.lista

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.DatosPrueba
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryPrueba
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
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
class ListaViewModelLocalTest {
    private val locales = DatosPrueba.smartphones.take(2)
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }

    @Test fun muestraDatosLocalesMientrasSincronizaYLosConservaSiFalla() = runTest {
        val pendiente = CompletableDeferred<Result<Int>>()
        val repo = RepoLocal().apply { almacen.tryEmit(locales); accion = { pendiente.await() } }
        val vm = ListaViewModel(repo)
        try {
            runCurrent()
            assertEquals(locales, datos(vm))
            assertTrue(vm.actualizando.value)
            vm.actualizar(); runCurrent(); assertEquals(1, repo.consultas)
            pendiente.complete(Result.failure(IOException("Sin conexión"))); runCurrent()
            assertEquals(locales, datos(vm))
            assertEquals("Sin conexión", vm.mensajeActualizacion.value)
            assertFalse(vm.actualizando.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun emisionVaciaInicialNoFinalizaLaPrimeraSincronizacion() = runTest {
        val pendiente = CompletableDeferred<Result<Int>>()
        val repo = RepoLocal().apply { almacen.tryEmit(emptyList()); accion = { pendiente.await() } }
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); assertEquals(UiState.Cargando, vm.estado.value)
            repo.almacen.emit(locales); runCurrent()
            assertEquals(locales, datos(vm))
            assertTrue(vm.actualizando.value)
            pendiente.complete(Result.success(2)); runCurrent()
            assertEquals(locales, datos(vm))
            assertFalse(vm.actualizando.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun datosLocalesTardiosSeMuestranAunqueLaRedYaHayaFallado() = runTest {
        val repo = RepoLocal()
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Error)
            repo.almacen.emit(locales); runCurrent()
            assertEquals(locales, datos(vm))
            assertEquals("Sin conexión", vm.mensajeActualizacion.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun emisionVaciaTardiaNoOcultaUnErrorInicial() = runTest {
        val repo = RepoLocal()
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); repo.almacen.emit(emptyList()); runCurrent()
            assertEquals(UiState.Error("Sin conexión"), vm.estado.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun reintentoPendienteNoTrataOtraEmisionVaciaComoExito() = runTest {
        val repo = RepoLocal()
        val vm = ListaViewModel(repo)
        try {
            runCurrent()
            val pendiente = CompletableDeferred<Result<Int>>()
            repo.accion = { pendiente.await() }
            vm.reintentar(); runCurrent()
            repo.almacen.emit(emptyList()); runCurrent()
            assertEquals(UiState.Cargando, vm.estado.value)
            pendiente.complete(Result.success(0)); runCurrent()
            assertTrue(datos(vm).isEmpty())
        } finally { vm.viewModelScope.cancel() }
    }

    private fun datos(vm: ListaViewModel) = (vm.estado.value as UiState.Exito<List<Item>>).datos

    private class RepoLocal : ItemRepository by ItemRepositoryPrueba() {
        val almacen = MutableSharedFlow<List<Item>>(replay = 1)
        override val items = almacen
        var consultas = 0
        var accion: suspend () -> Result<Int> = { Result.failure(IOException("Sin conexión")) }
        override suspend fun refrescar(): Result<Int> { consultas++; return accion() }
    }
}
