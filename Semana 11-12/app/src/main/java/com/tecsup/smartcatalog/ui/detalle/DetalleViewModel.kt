package com.tecsup.smartcatalog.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(private val repository: ItemRepository, private val id: Int) : ViewModel() {
    private val estadoMutable = MutableStateFlow<UiState<Item>>(UiState.Cargando)
    val estado: StateFlow<UiState<Item>> = estadoMutable.asStateFlow()
    private var consulta: Job? = null

    init { consultar() }

    fun reintentar() = consultar()

    private fun consultar() {
        if (id <= 0) {
            estadoMutable.value = UiState.Error("El identificador del smartphone no es válido.")
            return
        }
        if (consulta?.isActive == true) return
        estadoMutable.value = UiState.Cargando
        consulta = viewModelScope.launch {
            try {
                val producto = repository.obtenerDetalle(id).getOrThrow()
                require(producto.id == id) { "El detalle recibido no corresponde al producto solicitado." }
                estadoMutable.value = UiState.Exito(producto)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                estadoMutable.value = UiState.Error(error.message?.takeIf { it.isNotBlank() }
                    ?: "No se pudo cargar el detalle. Inténtalo nuevamente.")
            }
        }
    }

    class Factory(private val repository: ItemRepository, private val id: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DetalleViewModel::class.java)) { "Esta fábrica solo crea DetalleViewModel." }
            return DetalleViewModel(repository, id) as T
        }
    }
}
