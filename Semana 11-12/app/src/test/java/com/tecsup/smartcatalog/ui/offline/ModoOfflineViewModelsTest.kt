package com.tecsup.smartcatalog.ui.offline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.DetalleConOrigen
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.domain.model.OrigenDetalle
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel
import com.tecsup.smartcatalog.ui.lista.EstadoSincronizacion
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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
class ModoOfflineViewModelsTest {
    private val local = Item(1, "Guardado", "Descripción guardada", 100.0, "", "smartphones", true)
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }
    private fun cerrar(vararg vms: ViewModel) = vms.forEach { it.viewModelScope.cancel() }
    private fun datos(vm: ListaViewModel) = (vm.estado.value as UiState.Exito<List<Item>>).datos
    private fun ficha(vm: DetalleViewModel) = (vm.estado.value as UiState.Exito<Item>).datos

    @Test fun primeraCargaSinCacheYFalloNoInventaProductos() = runTest {
        val repo = RepoFalso().apply { sincronizar = { Result.failure(IOException("Sin servidor")) } }
        val vm = ListaViewModel(repo)
        try { runCurrent(); assertTrue(vm.estado.value is UiState.Error); assertTrue(vm.sincronizacion.value is EstadoSincronizacion.Fallo); assertTrue(repo.memoria.value.isEmpty()) }
        finally { cerrar(vm) }
    }

    @Test fun datosLocalesDuranteCargaNormalNoSeEtiquetanComoFallo() = runTest {
        val pendiente = CompletableDeferred<Result<Int>>()
        val vm = ListaViewModel(RepoFalso(listOf(local)).apply { sincronizar = { pendiente.await() } })
        try {
            runCurrent(); assertEquals(listOf(local), datos(vm))
            assertNull((vm.sincronizacion.value as EstadoSincronizacion.Actualizando).falloAnterior)
            pendiente.complete(Result.success(1)); runCurrent()
            assertEquals(EstadoSincronizacion.Correcta, vm.sincronizacion.value)
        } finally { cerrar(vm) }
    }

    @Test fun falloConCacheSeConservaDuranteReintentoYSeQuitaSoloTrasExito() = runTest {
        val repo = RepoFalso(listOf(local)).apply { sincronizar = { Result.failure(IOException("Sin servidor")) } }
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); assertEquals(listOf(local), datos(vm)); assertTrue(vm.sincronizacion.value is EstadoSincronizacion.Fallo)
            val pendiente = CompletableDeferred<Result<Int>>()
            repo.sincronizar = { pendiente.await() }; vm.reintentar(); runCurrent()
            assertEquals("Sin servidor", (vm.sincronizacion.value as EstadoSincronizacion.Actualizando).falloAnterior)
            assertEquals(listOf(local), datos(vm))
            pendiente.complete(Result.success(1)); runCurrent()
            assertEquals(EstadoSincronizacion.Correcta, vm.sincronizacion.value); assertNull(vm.mensajeActualizacion.value)
        } finally { cerrar(vm) }
    }

    @Test fun listaVaciaValidaEsExitoYNoUnErrorDePrimeraDescarga() = runTest {
        val vm = ListaViewModel(RepoFalso())
        try { runCurrent(); assertTrue(datos(vm).isEmpty()); assertEquals(EstadoSincronizacion.Correcta, vm.sincronizacion.value) }
        finally { cerrar(vm) }
    }

    @Test fun reintentosSimultaneosNoDuplicanSincronizacion() = runTest {
        val pendiente = CompletableDeferred<Result<Int>>()
        val repo = RepoFalso().apply { sincronizar = { pendiente.await() } }
        val vm = ListaViewModel(repo)
        try { runCurrent(); repeat(10) { vm.reintentar(); vm.actualizar() }; runCurrent(); assertEquals(1, repo.consultasLista); pendiente.complete(Result.success(0)); runCurrent() }
        finally { cerrar(vm) }
    }

    @Test fun errorAlMarcarFavoritoNoSeConfundeConFalloDelServidor() = runTest {
        val repo = RepoFalso(listOf(local)).apply { fallaFavorito = true }
        val vm = ListaViewModel(repo)
        try { runCurrent(); vm.alternarFavorito(1); runCurrent(); assertNotNull(vm.mensajeActualizacion.value); assertEquals(EstadoSincronizacion.Correcta, vm.sincronizacion.value) }
        finally { cerrar(vm) }
    }

    @Test fun emisionLocalTardiaConservaElFalloRealDeSincronizacion() = runTest {
        val repo = RepoFalso().apply { sincronizar = { Result.failure(IOException("Sin servidor")) } }
        val vm = ListaViewModel(repo)
        try { runCurrent(); repo.memoria.value = listOf(local); runCurrent(); assertEquals(listOf(local), datos(vm)); assertTrue(vm.sincronizacion.value is EstadoSincronizacion.Fallo) }
        finally { cerrar(vm) }
    }

    @Test fun detalleRemotoExitosoTieneOrigenRemotoYFavoritoLocal() = runTest {
        val vm = DetalleViewModel(RepoFalso(listOf(local)), 1)
        try { runCurrent(); assertEquals(OrigenDetalle.REMOTO, vm.origen.value); assertEquals("Remoto", ficha(vm).nombre); assertTrue(ficha(vm).favorito) }
        finally { cerrar(vm) }
    }

    @Test fun respaldoLocalSeIdentificaYPermiteCambiarFavoritoSinHttpAdicional() = runTest {
        val repo = RepoFalso(listOf(local)).apply { fallaDetalle = true }
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertEquals(OrigenDetalle.LOCAL, vm.origen.value); assertEquals(local, ficha(vm))
            vm.alternarFavorito(); runCurrent(); assertFalse(ficha(vm).favorito)
            assertEquals(OrigenDetalle.LOCAL, vm.origen.value); assertEquals(1, repo.consultasDetalle)
        } finally { cerrar(vm) }
    }

    @Test fun reintentoRemotoRecuperadoEliminaOrigenLocal() = runTest {
        val repo = RepoFalso(listOf(local)).apply { fallaDetalle = true }
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertEquals(OrigenDetalle.LOCAL, vm.origen.value)
            repo.fallaDetalle = false; vm.reintentar(); runCurrent()
            assertEquals(OrigenDetalle.REMOTO, vm.origen.value); assertEquals("Remoto", ficha(vm).nombre)
            assertTrue(ficha(vm).favorito); assertEquals(2, repo.consultasDetalle)
        } finally { cerrar(vm) }
    }

    @Test fun sinRespaldoMuestraErrorYRequiereReintentoAunqueLleguenDatosLocales() = runTest {
        val repo = RepoFalso().apply { fallaDetalle = true }
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Error); assertNull(vm.origen.value)
            repo.memoria.value = listOf(local); runCurrent(); assertTrue(vm.estado.value is UiState.Error)
            vm.reintentar(); runCurrent(); assertEquals(OrigenDetalle.LOCAL, vm.origen.value); assertEquals(local, ficha(vm))
        } finally { cerrar(vm) }
    }

    private class RepoFalso(iniciales: List<Item> = emptyList()) : ItemRepository {
        val memoria = MutableStateFlow(iniciales)
        override val items = memoria
        override val favoritos = memoria.map { it.filter { producto -> producto.favorito } }
        var consultasLista = 0
        var consultasDetalle = 0
        var fallaFavorito = false
        var fallaDetalle = false
        var sincronizar: suspend () -> Result<Int> = { Result.success(memoria.value.size) }
        override suspend fun refrescar(): Result<Int> { consultasLista++; return sincronizar() }
        override suspend fun obtenerDetalle(id: Int): Result<Item> {
            consultasDetalle++
            return if (fallaDetalle) Result.failure(IOException("Sin servidor"))
            else Result.success(Item(id, "Remoto", "Detalle remoto", 200.0, "", "smartphones"))
        }
        override suspend fun obtenerDetalleConRespaldo(id: Int): Result<DetalleConOrigen> {
            val respuesta = obtenerDetalle(id)
            respuesta.getOrNull()?.let { return Result.success(DetalleConOrigen(it, OrigenDetalle.REMOTO)) }
            return memoria.value.firstOrNull { it.id == id }?.let { Result.success(DetalleConOrigen(it, OrigenDetalle.LOCAL)) }
                ?: Result.failure(requireNotNull(respuesta.exceptionOrNull()))
        }
        override suspend fun alternarFavorito(id: Int) {
            if (fallaFavorito) throw IllegalStateException("Fallo de prueba")
            memoria.update { lista -> lista.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }
        }
        override suspend fun cantidadGuardada() = memoria.value.size
        override suspend fun cantidadFavoritos() = memoria.value.count { it.favorito }
        override suspend fun limpiarCache() { memoria.value = emptyList() }
    }
}
