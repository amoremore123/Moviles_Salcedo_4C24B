package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificacionesViewModel(
    private val repository: AyudaRepository,
    private val usuarioId: Int
) : ViewModel() {
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _noLeidasOriginales = repository.observarNotificacionesNoLeidas(usuarioId)
        .onEach { _cargando.value = false; _error.value = null }
        .catch {
            _error.value = "No se pudieron cargar las notificaciones"
            _cargando.value = false
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notificaciones: StateFlow<List<Notificacion>> = repository.observarNotificaciones(usuarioId)
        .onEach { _cargando.value = false }
        .catch {
            _error.value = "No se pudieron cargar las notificaciones"
            _cargando.value = false
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cantidadNoLeidas: StateFlow<Int> = repository.contarNotificacionesNoLeidas(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun marcarTodoComoLeido() {
        viewModelScope.launch {
            try {
                repository.marcarNotificacionesComoLeidas(usuarioId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudieron marcar como leidas"
            }
        }
    }

    fun marcarComoLeida(id: Int) {
        viewModelScope.launch {
            try {
                repository.marcarNotificacionComoLeida(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo marcar como leida"
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarNotificacion(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo eliminar la notificacion"
            }
        }
    }

    class Factory(
        private val repository: AyudaRepository,
        private val usuarioId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NotificacionesViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return NotificacionesViewModel(repository, usuarioId) as T
        }
    }
}
