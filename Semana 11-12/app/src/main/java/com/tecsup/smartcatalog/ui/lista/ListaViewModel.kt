package com.tecsup.smartcatalog.ui.lista

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

class ListaViewModel(private val repository: ItemRepository) : ViewModel() {
    private val estadoMutable = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = estadoMutable.asStateFlow()
    private var carga: Job? = null

    init {
        cargar()
    }

    fun reintentar() {
        cargar()
    }

    private fun cargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            estadoMutable.value = UiState.Cargando
            try {
                repository.refrescar().getOrThrow()
                repository.items.collect { items ->
                    estadoMutable.value = UiState.Exito(items)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                estadoMutable.value = UiState.Error(
                    error.message ?: "No pudimos cargar los smartphones. Inténtalo nuevamente."
                )
            }
        }
    }

    class Factory(private val repository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ListaViewModel::class.java)) {
                "Esta fábrica solo crea ListaViewModel."
            }
            return ListaViewModel(repository) as T
        }
    }
}
