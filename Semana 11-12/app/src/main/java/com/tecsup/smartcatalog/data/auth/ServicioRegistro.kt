package com.tecsup.smartcatalog.data.auth

import android.content.Context
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

interface ServicioRegistro {
    fun recuperarNombrePendiente(): String?
    suspend fun crearCuenta(nombre: String, correo: String, clave: String)
    suspend fun enviarVerificacion()
    suspend fun correoVerificado(): Boolean
    suspend fun finalizarPerfil(nombre: String)
}

/** Contrato para la futura llamada autenticada. El cliente solo proporciona el nombre. */
fun interface FinalizadorRegistro {
    suspend fun finalizarRegistroSmartCatalog(nombre: String)
}

class ErrorRegistro(message: String) : Exception(message)

class AlmacenRegistroPendiente(context: Context) {
    private val preferencias = context.getSharedPreferences("registro_pendiente", Context.MODE_PRIVATE)

    fun guardar(uid: String, nombre: String) {
        check(preferencias.edit().putString("uid", uid).putString("nombre", nombre).commit())
    }

    fun nombrePara(uid: String?): String? =
        if (uid != null && preferencias.getString("uid", null) == uid) preferencias.getString("nombre", null)
        else null

    fun limpiar() {
        check(preferencias.edit().clear().commit())
    }
}

/** Adaptador preparado para un entorno aprobado. La app de producción no lo instancia. */
class ServicioRegistroFirebase(
    private val auth: FirebaseAuth,
    private val finalizador: FinalizadorRegistro,
    private val almacen: AlmacenRegistroPendiente
) : ServicioRegistro {
    override fun recuperarNombrePendiente(): String? = almacen.nombrePara(auth.currentUser?.uid)

    override suspend fun crearCuenta(nombre: String, correo: String, clave: String) = seguro {
        check(auth.currentUser == null)
        val usuario = requireNotNull(auth.createUserWithEmailAndPassword(correo, clave).await().user)
        almacen.guardar(usuario.uid, nombre)
    }

    override suspend fun enviarVerificacion() {
        seguro { usuarioPendiente().sendEmailVerification().await() }
    }

    override suspend fun correoVerificado(): Boolean = seguro {
        usuarioPendiente().reload().await()
        usuarioPendiente().getIdToken(true).await().claims["email_verified"] == true
    }

    override suspend fun finalizarPerfil(nombre: String) = seguro {
        check(nombre == recuperarNombrePendiente())
        check(correoVerificado())
        finalizador.finalizarRegistroSmartCatalog(nombre)
        almacen.limpiar()
        auth.signOut()
    }

    private fun usuarioPendiente() = requireNotNull(auth.currentUser).also {
        check(almacen.nombrePara(it.uid) != null)
    }

    private suspend fun <T> seguro(operacion: suspend () -> T): T = try {
        operacion()
    } catch (cancelada: CancellationException) {
        throw cancelada
    } catch (_: FirebaseAuthUserCollisionException) {
        throw ErrorRegistro("Ese correo ya tiene cuenta. Vuelve al inicio de sesión.")
    } catch (_: FirebaseAuthWeakPasswordException) {
        throw ErrorRegistro("La contraseña no cumple la política de Firebase.")
    } catch (_: FirebaseNetworkException) {
        throw ErrorRegistro("Revisa tu conexión e inténtalo nuevamente.")
    } catch (_: Exception) {
        throw ErrorRegistro("No se pudo completar la operación. Inténtalo nuevamente.")
    }
}
