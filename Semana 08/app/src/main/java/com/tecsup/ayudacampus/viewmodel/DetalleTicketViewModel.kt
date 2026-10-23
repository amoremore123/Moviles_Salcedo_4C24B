package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetalleTicketViewModel(
    private val ticketId: Int,
    private val repository: AyudaRepository,
    private val sesion: StateFlow<Usuario?>
) : ViewModel() {
    data class Estado(
        val detalle: TicketConDetalle? = null,
        val cargando: Boolean = true,
        val errorCarga: String? = null,
        val texto: String = "",
        val operando: Boolean = false,
        val error: String? = null,
        val confirmacion: String? = null
    )
    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()
    private val recarga = MutableStateFlow(0)
    private var ocupado = false
    private var ultimoUsuario: Int? = null

    init {
        viewModelScope.launch {
            combine(sesion, recarga) { usuario, _ -> usuario }.collectLatest { usuario ->
                val borrador = if (ultimoUsuario == usuario?.id) _estado.value.texto else ""
                ultimoUsuario = usuario?.id
                _estado.value = Estado(texto = borrador, operando = ocupado)
                if (ticketId <= 0 || usuario == null || usuario.id <= 0 || usuario.rol != "CLIENTE") {
                    _estado.update { it.copy(cargando = false, errorCarga = "Ticket no disponible para esta sesión") }
                    return@collectLatest
                }
                try {
                    val registrado = repository.obtenerUsuario(usuario.id)
                    if (registrado?.rol != "CLIENTE") throw Regla("Ticket no disponible para esta sesión")
                    repository.observarTicket(ticketId).collect { detalle ->
                        val permitido = mismaSesion(usuario) && detalle?.ticket?.clienteId == usuario.id
                        _estado.update {
                            it.copy(
                                detalle = if (permitido) detalle else null,
                                cargando = false,
                                errorCarga = if (permitido) null else "Ticket no disponible para esta sesión"
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _estado.update { it.copy(detalle = null, cargando = false, errorCarga = if (e is Regla) e.message else "No se pudo cargar el ticket. Inténtalo nuevamente.") }
                }
            }
        }
    }

    private fun mismaSesion(usuario: Usuario) = sesion.value?.let {
        it.id == usuario.id && it.rol == "CLIENTE"
    } == true

    fun reintentar() { recarga.update { it + 1 } }
    fun actualizarComentario(texto: String) {
        if (!ocupado) _estado.update { it.copy(texto = texto, error = null, confirmacion = null) }
    }

    private class Regla(mensaje: String) : Exception(mensaje)

    private suspend fun autorizar(): Pair<Usuario, TicketConDetalle> {
        val usuario = sesion.value?.takeIf { it.id > 0 && it.rol == "CLIENTE" }
            ?: throw Regla("Necesitas una sesión de estudiante válida")
        val autor = repository.obtenerUsuario(usuario.id)?.takeIf { it.rol == "CLIENTE" }
            ?: throw Regla("La sesión ya no es válida")
        val detalle = repository.observarTicket(ticketId).first()
            ?.takeIf { it.ticket.clienteId == usuario.id }
            ?: throw Regla("Ticket no disponible para esta sesión")
        if (!mismaSesion(usuario)) throw Regla("La sesión ya no es válida")
        return autor to detalle
    }

    private fun ejecutar(accion: suspend (Usuario, TicketConDetalle) -> String) {
        if (ocupado) return
        ocupado = true
        _estado.update { it.copy(operando = true, error = null, confirmacion = null) }
        viewModelScope.launch {
            try {
                val (autor, detalle) = autorizar()
                val mensaje = accion(autor, detalle)
                _estado.update { it.copy(confirmacion = mensaje) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(error = if (e is Regla) e.message else "No se pudo guardar el cambio. Inténtalo nuevamente.") }
            } finally {
                ocupado = false
                _estado.update { it.copy(operando = false) }
            }
        }
    }

    fun comentar(texto: String = _estado.value.texto) {
        if (ocupado) return
        if (texto.isBlank()) {
            _estado.update { it.copy(error = "Escribe un comentario antes de enviarlo", confirmacion = null) }
            return
        }
        ejecutar { autor, detalle ->
            if (detalle.ticket.estado == "CERRADO") throw Regla("Los tickets cerrados no admiten comentarios")
            if (!mismaSesion(autor)) throw Regla("La sesión ya no es válida")
            repository.agregarComentario(ticketId, autor, texto.trim())
            _estado.update { it.copy(texto = "") }
            "Comentario enviado"
        }
    }

    fun cerrar(calificacion: Int) = ejecutar { autor, detalle ->
        if (calificacion !in 1..5) throw Regla("Selecciona una calificación de 1 a 5 estrellas")
        if (detalle.ticket.estado != "RESUELTO") throw Regla("Solo puedes cerrar un ticket resuelto")
        if (!mismaSesion(autor)) throw Regla("La sesión ya no es válida")
        repository.cerrarConCalificacion(ticketId, calificacion)
        "Ticket cerrado correctamente"
    }

    fun reabrir() = ejecutar { autor, detalle ->
        if (detalle.ticket.estado != "RESUELTO") throw Regla("Solo puedes reabrir un ticket resuelto")
        if (!mismaSesion(autor)) throw Regla("La sesión ya no es válida")
        repository.reabrir(ticketId)
        "Ticket reabierto"
    }

    class Factory(
        private val id: Int,
        private val repository: AyudaRepository,
        private val sesion: StateFlow<Usuario?>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DetalleTicketViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return DetalleTicketViewModel(id, repository, sesion) as T
        }
    }
}
