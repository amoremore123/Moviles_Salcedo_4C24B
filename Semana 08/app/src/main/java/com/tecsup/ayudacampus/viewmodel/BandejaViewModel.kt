package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.EstadoTicket
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// PanelViewModel reutiliza esta bandeja y su misma sesion validada.
open class BandejaViewModel(
    private val repository: AyudaRepository,
    private val sesion: StateFlow<Usuario?>
) : ViewModel() {
    enum class FiltroEstado(val etiqueta: String, val estado: EstadoTicket?) {
        TODOS("Todos", null), ABIERTOS("Abiertos", EstadoTicket.ABIERTO),
        EN_PROCESO("En proceso", EstadoTicket.EN_PROCESO), RESUELTOS("Resueltos", EstadoTicket.RESUELTO),
        CERRADOS("Cerrados", EstadoTicket.CERRADO)
    }
    enum class FiltroPrioridad(val etiqueta: String, val prioridad: Prioridad?) {
        TODAS("Todas", null), ALTA("Alta", Prioridad.ALTA), MEDIA("Media", Prioridad.MEDIA), BAJA("Baja", Prioridad.BAJA)
    }
    private val _administrador = MutableStateFlow<Usuario?>(null)
    val administrador: StateFlow<Usuario?> = _administrador.asStateFlow()
    private val _todos = MutableStateFlow<List<TicketConDetalle>>(emptyList())
    val todos: StateFlow<List<TicketConDetalle>> = _todos.asStateFlow()
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val recarga = MutableStateFlow(0)
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()
    private val _estado = MutableStateFlow(FiltroEstado.TODOS)
    val filtroEstado: StateFlow<FiltroEstado> = _estado.asStateFlow()
    private val _prioridad = MutableStateFlow(FiltroPrioridad.TODAS)
    val filtroPrioridad: StateFlow<FiltroPrioridad> = _prioridad.asStateFlow()

    val tickets: StateFlow<List<TicketConDetalle>> = combine(todos, filtroEstado, filtroPrioridad, busqueda) { lista, estado, prioridad, texto ->
        val consulta = texto.trim()
        val numero = consulta.removePrefix("#").toIntOrNull()
        // filter conserva el orden ALTA / MEDIA / BAJA y fecha que entrega Room.
        lista.filter { detalle ->
            val ticket = detalle.ticket
            (estado.estado == null || ticket.estado == estado.estado.name) &&
                (prioridad.prioridad == null || ticket.prioridad == prioridad.prioridad.name) &&
                (consulta.isEmpty() || ticket.titulo.contains(consulta, ignoreCase = true) ||
                    detalle.cliente.nombre.contains(consulta, ignoreCase = true) || ticket.id == numero ||
                    ticket.id.toString().contains(consulta) || ticket.id.toString().padStart(4, '0').contains(consulta))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            combine(sesion, recarga) { usuario, _ -> usuario }.collectLatest { usuario ->
                _administrador.value = null
                _todos.value = emptyList()
                _cargando.value = true
                _error.value = null
                if (usuario == null || usuario.id <= 0 || usuario.rol != "ADMIN") {
                    _cargando.value = false
                    _error.value = "Necesitas una sesión válida de Soporte TI"
                    return@collectLatest
                }
                try {
                    val registrado = repository.obtenerUsuario(usuario.id)
                    if (registrado?.rol != "ADMIN" || !mismaSesion(usuario)) {
                        _error.value = "La sesión de soporte ya no es válida"
                        _cargando.value = false
                        return@collectLatest
                    }
                    _administrador.value = registrado
                    repository.observarTodosLosTickets().collect { lista ->
                        _todos.value = if (mismaSesion(usuario)) lista else emptyList()
                        _cargando.value = false
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _todos.value = emptyList()
                    _administrador.value = null
                    _error.value = "No se pudo cargar el panel. Inténtalo nuevamente."
                    _cargando.value = false
                }
            }
        }
    }
    protected fun mismaSesion(usuario: Usuario) = sesion.value?.let { it.id == usuario.id && it.rol == "ADMIN" } == true
    fun buscar(texto: String) { _busqueda.value = texto }
    fun seleccionarEstado(estado: FiltroEstado) { _estado.value = estado }
    fun seleccionarPrioridad(prioridad: FiltroPrioridad) { _prioridad.value = prioridad }
    fun reintentar() { recarga.update { it + 1 } }
    fun verAltasAbiertas() {
        _busqueda.value = ""
        _estado.value = FiltroEstado.ABIERTOS
        _prioridad.value = FiltroPrioridad.ALTA
    }
}
