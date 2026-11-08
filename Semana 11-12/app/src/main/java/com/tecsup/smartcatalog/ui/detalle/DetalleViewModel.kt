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
    private val mensajeMutable = MutableStateFlow<String?>(null)
    val mensajeFavorito = mensajeMutable.asStateFlow()
    private val habilitadoMutable = MutableStateFlow(false)
    val puedeAlternarFavorito = habilitadoMutable.asStateFlow()
    private var detalleRemoto: Item? = null
    private var favoritoLocal = false
    private var observacion: Job? = null
    private var consulta: Job? = null

    init { observarFavorito(); consultar() }

    fun reintentar() { observarFavorito(); consultar() }

    private fun observarFavorito() {
        if (id <= 0 || observacion?.isActive == true) return
        observacion = viewModelScope.launch {
            try {
                repository.items.collect { items ->
                    val local = items.firstOrNull { it.id == id }
                    favoritoLocal = local?.favorito ?: false
                    habilitadoMutable.value = local != null
                    publicarDetalle()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                habilitadoMutable.value = false
                mensajeMutable.value = "No se pudo consultar el estado de favorito. Inténtalo nuevamente."
            }
        }
    }

    fun alternarFavorito() {
        if (!habilitadoMutable.value) return
        mensajeMutable.value = null
        viewModelScope.launch {
            try {
                repository.alternarFavorito(id)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                mensajeMutable.value = "No se pudo actualizar el favorito. Inténtalo nuevamente."
            }
        }
    }

    private fun publicarDetalle() {
        detalleRemoto?.let { estadoMutable.value = UiState.Exito(it.copy(favorito = favoritoLocal)) }
    }

    private fun consultar() {
        if (id <= 0) {
            estadoMutable.value = UiState.Error("El identificador del smartphone no es válido.")
            return
        }
        if (consulta?.isActive == true) return
        detalleRemoto = null // Una emisión local no debe convertir un reintento HTTP fallido en éxito.
        estadoMutable.value = UiState.Cargando
        consulta = viewModelScope.launch {
            try {
                val producto = repository.obtenerDetalle(id).getOrThrow()
                require(producto.id == id) { "El detalle recibido no corresponde al producto solicitado." }
                detalleRemoto = producto
                publicarDetalle()
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
