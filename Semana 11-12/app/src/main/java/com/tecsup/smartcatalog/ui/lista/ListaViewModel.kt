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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ListaViewModel(private val repository: ItemRepository) : ViewModel() {
    private val estadoMutable = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = estadoMutable.asStateFlow()
    private val actualizandoMutable = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = actualizandoMutable.asStateFlow()
    private val mensajeMutable = MutableStateFlow<String?>(null)
    val mensajeActualizacion: StateFlow<String?> = mensajeMutable.asStateFlow()
    private var consulta: Job? = null
    private var cargaInicialCompleta = false

    init {
        viewModelScope.launch {
            try {
                repository.items.collect { items ->
                    if (cargaInicialCompleta) estadoMutable.value = UiState.Exito(items)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                informarError(error)
            }
        }
        consultar()
    }

    fun reintentar() = consultar()
    fun actualizar() = consultar()

    private fun consultar() {
        if (consulta?.isActive == true) return
        mensajeMutable.value = null
        actualizandoMutable.value = true
        if (!cargaInicialCompleta) estadoMutable.value = UiState.Cargando
        consulta = viewModelScope.launch {
            try {
                repository.refrescar().getOrThrow()
                val productos = repository.items.first()
                cargaInicialCompleta = true
                estadoMutable.value = UiState.Exito(productos)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                informarError(error)
            } finally {
                actualizandoMutable.value = false
            }
        }
    }

    private fun informarError(error: Exception) {
        val mensaje = error.message?.takeIf { it.isNotBlank() }
            ?: "No pudimos cargar los smartphones. Inténtalo nuevamente."
        if (cargaInicialCompleta) mensajeMutable.value = mensaje
        else estadoMutable.value = UiState.Error(mensaje)
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
