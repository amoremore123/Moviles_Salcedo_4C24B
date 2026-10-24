package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import com.tecsup.ayudacampus.model.Prioridad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NuevoTicketViewModel(
    private val repository: AyudaRepository,
    private val usuarioActual: StateFlow<Usuario?>,
    private val recepcion: Flow<Boolean> = flowOf(true)
) : ViewModel() {
    data class Formulario(
        val categoriaId: Int? = null,
        val titulo: String = "",
        val descripcion: String = "",
        val ubicacion: String = "",
        val prioridad: Prioridad = Prioridad.MEDIA
    )

    data class Errores(
        val categoria: String? = null,
        val titulo: String? = null,
        val descripcion: String? = null,
        val ubicacion: String? = null
    ) {
        fun hayErrores() = listOf(categoria, titulo, descripcion, ubicacion).any { it != null }
    }

    private val _formulario = MutableStateFlow(Formulario())
    val formulario: StateFlow<Formulario> = _formulario.asStateFlow()
    private val _errores = MutableStateFlow(Errores())
    val errores: StateFlow<Errores> = _errores.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()
    private val _ticketCreado = MutableStateFlow<Long?>(null)
    val ticketCreado: StateFlow<Long?> = _ticketCreado.asStateFlow()
    private val _cargandoCategorias = MutableStateFlow(true)
    val cargandoCategorias: StateFlow<Boolean> = _cargandoCategorias.asStateFlow()

    val recibirTickets = recepcion.map<Boolean, Boolean?> { it }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val categorias = repository.observarCategoriasActivas()
        .onEach { _cargandoCategorias.value = false }
        .catch {
            _cargandoCategorias.value = false
            _error.value = "No se pudieron cargar las categorías. Vuelve a abrir el formulario."
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun editar(cambio: (Formulario) -> Formulario) {
        if (_guardando.value || _ticketCreado.value != null) return
        _formulario.update(cambio)
        _error.value = null
    }

    fun seleccionarCategoria(id: Int) {
        editar { it.copy(categoriaId = id) }
        if (!_guardando.value) _errores.update { it.copy(categoria = null) }
    }
    fun actualizarTitulo(texto: String) {
        editar { it.copy(titulo = texto) }
        if (!_guardando.value) _errores.update { it.copy(titulo = null) }
    }
    fun actualizarDescripcion(texto: String) {
        editar { it.copy(descripcion = texto) }
        if (!_guardando.value) _errores.update { it.copy(descripcion = null) }
    }
    fun actualizarUbicacion(texto: String) {
        editar { it.copy(ubicacion = texto) }
        if (!_guardando.value) _errores.update { it.copy(ubicacion = null) }
    }
    fun seleccionarPrioridad(prioridad: Prioridad) = editar { it.copy(prioridad = prioridad) }

    fun enviar() {
        if (_guardando.value || _ticketCreado.value != null) return
        _error.value = null
        val usuario = usuarioActual.value
        if (usuario == null || usuario.id <= 0 || usuario.rol != "CLIENTE") {
            _error.value = "Necesitas una sesión de estudiante válida para crear un ticket"
            return
        }
        val datos = _formulario.value
        _errores.value = Errores(
            categoria = if (datos.categoriaId == null) "Selecciona una categoría" else null,
            titulo = if (datos.titulo.trim().length < 5) "El título debe tener al menos 5 caracteres" else null,
            descripcion = if (datos.descripcion.trim().length < 10) "La descripción debe tener al menos 10 caracteres" else null,
            ubicacion = if (datos.ubicacion.isBlank()) "Ingresa la ubicación" else null
        )
        if (_errores.value.hayErrores()) return

        // Se activa antes de lanzar la corrutina para bloquear pulsaciones repetidas.
        _guardando.value = true
        viewModelScope.launch {
            try {
                val registrado = repository.obtenerUsuario(usuario.id)
                if (registrado?.rol != "CLIENTE" || usuarioActual.value?.id != usuario.id || usuarioActual.value?.rol != "CLIENTE") {
                    _error.value = "La sesión ya no es válida. Vuelve a iniciar sesión."
                    return@launch
                }
                val activas = repository.observarCategoriasActivas().first()
                if (activas.none { it.id == datos.categoriaId }) {
                    _errores.update { it.copy(categoria = "Selecciona una categoría activa") }
                    return@launch
                }
                if (usuarioActual.value?.id != usuario.id || usuarioActual.value?.rol != "CLIENTE") {
                    _error.value = "La sesión ya no es válida. Vuelve a iniciar sesión."
                    return@launch
                }
                if (!recepcion.first()) {
                    _error.value = "Mesa de ayuda cerrada. Inténtalo cuando se reanude la atención."
                    return@launch
                }
                if (usuarioActual.value?.id != usuario.id || usuarioActual.value?.rol != "CLIENTE") {
                    _error.value = "La sesión ya no es válida. Vuelve a iniciar sesión."
                    return@launch
                }
                _ticketCreado.value = repository.crearTicket(
                    clienteId = usuario.id, categoriaId = requireNotNull(datos.categoriaId),
                    titulo = datos.titulo.trim(), descripcion = datos.descripcion.trim(),
                    ubicacion = datos.ubicacion.trim(), prioridad = datos.prioridad
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo crear el ticket. Conservamos tus datos; inténtalo nuevamente."
            } finally {
                _guardando.value = false
            }
        }
    }

    class Factory(
        private val repository: AyudaRepository,
        private val usuarioActual: StateFlow<Usuario?>,
        private val recepcion: Flow<Boolean> = flowOf(true)
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NuevoTicketViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return NuevoTicketViewModel(repository, usuarioActual, recepcion) as T
        }
    }
}
