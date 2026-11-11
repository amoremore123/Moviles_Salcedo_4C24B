package com.tecsup.smartcatalog.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

internal class ProveedorAuthSdk(private val auth: FirebaseAuth) : ProveedorAutenticacion {
    override val uidActual: String? get() = auth.currentUser?.uid

    override suspend fun autenticar(correo: String, clave: String): IdentidadFirebase = seguro {
        val user = auth.signInWithEmailAndPassword(correo, clave).await().user
            ?: throw ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
        identidad(user)
    }

    override suspend fun usuarioActual(): IdentidadFirebase? = seguro {
        val user = auth.currentUser ?: return@seguro null
        user.getIdToken(true).await() // Verificar contra Auth; no aceptar solo la identidad persistida en el dispositivo.
        identidad(user)
    }

    override fun cerrarSesion() { auth.signOut() }

    private fun identidad(user: FirebaseUser): IdentidadFirebase = IdentidadFirebase(
        user.uid,
        user.email ?: throw ErrorAutenticacion(TipoErrorAutenticacion.PERFIL_INVALIDO)
    )
}

internal class ProveedorPerfilesSdk(private val firestore: FirebaseFirestore) : ProveedorPerfiles {
    override suspend fun obtener(uid: String): PerfilFirebase? = seguro {
        val documento = firestore.collection("usuarios").document(uid).get(Source.SERVER).await()
        if (!documento.exists()) null
        else PerfilFirebase(
            correo = documento.get("correo") as? String,
            rol = documento.get("rol") as? String,
            nombre = documento.get("nombre") as? String
        )
    }
}

private suspend fun <T> seguro(operacion: suspend () -> T): T = try {
    operacion()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (error: ErrorAutenticacion) {
    throw error
} catch (_: FirebaseAuthInvalidCredentialsException) {
    throw ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
} catch (_: FirebaseAuthInvalidUserException) {
    throw ErrorAutenticacion(TipoErrorAutenticacion.CREDENCIALES)
} catch (_: FirebaseNetworkException) {
    throw ErrorAutenticacion(TipoErrorAutenticacion.RED)
} catch (error: FirebaseFirestoreException) {
    val tipo = if (error.code in setOf(FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED))
        TipoErrorAutenticacion.RED else TipoErrorAutenticacion.GENERAL
    throw ErrorAutenticacion(tipo)
} catch (_: Exception) {
    throw ErrorAutenticacion(TipoErrorAutenticacion.GENERAL)
}
