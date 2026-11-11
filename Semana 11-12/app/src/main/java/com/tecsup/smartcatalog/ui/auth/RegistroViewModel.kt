package com.tecsup.smartcatalog.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecsup.smartcatalog.data.auth.ErrorRegistro
import com.tecsup.smartcatalog.data.auth.ServicioRegistro
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal fun validarRegistro(nombre: String, correo: String, clave: String, confirmacion: String): String? = when {
    nombre.trim().length !in 1..80 -> "Ingresa un nombre de hasta 80 caracteres."
    correo.trim().length > 254 || !Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+").matches(correo.trim()) ->
        "Ingresa un correo válido."
    clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
    clave != clave.trim() -> "Quita los espacios al inicio o al final de la contraseña."
    clave != confirmacion -> "Las contraseñas no coinciden."
    else -> null
}

data class EstadoRegistro(
    val cargando: Boolean = false,
    val pendiente: Boolean = false,
    val completo: Boolean = false,
    val mensaje: String? = null
)

class RegistroViewModel(private val servicio: ServicioRegistro? = null) : ViewModel() {
    val disponible: Boolean get() = servicio != null
    private var nombrePendiente = servicio?.recuperarNombrePendiente()
    private val mutable = MutableStateFlow(
        EstadoRegistro(
            pendiente = nombrePendiente != null,
            mensaje = nombrePendiente?.let { "Tu cuenta está creada. Verifica tu correo para confirmar el perfil." }
        )
    )
    val estado = mutable.asStateFlow()

    fun iniciar(nombre: String, correo: String, clave: String, confirmacion: String) {
        if (mutable.value.cargando || mutable.value.pendiente || mutable.value.completo) return
        val error = validarRegistro(nombre, correo, clave, confirmacion)
        if (error != null || servicio == null) {
            mutable.value = EstadoRegistro(mensaje = error ?: "El registro se habilitará cuando el servidor esté disponible.")
            return
        }
        ejecutar {
            servicio.crearCuenta(nombre.trim(), correo.trim(), clave)
            nombrePendiente = checkNotNull(servicio.recuperarNombrePendiente())
            servicio.enviarVerificacion()
            EstadoRegistro(pendiente = true, mensaje = "Abre el enlace que enviamos a tu correo.")
        }
    }

    fun reenviar() {
        val proveedor = servicio ?: return
        if (nombrePendiente == null) return
        ejecutar {
            proveedor.enviarVerificacion()
            EstadoRegistro(pendiente = true, mensaje = "Enviamos otro enlace de verificación.")
        }
    }

    fun comprobarCorreo() {
        val proveedor = servicio ?: return
        val nombre = nombrePendiente ?: return
        ejecutar {
            if (!proveedor.correoVerificado()) {
                EstadoRegistro(pendiente = true, mensaje = "Abre el enlace de verificación antes de continuar.")
            } else {
                proveedor.finalizarPerfil(nombre)
                nombrePendiente = null
                EstadoRegistro(completo = true, mensaje = "Perfil confirmado. Ya puedes iniciar sesión.")
            }
        }
    }

    private fun ejecutar(operacion: suspend () -> EstadoRegistro) {
        if (mutable.value.cargando) return
        mutable.value = mutable.value.copy(cargando = true, mensaje = null)
        viewModelScope.launch {
            try {
                mutable.value = operacion()
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (fallo: Exception) {
                val pendiente = nombrePendiente != null
                val mensaje = (fallo as? ErrorRegistro)?.message
                    ?: "No se pudo completar la operación. Inténtalo nuevamente."
                mutable.value = EstadoRegistro(
                    pendiente = pendiente,
                    mensaje = if (pendiente) "Tu cuenta está creada. Falta confirmar el perfil. $mensaje" else mensaje
                )
            } finally {
                mutable.value = mutable.value.copy(cargando = false)
            }
        }
    }

    class Factory(private val servicio: ServicioRegistro?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RegistroViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return RegistroViewModel(servicio) as T
        }
    }
}
