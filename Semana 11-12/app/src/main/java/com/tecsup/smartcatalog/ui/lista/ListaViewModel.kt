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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListaViewModel(private val repository: ItemRepository) : ViewModel() {
    private val estadoMutable = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Item>>> = estadoMutable.asStateFlow()
    private val criterios = MutableStateFlow(CriteriosCatalogo())
    internal val vistaCatalogo = combine(estado, criterios) { estado, criterios ->
        val productos = (estado as? UiState.Exito)?.datos.orEmpty()
        VistaCatalogo(estado, criterios, criterios.aplicar(productos))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VistaCatalogo(UiState.Cargando, criterios.value, emptyList()))
    private val actualizandoMutable = MutableStateFlow(false)
    val actualizando: StateFlow<Boolean> = actualizandoMutable.asStateFlow()
    private val mensajeMutable = MutableStateFlow<String?>(null)
    val mensajeActualizacion: StateFlow<String?> = mensajeMutable.asStateFlow()
    private val sincronizacionMutable = MutableStateFlow<EstadoSincronizacion>(EstadoSincronizacion.Inicial)
    val sincronizacion = sincronizacionMutable.asStateFlow()
    private var consulta: Job? = null
    private var cargaInicialCompleta = false
    private var productosLocales: List<Item>? = null
    private var errorSincronizacion: Exception? = null

    init {
        viewModelScope.launch {
            try {
                repository.items.collect { items ->
                    productosLocales = items
                    if (items.isNotEmpty() || (cargaInicialCompleta && errorSincronizacion == null)) {
                        estadoMutable.value = UiState.Exito(items)
                        mensajeMutable.value = errorSincronizacion?.let { mensajeDeError(it) }
                    } else if (errorSincronizacion != null) {
                        estadoMutable.value = UiState.Error(mensajeDeError(requireNotNull(errorSincronizacion)))
                    }
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

    internal fun actualizarCriterios(cambio: (CriteriosCatalogo) -> CriteriosCatalogo) = criterios.update(cambio)

    fun alternarFavorito(id: Int) {
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

    private fun consultar() {
        if (consulta?.isActive == true) return
        mensajeMutable.value = null
        errorSincronizacion = null
        cargaInicialCompleta = false
        actualizandoMutable.value = true
        val falloAnterior = (sincronizacionMutable.value as? EstadoSincronizacion.Fallo)?.mensaje
            ?: (sincronizacionMutable.value as? EstadoSincronizacion.Actualizando)?.falloAnterior
        sincronizacionMutable.value = EstadoSincronizacion.Actualizando(falloAnterior)
        if (productosLocales.isNullOrEmpty()) estadoMutable.value = UiState.Cargando
        consulta = viewModelScope.launch {
            try {
                repository.refrescar().getOrThrow()
                val productos = repository.items.first()
                productosLocales = productos
                cargaInicialCompleta = true
                sincronizacionMutable.value = EstadoSincronizacion.Correcta
                estadoMutable.value = UiState.Exito(productos)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                sincronizacionMutable.value = EstadoSincronizacion.Fallo(mensajeDeError(error))
                informarError(error)
            } finally {
                actualizandoMutable.value = false
            }
        }
    }

    private fun informarError(error: Exception) {
        cargaInicialCompleta = true
        errorSincronizacion = error
        val productos = productosLocales.orEmpty()
        if (productos.isNotEmpty()) {
            estadoMutable.value = UiState.Exito(productos)
            mensajeMutable.value = mensajeDeError(error)
        } else estadoMutable.value = UiState.Error(mensajeDeError(error))
    }

    private fun mensajeDeError(error: Exception): String = error.message?.takeIf { it.isNotBlank() }
        ?: "No pudimos cargar los smartphones. Inténtalo nuevamente."

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
