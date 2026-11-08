package com.tecsup.smartcatalog.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import com.tecsup.smartcatalog.ui.detalle.DetalleViewModel
import com.tecsup.smartcatalog.ui.lista.ListaViewModel
import java.io.IOException
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
class FavoritosViewModelsTest {
    private fun item(id: Int = 1, favorito: Boolean = false) = Item(id, "Modelo $id", "Descripción", 225.50, "", "smartphones", favorito)
    @Before fun preparar() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun restaurar() { Dispatchers.resetMain() }
    private fun cerrar(vararg vms: ViewModel) = vms.forEach { it.viewModelScope.cancel() }
    private fun favoritos(vm: FavoritosViewModel) = (vm.estado.value as UiState.Exito<List<Item>>).datos
    private fun lista(vm: ListaViewModel) = (vm.estado.value as UiState.Exito<List<Item>>).datos
    private fun detalle(vm: DetalleViewModel) = (vm.estado.value as UiState.Exito<Item>).datos

    @Test fun fabricaMuestraVacioSinConsultarHttp() = runTest {
        val repo = RepoFalso()
        val vm = FavoritosViewModel.Factory(repo).create(FavoritosViewModel::class.java)
        try {
            runCurrent(); assertTrue(favoritos(vm).isEmpty())
            assertEquals(0, repo.consultasLista + repo.consultasDetalle)
        } finally { cerrar(vm) }
    }

    @Test fun observaCambiosLocalesYQuitaElFavorito() = runTest {
        val repo = RepoFalso(listOf(item()))
        val vm = FavoritosViewModel(repo)
        try {
            runCurrent(); assertTrue(favoritos(vm).isEmpty())
            repo.alternarFavorito(1); runCurrent()
            assertEquals(listOf(item(favorito = true)), favoritos(vm))
            vm.quitarFavorito(1); runCurrent()
            assertTrue(favoritos(vm).isEmpty())
            assertEquals(0, repo.consultasLista + repo.consultasDetalle)
        } finally { cerrar(vm) }
    }

    @Test fun dosPulsacionesParaQuitarNoVuelvenAAgregarElProducto() = runTest {
        val repo = RepoFalso(listOf(item(favorito = true)))
        val vm = FavoritosViewModel(repo)
        try {
            runCurrent(); repeat(5) { vm.quitarFavorito(1) }; runCurrent()
            assertTrue(favoritos(vm).isEmpty())
            assertEquals(1, repo.alternancias)
        } finally { cerrar(vm) }
    }

    @Test fun errorAlQuitarConservaLaListaYExponeUnMensaje() = runTest {
        val repo = RepoFalso(listOf(item(favorito = true))).apply { fallaEscritura = true }
        val vm = FavoritosViewModel(repo)
        try {
            runCurrent(); vm.quitarFavorito(1); runCurrent()
            assertEquals(1, favoritos(vm).size)
            assertNotNull(vm.mensaje.value)
        } finally { cerrar(vm) }
    }

    @Test fun catalogoAlternaRapidamenteSinRecargarLaApi() = runTest {
        val repo = RepoFalso(listOf(item()))
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); repeat(31) { vm.alternarFavorito(1) }; runCurrent()
            assertTrue(lista(vm).single().favorito)
            assertEquals(31, repo.alternancias)
            assertEquals(1, repo.consultasLista)
        } finally { cerrar(vm) }
    }

    @Test fun detallePrefiereFavoritoLocalAlValorDeLaRespuestaRemota() = runTest {
        val repo = RepoFalso(listOf(item(favorito = true)))
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertTrue(detalle(vm).favorito)
            assertEquals("Detalle remoto", detalle(vm).nombre)
            vm.alternarFavorito(); runCurrent()
            assertFalse(detalle(vm).favorito)
            assertEquals(1, repo.consultasDetalle)
            assertEquals(0, repo.consultasLista)
        } finally { cerrar(vm) }
    }

    @Test fun lasTresPantallasCompartenElEstadoLocal() = runTest {
        val repo = RepoFalso(listOf(item()))
        val catalogo = ListaViewModel(repo)
        val guardados = FavoritosViewModel(repo)
        val ficha = DetalleViewModel(repo, 1)
        try {
            runCurrent(); ficha.alternarFavorito(); runCurrent()
            assertTrue(lista(catalogo).single().favorito)
            assertEquals(1, favoritos(guardados).size)
            guardados.quitarFavorito(1); runCurrent()
            assertFalse(lista(catalogo).single().favorito)
            assertFalse(detalle(ficha).favorito)
            assertTrue(favoritos(guardados).isEmpty())
            assertEquals(1, repo.consultasLista)
            assertEquals(1, repo.consultasDetalle)
        } finally { cerrar(catalogo, guardados, ficha) }
    }

    @Test fun favoritoLocalNoOcultaUnFalloDelDetalleHttp() = runTest {
        val repo = RepoFalso(listOf(item(favorito = true))).apply { fallaDetalle = true }
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Error)
            repo.alternarFavorito(1); runCurrent()
            assertTrue(vm.estado.value is UiState.Error)
        } finally { cerrar(vm) }
    }

    @Test fun emisionLocalNoResucitaUnDetalleAnteriorTrasUnReintentoFallido() = runTest {
        val repo = RepoFalso(listOf(item()))
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertTrue(vm.estado.value is UiState.Exito)
            repo.fallaDetalle = true; vm.reintentar(); runCurrent()
            repo.alternarFavorito(1); runCurrent()
            assertTrue(vm.estado.value is UiState.Error)
            assertEquals(2, repo.consultasDetalle)
        } finally { cerrar(vm) }
    }

    @Test fun productoAusenteNoIntentaEscribirNiCierraElDetalle() = runTest {
        val repo = RepoFalso()
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); assertFalse(vm.puedeAlternarFavorito.value)
            vm.alternarFavorito(); runCurrent()
            assertTrue(vm.estado.value is UiState.Exito)
            assertEquals(0, repo.alternancias)
        } finally { cerrar(vm) }
    }

    @Test fun errorDeEscrituraEnDetalleNoModificaElCorazon() = runTest {
        val repo = RepoFalso(listOf(item())).apply { fallaEscritura = true }
        val vm = DetalleViewModel(repo, 1)
        try {
            runCurrent(); vm.alternarFavorito(); runCurrent()
            assertFalse(detalle(vm).favorito)
            assertNotNull(vm.mensajeFavorito.value)
        } finally { cerrar(vm) }
    }

    @Test fun errorDeEscrituraEnCatalogoConservaLosProductos() = runTest {
        val repo = RepoFalso(listOf(item())).apply { fallaEscritura = true }
        val vm = ListaViewModel(repo)
        try {
            runCurrent(); vm.alternarFavorito(1); runCurrent()
            assertEquals(listOf(item()), lista(vm))
            assertNotNull(vm.mensajeActualizacion.value)
        } finally { cerrar(vm) }
    }

    private class RepoFalso(iniciales: List<Item> = emptyList()) : ItemRepository {
        val memoria = MutableStateFlow(iniciales)
        override val items = memoria
        override val favoritos = memoria.map { it.filter { producto -> producto.favorito } }
        var consultasLista = 0
        var consultasDetalle = 0
        var alternancias = 0
        var fallaEscritura = false
        var fallaDetalle = false
        override suspend fun refrescar(): Result<Int> { consultasLista++; return Result.success(memoria.value.size) }
        override suspend fun obtenerDetalle(id: Int): Result<Item> {
            consultasDetalle++
            return if (fallaDetalle) Result.failure(IOException("Sin red"))
            else Result.success(Item(id, "Detalle remoto", "Respuesta HTTP falsa", 300.0, "", "smartphones", false))
        }
        override suspend fun alternarFavorito(id: Int) {
            if (fallaEscritura) throw IllegalStateException("Fallo de prueba")
            alternancias++
            memoria.update { lista -> lista.map { if (it.id == id) it.copy(favorito = !it.favorito) else it } }
        }
        override suspend fun cantidadGuardada() = memoria.value.size
        override suspend fun cantidadFavoritos() = memoria.value.count { it.favorito }
        override suspend fun limpiarCache() { memoria.value = emptyList() }
    }
}
