package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.prefs.PreferenciasStore
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SesionViewModel(
    private val repository: AyudaRepository,
    private val preferencias: PreferenciasStore
) : ViewModel() {
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()
    private val _recuperandoSesion = MutableStateFlow(true)
    val recuperandoSesion: StateFlow<Boolean> = _recuperandoSesion.asStateFlow()
    val modoOscuro: StateFlow<Boolean> = preferencias.modoOscuro.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val recibirTickets = preferencias.recibirTickets.stateIn<Boolean?>(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            try {
                val (rol, id) = combine(preferencias.perfilActivo, preferencias.usuarioId) { rol, id ->
                    rol to id
                }.first()
                if (rol in setOf("CLIENTE", "ADMIN") && id > 0) {
                    val usuario = repository.obtenerUsuario(id)
                    if (usuario != null && usuario.rol == rol) {
                        _usuarioActual.value = usuario
                    } else {
                        preferencias.cerrarSesion()
                    }
                } else if (rol.isNotEmpty() || id != 0) {
                    preferencias.cerrarSesion()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo recuperar la sesión. Intenta ingresar nuevamente."
            } finally {
                _recuperandoSesion.value = false
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun login(correo: String, clave: String, rolSeleccionado: String) {
        if (_cargando.value || _recuperandoSesion.value) return
        _error.value = null
        if (correo.isBlank() || clave.isBlank()) {
            _error.value = "Ingresa tu correo y contraseña"
            return
        }
        if (rolSeleccionado !in setOf("CLIENTE", "ADMIN")) {
            _error.value = "Selecciona un perfil válido"
            return
        }
        _cargando.value = true
        viewModelScope.launch {
            try {
                val usuario = repository.login(correo.trim(), clave)
                when {
                    usuario == null -> _error.value = "Correo o contraseña incorrectos"
                    usuario.rol != rolSeleccionado ->
                        _error.value = "El usuario no corresponde al perfil seleccionado"
                    else -> {
                        preferencias.guardarSesion(usuario.rol, usuario.id)
                        _usuarioActual.value = usuario
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo iniciar sesión. Inténtalo nuevamente."
            } finally {
                _cargando.value = false
            }
        }
    }

    // Cambio explícito del modo práctica del laboratorio; usa un usuario real de Room.
    fun cambiarPerfil() {
        if (_cargando.value || _recuperandoSesion.value) return
        val actual = _usuarioActual.value ?: return
        if (actual.id <= 0 || actual.rol !in setOf("CLIENTE", "ADMIN")) return
        _cargando.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val vigente = repository.obtenerUsuario(actual.id)
                check(vigente?.rol == actual.rol && _usuarioActual.value == actual)
                val otroRol = if (actual.rol == "CLIENTE") "ADMIN" else "CLIENTE"
                val destino = repository.obtenerUsuarioPorRol(otroRol)
                check(destino != null && destino.id > 0 && destino.rol == otroRol)
                check(_usuarioActual.value == actual)
                preferencias.guardarSesion(destino.rol, destino.id)
                _usuarioActual.value = destino
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "No se pudo cambiar de perfil. La sesión actual se conserva." }
            finally { _cargando.value = false }
        }
    }

    fun cerrarSesion() {
        if (_cargando.value || _recuperandoSesion.value) return
        _cargando.value = true
        viewModelScope.launch {
            try {
                preferencias.cerrarSesion()
                _usuarioActual.value = null
                _error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "No se pudo cerrar la sesión. Inténtalo nuevamente."
            } finally {
                _cargando.value = false
            }
        }
    }

    class Factory(
        private val repository: AyudaRepository,
        private val preferencias: PreferenciasStore
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SesionViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return SesionViewModel(repository, preferencias) as T
        }
    }
}
