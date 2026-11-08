package com.tecsup.smartcatalog.ui.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.repository.ItemRepository
import com.tecsup.smartcatalog.domain.model.Item
import com.tecsup.smartcatalog.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FavoritosViewModel(private val repository: ItemRepository) : ViewModel() {
    private val estadoMutable = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado = estadoMutable.asStateFlow()
    private val mensajeMutable = MutableStateFlow<String?>(null)
    val mensaje = mensajeMutable.asStateFlow()
    private val cambios = Mutex()
    private var observacion: Job? = null

    init { observar() }

    fun reintentar() = observar()

    private fun observar() {
        if (observacion?.isActive == true) return
        estadoMutable.value = UiState.Cargando
        observacion = viewModelScope.launch {
            try {
                repository.favoritos.collect { estadoMutable.value = UiState.Exito(it) }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                estadoMutable.value = UiState.Error("No se pudieron cargar tus favoritos. Inténtalo nuevamente.")
            }
        }
    }

    fun quitarFavorito(id: Int) {
        viewModelScope.launch {
            cambios.withLock {
                mensajeMutable.value = null
                try {
                    // Releer dentro del mutex: dos pulsaciones para quitar no lo vuelven a agregar.
                    if (repository.favoritos.first().any { it.id == id }) repository.alternarFavorito(id)
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (_: Exception) {
                    mensajeMutable.value = "No se pudo quitar el favorito. Inténtalo nuevamente."
                }
            }
        }
    }

    class Factory(private val repository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FavoritosViewModel::class.java)) { "Esta fábrica solo crea FavoritosViewModel." }
            return FavoritosViewModel(repository) as T
        }
    }
}
