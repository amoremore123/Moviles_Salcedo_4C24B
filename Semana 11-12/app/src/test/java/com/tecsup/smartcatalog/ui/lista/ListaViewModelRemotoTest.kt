package com.tecsup.smartcatalog.ui.lista

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.DatosPrueba
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
class ListaViewModelRemotoTest {
    private val ejemplos = DatosPrueba.smartphones.take(2)

    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }

    @Test fun cargaInicialYConsultasSimultaneasNoSeDuplican() = runTest {
        val pendiente = CompletableDeferred<List<Item>>()
        val repo = RepositorioFalso { Result.success(pendiente.await()) }
        val vm = ListaViewModel(repo)
        try {
            assertEquals(UiState.Cargando, vm.estado.value)
            runCurrent()
            assertTrue(vm.actualizando.value)
            vm.actualizar(); vm.reintentar(); runCurrent()
            assertEquals(1, repo.consultas)
            pendiente.complete(ejemplos); runCurrent()
            assertEquals(ejemplos, datos(vm))
            assertFalse(vm.actualizando.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun exitoUsaLosProductosDelContratoYObservaSusCambios() = runTest {
        val repo = RepositorioFalso { Result.success(ejemplos.take(1)) }
        val vm = ListaViewModel(repo)
        try {
            runCurrent()
            assertEquals(ejemplos.take(1), datos(vm))
            repo.memoria.value = ejemplos; runCurrent()
            assertEquals(ejemplos, datos(vm))
            assertEquals(1, repo.consultas)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun errorInicialNoSeConvierteEnExitoVacio() = runTest {
        val vm = ListaViewModel(RepositorioFalso { Result.failure(IOException("Sin conexión")) })
        try {
            runCurrent()
            assertEquals(UiState.Error("Sin conexión"), vm.estado.value)
            assertFalse(vm.actualizando.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun reintentoConsultaOtraVezYRecuperaElCatalogo() = runTest {
        var falla = true
        val repo = RepositorioFalso { if (falla) Result.failure(IOException("Sin conexión")) else Result.success(ejemplos) }
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Error)
            falla = false; vm.reintentar(); runCurrent()
            assertEquals(ejemplos, datos(vm))
            assertEquals(2, repo.consultas)
            assertNull(vm.mensajeActualizacion.value)
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun respuestaVaciaCorrectaEsExitoVacio() = runTest {
        val vm = ListaViewModel(RepositorioFalso { Result.success(emptyList()) })
        try {
            runCurrent()
            assertTrue(vm.estado.value is UiState.Exito)
            assertTrue(datos(vm).isEmpty())
        } finally { vm.viewModelScope.cancel() }
    }

    @Test fun falloDeActualizacionConservaDatosYPermiteRecuperarse() = runTest {
        val repo = RepositorioFalso { Result.success(ejemplos) }
        val vm = ListaViewModel(repo)
        try {
            runCurrent()
            val pendiente = CompletableDeferred<Result<List<Item>>>()
            repo.accion = { pendiente.await() }
            vm.actualizar(); runCurrent()
            assertTrue(vm.actualizando.value)
            assertEquals(ejemplos, datos(vm))
            vm.actualizar(); runCurrent(); assertEquals(2, repo.consultas)
            pendiente.complete(Result.failure(IOException("Sin conexión"))); runCurrent()
            assertEquals(ejemplos, datos(vm))
            assertEquals("Sin conexión", vm.mensajeActualizacion.value)
            assertFalse(vm.actualizando.value)
            repo.accion = { Result.success(DatosPrueba.smartphones.take(3)) }
            vm.actualizar(); runCurrent()
            assertEquals(3, datos(vm).size)
            assertNull(vm.mensajeActualizacion.value)
        } finally { vm.viewModelScope.cancel() }
    }

    private fun datos(vm: ListaViewModel): List<Item> = (vm.estado.value as UiState.Exito<List<Item>>).datos

    private class RepositorioFalso(var accion: suspend () -> Result<List<Item>>) : ItemRepository {
        val memoria = MutableStateFlow<List<Item>>(emptyList())
        var consultas = 0
        override val items = memoria
        override val favoritos = MutableStateFlow<List<Item>>(emptyList())
        override suspend fun refrescar(): Result<Int> {
            consultas++
            return accion().map { memoria.value = it; it.size }
        }
        override suspend fun obtenerDetalle(id: Int): Result<Item> = memoria.value.firstOrNull { it.id == id }
            ?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())
        override suspend fun alternarFavorito(id: Int) = Unit
        override suspend fun cantidadGuardada(): Int = memoria.value.size
        override suspend fun cantidadFavoritos(): Int = 0
        override suspend fun limpiarCache() { memoria.value = emptyList() }
    }
}
