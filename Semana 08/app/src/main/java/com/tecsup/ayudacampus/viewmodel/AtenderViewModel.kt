package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AtenderViewModel(
    private val ticketId: Int,
    private val repository: AyudaRepository,
    private val sesion: StateFlow<Usuario?>
) : ViewModel() {
    data class Pantalla(
        val detalle: TicketConDetalle? = null, val autorId: Int? = null,
        val cargando: Boolean = true, val errorCarga: String? = null,
        val estadoSeleccionado: EstadoTicket? = null, val prioridadSeleccionada: Prioridad? = null,
        val respuesta: String = "", val operando: Boolean = false,
        val error: String? = null, val confirmacion: String? = null
    )
    private val _pantalla = MutableStateFlow(Pantalla())
    val pantalla = _pantalla.asStateFlow()
    val ticket = pantalla.map { it.detalle }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val recarga = MutableStateFlow(0)
    private var ocupado = false
    private var estadoEditado = false
    private var prioridadEditada = false
    private var ultimoUsuario: Int? = null

    init {
        viewModelScope.launch {
            combine(sesion, recarga) { usuario, _ -> usuario }.collectLatest { usuario ->
                val borrador = if (ultimoUsuario == usuario?.id && usuario?.rol == "ADMIN") _pantalla.value.respuesta else ""
                ultimoUsuario = usuario?.id
                estadoEditado = false; prioridadEditada = false
                _pantalla.value = Pantalla(respuesta = borrador, operando = ocupado)
                try {
                    if (ticketId <= 0) throw ReglaSoporte("Ticket no disponible")
                    val autor = repository.validarSoporte(sesion)
                    repository.observarTicket(ticketId).collect { detalle ->
                        exigirMismaSesion(sesion, autor)
                        if (detalle == null) throw ReglaSoporte("Ticket no disponible")
                        val cerrado = detalle.ticket.estado == "CERRADO"
                        if (cerrado) { estadoEditado = false; prioridadEditada = false }
                        _pantalla.update {
                            it.copy(detalle = detalle, autorId = autor.id, cargando = false, errorCarga = null,
                                estadoSeleccionado = if (estadoEditado) it.estadoSeleccionado else runCatching { EstadoTicket.valueOf(detalle.ticket.estado) }.getOrNull(),
                                prioridadSeleccionada = if (prioridadEditada) it.prioridadSeleccionada else runCatching { Prioridad.valueOf(detalle.ticket.prioridad) }.getOrNull())
                        }
                    }
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    _pantalla.update { it.copy(detalle = null, autorId = null, cargando = false,
                        errorCarga = if (e is ReglaSoporte) e.message else "No se pudo cargar el ticket. Inténtalo nuevamente.") }
                }
            }
        }
    }
    fun reintentar() { recarga.update { it + 1 } }
    fun cambiarEstado(estado: EstadoTicket) {
        if (ocupado) return
        if (estado == EstadoTicket.CERRADO || _pantalla.value.detalle?.ticket?.estado == "CERRADO") {
            _pantalla.update { it.copy(error = "El cierre con calificación corresponde al estudiante") }
            return
        }
        estadoEditado = true
        _pantalla.update { it.copy(estadoSeleccionado = estado, error = null, confirmacion = null) }
    }
    fun cambiarPrioridad(prioridad: Prioridad) {
        if (ocupado || _pantalla.value.detalle?.ticket?.estado == "CERRADO") return
        prioridadEditada = true
        _pantalla.update { it.copy(prioridadSeleccionada = prioridad, error = null, confirmacion = null) }
    }
    fun actualizarRespuesta(texto: String) {
        if (!ocupado) _pantalla.update { it.copy(respuesta = texto, error = null, confirmacion = null) }
    }
    private fun ejecutar(accion: suspend (Usuario, TicketConDetalle) -> String) {
        if (ocupado) return
        ocupado = true
        _pantalla.update { it.copy(operando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try {
                val autor = repository.validarSoporte(sesion)
                val detalle = repository.observarTicket(ticketId).first() ?: throw ReglaSoporte("Ticket no disponible")
                exigirMismaSesion(sesion, autor)
                if (detalle.ticket.estado == "CERRADO") throw ReglaSoporte("El ticket está cerrado y es de solo lectura")
                val mensaje = accion(autor, detalle)
                exigirMismaSesion(sesion, autor)
                _pantalla.update { it.copy(confirmacion = mensaje) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _pantalla.update { it.copy(error = if (e is ReglaSoporte) e.message else "No se pudo completar la operación. Revisa los datos guardados y vuelve a intentarlo.") }
            } finally {
                ocupado = false
                _pantalla.update { it.copy(operando = false) }
            }
        }
    }
    fun responder(texto: String = _pantalla.value.respuesta) {
        if (ocupado) return
        if (texto.isBlank()) {
            _pantalla.update { it.copy(error = "Escribe una respuesta antes de enviarla", confirmacion = null) }
            return
        }
        ejecutar { autor, _ ->
            exigirMismaSesion(sesion, autor)
            repository.agregarComentario(ticketId, autor, texto.trim())
            _pantalla.update { it.copy(respuesta = "") }
            "Respuesta enviada"
        }
    }
    fun guardar() {
        val seleccion = _pantalla.value
        ejecutar { autor, actual ->
            val estado = seleccion.estadoSeleccionado ?: throw ReglaSoporte("Selecciona un estado válido")
            val prioridad = seleccion.prioridadSeleccionada ?: throw ReglaSoporte("Selecciona una prioridad válida")
            if (estado == EstadoTicket.CERRADO) throw ReglaSoporte("El cierre con calificación corresponde al estudiante")
            if (actual.ticket.prioridad != prioridad.name) {
                exigirMismaSesion(sesion, autor)
                repository.cambiarPrioridad(ticketId, prioridad)
            }
            if (actual.ticket.estado != estado.name) {
                val vigente = repository.observarTicket(ticketId).first() ?: throw ReglaSoporte("Ticket no disponible")
                if (vigente.ticket.estado == "CERRADO") throw ReglaSoporte("El estudiante ya cerró el ticket")
                exigirMismaSesion(sesion, autor)
                if (vigente.ticket.estado == "RESUELTO" && estado == EstadoTicket.ABIERTO) repository.reabrir(ticketId)
                else repository.cambiarEstado(ticketId, estado)
            }
            estadoEditado = false; prioridadEditada = false
            val actualizado = repository.observarTicket(ticketId).first()
            _pantalla.update { it.copy(
                estadoSeleccionado = actualizado?.ticket?.estado?.let { valor -> runCatching { EstadoTicket.valueOf(valor) }.getOrNull() },
                prioridadSeleccionada = actualizado?.ticket?.prioridad?.let { valor -> runCatching { Prioridad.valueOf(valor) }.getOrNull() }) }
            "Cambios guardados"
        }
    }
    class Factory(private val id: Int, private val repository: AyudaRepository, private val sesion: StateFlow<Usuario?>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AtenderViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return AtenderViewModel(id, repository, sesion) as T
        }
    }
}
