package com.tecsup.smartcatalog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.smartcatalog.data.auth.AuthRepository
import com.tecsup.smartcatalog.data.auth.ErrorAutenticacion
import com.tecsup.smartcatalog.data.auth.IdentidadFirebase
import com.tecsup.smartcatalog.data.auth.PerfilFirebase
import com.tecsup.smartcatalog.data.auth.ProveedorAutenticacion
import com.tecsup.smartcatalog.data.auth.ProveedorAuthSdk
import com.tecsup.smartcatalog.data.auth.ProveedorPerfiles
import com.tecsup.smartcatalog.data.auth.ProveedorPerfilesSdk
import com.tecsup.smartcatalog.data.auth.TipoErrorAutenticacion
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Autentica primero y valida después el perfil remoto. No almacena contraseñas ni concede roles por defecto. */
class AuthRepositoryFirebase(
    private val autenticacion: ProveedorAutenticacion,
    private val perfiles: ProveedorPerfiles
) : AuthRepository {
    constructor(auth: FirebaseAuth, firestore: FirebaseFirestore) : this(ProveedorAuthSdk(auth), ProveedorPerfilesSdk(firestore))

    private val operaciones = Mutex()
    private val generacion = AtomicLong()

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> = operaciones.withLock {
        val inicio = generacion.get()
        try {
            if (correo.isBlank() || clave.isBlank()) throw ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
            val usuario = autenticacion.autenticar(correo.trim(), clave)
            val sesion = validar(usuario, perfiles.obtener(usuario.uid))
            comprobarVigencia(inicio, usuario.uid)
            Result.success(sesion)
        } catch (cancelacion: CancellationException) {
            salirSeguro()
            throw cancelacion
        } catch (error: Exception) {
            salirSeguro()
            Result.failure(error as? ErrorAutenticacion ?: ErrorAutenticacion(TipoErrorAutenticacion.GENERAL))
        }
    }

    override suspend fun sesionActual(): Sesion? = operaciones.withLock {
        val inicio = generacion.get()
        try {
            val usuario = autenticacion.usuarioActual() ?: return@withLock null
            val sesion = validar(usuario, perfiles.obtener(usuario.uid))
            comprobarVigencia(inicio, usuario.uid)
            sesion
        } catch (cancelacion: CancellationException) {
            salirSeguro()
            throw cancelacion
        } catch (_: Exception) {
            salirSeguro()
            null // Sin verificación del servidor, no se publica ningún rol, incluido admin.
        }
    }

    override fun cerrarSesion() {
        generacion.incrementAndGet() // Invalida también un login cuya respuesta todavía esté pendiente.
        salirSeguro()
    }

    private fun comprobarVigencia(inicio: Long, uid: String) {
        if (generacion.get() != inicio || autenticacion.uidActual != uid)
            throw ErrorAutenticacion(TipoErrorAutenticacion.SESION_CAMBIADA)
    }

    private fun validar(usuario: IdentidadFirebase, perfil: PerfilFirebase?): Sesion {
        if (usuario.uid.isBlank() || usuario.correo.isBlank()) throw ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_INVALIDO)
        if (perfil == null) throw ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_AUSENTE)
        if (!usuario.correo.equals(perfil.correo?.trim(), ignoreCase = true))
            throw ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_INVALIDO)
        val rol = when (perfil.rol) {
            "admin" -> Rol.ADMIN
            "usuario" -> Rol.USUARIO
            else -> throw ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_INVALIDO)
        }
        return Sesion(usuario.uid, usuario.correo, rol, perfil.nombre?.trim()?.takeIf(String::isNotEmpty))
    }

    private fun salirSeguro() { runCatching { autenticacion.cerrarSesion() } }
}
