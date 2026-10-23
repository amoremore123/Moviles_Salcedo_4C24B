package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.relation.TicketConDetalle
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.EstadoTicket
import kotlinx.coroutines.flow.*

class MisTicketsViewModel(
    repository: AyudaRepository,
    clienteId: Int
) : ViewModel() {
    enum class Filtro(val etiqueta: String, val estado: EstadoTicket?) {
        TODOS("Todos", null), ABIERTOS("Abiertos", EstadoTicket.ABIERTO),
        EN_PROCESO("En proceso", EstadoTicket.EN_PROCESO), RESUELTOS("Resueltos", EstadoTicket.RESUELTO)
    }

    init { require(clienteId > 0) { "Se necesita el ID del estudiante autenticado" } }

    private val _filtro = MutableStateFlow(Filtro.TODOS)
    val filtro: StateFlow<Filtro> = _filtro.asStateFlow()
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val originales = repository.observarTicketsCliente(clienteId)
        .onEach { _cargando.value = false; _error.value = null }
        .catch {
            _error.value = "No se pudieron cargar tus tickets"
            _cargando.value = false
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tickets: StateFlow<List<TicketConDetalle>> = combine(originales, filtro, busqueda) { lista, filtro, texto ->
        val consulta = texto.trim()
        val numero = consulta.removePrefix("#").toIntOrNull()
        lista.filter { detalle ->
            val ticket = detalle.ticket
            (filtro.estado == null || ticket.estado == filtro.estado.name) &&
                (consulta.isEmpty() || ticket.titulo.contains(consulta, ignoreCase = true) ||
                    ticket.id.toString().contains(consulta) ||
                    ticket.id.toString().padStart(4, '0').contains(consulta) || ticket.id == numero)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cantidadAbiertos: StateFlow<Int> = originales.map { lista ->
        lista.count { it.ticket.estado == EstadoTicket.ABIERTO.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun seleccionarFiltro(filtro: Filtro) { _filtro.value = filtro }
    fun buscar(texto: String) { _busqueda.value = texto }

    class Factory(private val repository: AyudaRepository, private val clienteId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MisTicketsViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return MisTicketsViewModel(repository, clienteId) as T
        }
    }
}
