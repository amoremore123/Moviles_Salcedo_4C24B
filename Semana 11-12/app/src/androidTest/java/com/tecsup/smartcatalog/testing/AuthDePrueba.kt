package com.tecsup.smartcatalog.testing

import com.tecsup.smartcatalog.data.auth.AuthRepository
import com.tecsup.smartcatalog.data.auth.ErrorAutenticacion
import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicInteger

/** Solo se empaqueta en el APK de pruebas; jamás autentica contra Firebase ni guarda claves. */
class AuthDePrueba : AuthRepository {
    @Volatile var actual: Sesion? = Sesion("uid-simulado", "vista@example.invalid", Rol.USUARIO)
    @Volatile var rolLogin = Rol.USUARIO
    @Volatile var error: ErrorAutenticacion? = null
    @Volatile var pendiente: CompletableDeferred<Unit>? = null
    val consultas = AtomicInteger()
    val intentos = AtomicInteger()
    val cierres = AtomicInteger()
    override suspend fun sesionActual(): Sesion? {
        consultas.incrementAndGet()
        pendiente?.await()
        return actual
    }
    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> {
        intentos.incrementAndGet()
        error?.let { return Result.failure(it) }
        val sesion = Sesion("uid-simulado", correo, rolLogin)
        actual = sesion
        return Result.success(sesion)
    }
    override fun cerrarSesion() { actual = null; cierres.incrementAndGet() }
    fun reiniciar() {
        actual = null; rolLogin = Rol.USUARIO; error = null; pendiente = null
        consultas.set(0); intentos.set(0); cierres.set(0)
    }
}
