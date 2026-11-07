package com.tecsup.smartcatalog.ui.lista

import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.data.repository.ItemRepositoryPrueba
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListaViewModelTest {
    @Before
    fun prepararDispatcherPrincipal() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarDispatcherPrincipal() {
        Dispatchers.resetMain()
    }

    @Test
    fun pasaDeCargaALosCuatroProductosDelRepositorio() = runTest {
        val viewModel = ListaViewModel(ItemRepositoryPrueba())
        try {
            assertEquals(UiState.Cargando, viewModel.estado.value)
            runCurrent()
            assertEquals(4, datos(viewModel).size)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun reintentarRecuperaUnErrorDelRepositorio() = runTest {
        val base = ItemRepositoryPrueba()
        var intentos = 0
        val repository = object : ItemRepository by base {
            override suspend fun refrescar(): Result<Int> {
                intentos++
                return if (intentos == 1) Result.failure(IllegalStateException("Carga no disponible"))
                else base.refrescar()
            }
        }
        val viewModel = ListaViewModel(repository)
        try {
            runCurrent()
            assertTrue(viewModel.estado.value is UiState.Error)
            viewModel.reintentar()
            runCurrent()
            assertEquals(2, intentos)
            assertEquals(4, datos(viewModel).size)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun observaLaListaVaciaYRecargaSinCrearOtroViewModel() = runTest {
        val repository = ItemRepositoryPrueba()
        val viewModel = ListaViewModel(repository)
        try {
            runCurrent()
            repository.limpiarCache()
            runCurrent()
            assertTrue(datos(viewModel).isEmpty())
            viewModel.reintentar()
            runCurrent()
            assertEquals(4, datos(viewModel).size)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private fun datos(viewModel: ListaViewModel): List<Item> =
        (viewModel.estado.value as UiState.Exito<List<Item>>).datos
}
