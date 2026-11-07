package com.tecsup.smartcatalog.ui.detalle

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
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
class DetalleViewModelTest {
    private fun producto(id: Int) = Item(id, "Modelo $id", "Descripción completa", 225.50, "", "smartphones")
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }

    @Test fun permaneceCargandoYNoDuplicaUnaConsultaPendiente() = runTest {
        val pendiente = CompletableDeferred<Result<Item>>()
        val repo = RepositorioFalso { pendiente.await() }
        val vm = DetalleViewModel(repo, 73)
        try {
            assertEquals(UiState.Cargando, vm.estado.value)
            runCurrent()
            vm.reintentar(); vm.reintentar(); runCurrent()
            assertEquals(listOf(73), repo.solicitados)
            assertEquals(UiState.Cargando, vm.estado.value)
            pendiente.complete(Result.success(producto(73))); runCurrent()
            assertEquals(UiState.Exito(producto(73)), vm.estado.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun laFabricaUtilizaElIdSeleccionadoYDevuelveElProductoCorrecto() = runTest {
        val repo = RepositorioFalso { id -> Result.success(producto(id)) }
        val vm = DetalleViewModel.Factory(repo, 91).create(DetalleViewModel::class.java)
        try {
            runCurrent()
            assertEquals(listOf(91), repo.solicitados)
            assertEquals(UiState.Exito(producto(91)), vm.estado.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun muestraErrorCuandoFallaLaSolicitudSinUsarUnProductoDeLaLista() = runTest {
        val repo = RepositorioFalso { Result.failure(IOException("Sin conexión")) }
        val vm = DetalleViewModel(repo, 73)
        try {
            runCurrent()
            assertEquals(UiState.Error("Sin conexión"), vm.estado.value)
            assertEquals(listOf(73), repo.solicitados)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun reintentarRealizaUnaNuevaConsultaDelMismoIdYRecuperaElDetalle() = runTest {
        var falla = true
        val repo = RepositorioFalso { id -> if (falla) Result.failure(IOException("Sin conexión")) else Result.success(producto(id)) }
        val vm = DetalleViewModel(repo, 73)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Error)
            falla = false; vm.reintentar()
            assertEquals(UiState.Cargando, vm.estado.value)
            runCurrent()
            assertEquals(UiState.Exito(producto(73)), vm.estado.value)
            assertEquals(listOf(73, 73), repo.solicitados)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun losIdsInvalidosProducenErrorSinConsultarElRepositorio() = runTest {
        for (id in listOf(0, -1)) {
            val repo = RepositorioFalso { Result.success(producto(it)) }
            val vm = DetalleViewModel(repo, id)
            try {
                runCurrent(); vm.reintentar(); runCurrent()
                assertTrue(vm.estado.value is UiState.Error)
                assertTrue(repo.solicitados.isEmpty())
            } finally { vm.viewModelScope.cancel() }
        }
    }

    @Test fun noPresentaUnaRespuestaQueCorrespondeAOtroId() = runTest {
        val vm = DetalleViewModel(RepositorioFalso { Result.success(producto(99)) }, 73)
        try { runCurrent(); assertTrue(vm.estado.value is UiState.Error) }
        finally { vm.viewModelScope.cancel() }
    }

    private class RepositorioFalso(val accion: suspend (Int) -> Result<Item>) : ItemRepository {
        val solicitados = mutableListOf<Int>()
        override val items = MutableStateFlow<List<Item>>(emptyList())
        override val favoritos = MutableStateFlow<List<Item>>(emptyList())
        override suspend fun obtenerDetalle(id: Int): Result<Item> { solicitados.add(id); return accion(id) }
        override suspend fun refrescar(): Result<Int> = Result.success(items.value.size)
        override suspend fun alternarFavorito(id: Int) = Unit
        override suspend fun cantidadGuardada(): Int = items.value.size
        override suspend fun cantidadFavoritos(): Int = 0
        override suspend fun limpiarCache() { items.value = emptyList() }
    }
}
