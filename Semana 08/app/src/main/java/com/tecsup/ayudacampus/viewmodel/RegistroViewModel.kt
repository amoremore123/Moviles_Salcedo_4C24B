package com.tecsup.ayudacampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistroViewModel(
    private val repository: AyudaRepository
) : ViewModel() {
    data class Formulario(
        val nombre: String = "",
        val correo: String = "",
        val clave: String = "",
        val confirmarClave: String = "",
        val rol: String = "CLIENTE"
    )

    data class Errores(
        val nombre: String? = null,
        val correo: String? = null,
        val clave: String? = null,
        val confirmarClave: String? = null
    ) {
        fun hayErrores() = listOf(nombre, correo, clave, confirmarClave).any { it != null }
    }

    private val _formulario = MutableStateFlow(Formulario())
    val formulario: StateFlow<Formulario> = _formulario.asStateFlow()
    private val _errores = MutableStateFlow(Errores())
    val errores: StateFlow<Errores> = _errores.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _registrando = MutableStateFlow(false)
    val registrando: StateFlow<Boolean> = _registrando.asStateFlow()
    private val _registroExitoso = MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso.asStateFlow()

    private fun editar(cambio: (Formulario) -> Formulario) {
        if (_registrando.value || _registroExitoso.value) return
        _formulario.update(cambio)
        _error.value = null
    }

    fun actualizarNombre(texto: String) {
        editar { it.copy(nombre = texto) }
        if (!_registrando.value) _errores.update { it.copy(nombre = null) }
    }

    fun actualizarCorreo(texto: String) {
        editar { it.copy(correo = texto) }
        if (!_registrando.value) _errores.update { it.copy(correo = null) }
    }

    fun actualizarClave(texto: String) {
        editar { it.copy(clave = texto) }
        if (!_registrando.value) _errores.update { it.copy(clave = null) }
    }

    fun actualizarConfirmarClave(texto: String) {
        editar { it.copy(confirmarClave = texto) }
        if (!_registrando.value) _errores.update { it.copy(confirmarClave = null) }
    }

    fun seleccionarRol(rol: String) {
        if (!_registrando.value) editar { it.copy(rol = rol) }
    }

    fun registrar() {
        if (_registrando.value || _registroExitoso.value) return
        _error.value = null
        val datos = _formulario.value

        val correoValido = android.util.Patterns.EMAIL_ADDRESS.matcher(datos.correo.trim()).matches()

        _errores.value = Errores(
            nombre = if (datos.nombre.trim().length < 2) "El nombre debe tener al menos 2 caracteres" else null,
            correo = if (!correoValido) "Ingresa un correo valido" else if (datos.correo.trim().length < 5) "Ingresa un correo valido" else null,
            clave = if (datos.clave.length < 4) "La contraseña debe tener al menos 4 caracteres" else null,
            confirmarClave = if (datos.clave != datos.confirmarClave) "Las contraseñas no coinciden" else if (datos.confirmarClave.isBlank()) "Confirma tu contraseña" else null
        )

        if (_errores.value.hayErrores()) return

        _registrando.value = true
        viewModelScope.launch {
            try {
                val yaExiste = repository.obtenerUsuarioPorCorreo(datos.correo.trim())
                if (yaExiste != null) {
                    _errores.update { it.copy(correo = "Ya existe una cuenta con ese correo") }
                    _registrando.value = false
                    return@launch
                }
                repository.registrarUsuario(
                    nombre = datos.nombre.trim(),
                    correo = datos.correo.trim(),
                    clave = datos.clave,
                    rol = datos.rol
                )
                _registroExitoso.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _errores.update { it.copy(correo = e.message ?: "Ya existe un usuario con ese correo") }
                _registrando.value = false
            } catch (e: Exception) {
                _error.value = "No se pudo crear la cuenta. Intenta nuevamente."
                _registrando.value = false
            }
        }
    }

    fun reiniciar() {
        _registroExitoso.value = false
        _formulario.value = Formulario()
        _errores.value = Errores()
    }

    class Factory(private val repository: com.tecsup.ayudacampus.data.repository.AyudaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RegistroViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(repository) as T
        }
    }
}
