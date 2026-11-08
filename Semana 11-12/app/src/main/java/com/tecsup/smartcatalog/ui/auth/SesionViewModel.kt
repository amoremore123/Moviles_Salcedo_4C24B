package com.tecsup.smartcatalog.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.auth.AuthRepository
import com.tecsup.smartcatalog.data.auth.ErrorAutenticacion
import com.tecsup.smartcatalog.data.auth.TipoErrorAutenticacion
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Solo publica una sesión después de que el repositorio haya validado el perfil remoto. */
class SesionViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableEstado = MutableStateFlow<EstadoSesion>(EstadoSesion.Verificando)
    val estado = mutableEstado.asStateFlow()
    private var operacion: Job? = null
    private var generacion = 0L

    init { recuperarSesion() }

    private fun recuperarSesion() {
        val inicio = generacion
        operacion = viewModelScope.launch {
            try {
                val sesion = repository.sesionActual()
                if (inicio == generacion) mutableEstado.value = sesion?.let(::autenticado) ?: EstadoSesion.SinSesion
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (inicio == generacion) {
                    runCatching { repository.cerrarSesion() }
                    mutableEstado.value = EstadoSesion.Error("No se pudo verificar tu sesión. Inicia sesión de nuevo con conexión a Internet.")
                }
            }
        }
    }

    fun iniciarSesion(correo: String, clave: String) {
        if (estado.value is EstadoSesion.Autenticado || estado.value == EstadoSesion.Iniciando || estado.value == EstadoSesion.Verificando) return
        val email = correo.trim()
        val errorCorreo = when {
            email.isBlank() -> "Ingresa tu correo electrónico."
            !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email) -> "Ingresa un correo electrónico válido."
            else -> null
        }
        val errorClave = if (clave.isBlank()) "Ingresa tu contraseña." else null
        if (errorCorreo != null || errorClave != null) {
            mutableEstado.value = EstadoSesion.Error("Revisa los campos para continuar.", errorCorreo, errorClave)
            return
        }
        mutableEstado.value = EstadoSesion.Iniciando
        val inicio = generacion
        operacion = viewModelScope.launch {
            try {
                // La clave se pasa al repositorio; no forma parte del estado ni se persiste.
                val resultado = repository.iniciarSesion(email, clave)
                if (inicio == generacion) {
                    mutableEstado.value = resultado.fold(::autenticado) { error ->
                        EstadoSesion.Error((error as? ErrorAutenticacion)?.message
                            ?: ErrorAutenticacion(TipoErrorAutenticacion.GENERAL).message.orEmpty())
                    }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                if (inicio == generacion) {
                    runCatching { repository.cerrarSesion() }
                    mutableEstado.value = EstadoSesion.Error(ErrorAutenticacion(TipoErrorAutenticacion.GENERAL).message.orEmpty())
                }
            }
        }
    }

    fun cerrarSesion() {
        generacion++
        operacion?.cancel()
        runCatching { repository.cerrarSesion() }
        mutableEstado.value = EstadoSesion.SinSesion
    }

    private fun autenticado(sesion: Sesion): EstadoSesion.Autenticado = when (sesion.rol) {
        Rol.ADMIN -> EstadoSesion.Administrador(sesion)
        Rol.USUARIO -> EstadoSesion.Usuario(sesion)
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SesionViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return SesionViewModel(repository) as T
        }
    }
}
