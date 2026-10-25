package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Notificacion
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SoporteViewModel(
    private val repository: AyudaRepository,
    private val usuarioAdmin: StateFlow<Usuario?>
) : ViewModel() {
    enum class FiltroEstado(val etiqueta: String, val estado: EstadoTicket?) {
        TODOS("Todos", null), ABIERTOS("Abiertos", EstadoTicket.ABIERTO),
        EN_PROCESO("En proceso", EstadoTicket.EN_PROCESO),
        RESUELTOS("Resueltos", EstadoTicket.RESUELTO), CERRADOS("Cerrados", EstadoTicket.CERRADO)
    }

    enum class FiltroPrioridad(val etiqueta: String, val prioridad: Prioridad?) {
        TODOS("Todas", null), ALTA("Alta", Prioridad.ALTA),
        MEDIA("Media", Prioridad.MEDIA), BAJA("Baja", Prioridad.BAJA)
    }

    private val _filtroEstado = MutableStateFlow(FiltroEstado.TODOS)
    val filtroEstado: StateFlow<FiltroEstado> = _filtroEstado.asStateFlow()

    private val _filtroPrioridad = MutableStateFlow(FiltroPrioridad.TODOS)
    val filtroPrioridad: StateFlow<FiltroPrioridad> = _filtroPrioridad.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val contadorAltasSinAtender: StateFlow<Int> = repository.contarAltasSinAtender(86_400_000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val contadorPorEstado: Map<EstadoTicket, StateFlow<Int>> = EstadoTicket.entries.associateWith { estado ->
        repository.observarCantidadPorEstado(estado)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    }

    private val todosOriginales = repository.observarTodosLosTickets()
        .onEach { _cargando.value = false; _error.value = null }
        .catch {
            _error.value = "No se pudieron cargar los tickets"
            _cargando.value = false
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tickets: StateFlow<List<TicketConDetalle>> = combine(todosOriginales, _filtroEstado, _filtroPrioridad, _busqueda) { lista, filtroEst, filtroPri, texto ->
        val consulta = texto.trim()
        lista.filter { detalle ->
            val ticket = detalle.ticket
            val coincideEstado = filtroEst.estado == null || ticket.estado == filtroEst.estado.name
            val coincidePrioridad = filtroPri.prioridad == null || ticket.prioridad == filtroPri.prioridad.name
            val coincideBusqueda = consulta.isEmpty() ||
                ticket.titulo.contains(consulta, ignoreCase = true) ||
                ticket.id.toString().contains(consulta) ||
                ticket.id.toString().padStart(4, '0').contains(consulta) ||
                detalle.cliente.nombre.contains(consulta, ignoreCase = true)
            coincideEstado && coincidePrioridad && coincideBusqueda
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cambiarEstado(filtro: FiltroEstado) { _filtroEstado.value = filtro }
    fun cambiarPrioridad(filtro: FiltroPrioridad) { _filtroPrioridad.value = filtro }
    fun buscar(texto: String) { _busqueda.value = texto }

    fun cambiarEstadoTicket(ticketId: Int, nuevoEstado: EstadoTicket) {
        val admin = usuarioAdmin.value
        if (admin == null) return
        viewModelScope.launch {
            try {
                repository.cambiarEstado(ticketId, nuevoEstado)
                repository.generarNotificacion(
                    destinatarioId = obtenerClienteIdParaTicket(ticketId),
                    tipo = "TICKET_ACTUALIZADO",
                    origenId = admin.id,
                    origenNombre = admin.nombre,
                    ticketId = ticketId,
                    mensaje = "Tu ticket #${ticketId.toString().padStart(4, '0')} fue actualizado a ${nuevoEstado.name}"
                )
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo actualizar el estado" }
        }
    }

    fun cambiarPrioridadTicket(ticketId: Int, nuevaPrioridad: Prioridad) {
        viewModelScope.launch {
            try {
                repository.cambiarPrioridad(ticketId, nuevaPrioridad)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo cambiar la prioridad" }
        }
    }

    fun cerrarTicket(ticketId: Int, calificacion: Int) {
        viewModelScope.launch {
            try {
                repository.cerrarConCalificacion(ticketId, calificacion)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo cerrar el ticket" }
        }
    }

    fun reabrirTicket(ticketId: Int) {
        viewModelScope.launch {
            try {
                repository.reabrir(ticketId)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo reabrir el ticket" }
        }
    }

    fun agregarComentario(ticketId: Int, texto: String) {
        val admin = usuarioAdmin.value
        if (admin == null) return
        viewModelScope.launch {
            try {
                repository.agregarComentario(ticketId, admin, texto)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo agregar el comentario" }
        }
    }

    private suspend fun obtenerClienteIdParaTicket(ticketId: Int): Int {
        return repository.obtenerTicketPorId(ticketId)
            ?.ticket
            ?.let { it.clienteId }
            ?: 1
    }

    class Factory(
        private val repository: AyudaRepository,
        private val usuarioAdmin: StateFlow<Usuario?>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SoporteViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return SoporteViewModel(repository, usuarioAdmin) as T
        }
    }
}
