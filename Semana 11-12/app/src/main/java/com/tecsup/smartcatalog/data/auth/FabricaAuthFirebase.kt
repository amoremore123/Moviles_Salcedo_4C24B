package com.tecsup.smartcatalog.data.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.smartcatalog.BuildConfig
import com.tecsup.smartcatalog.data.repository.AuthRepositoryFirebase
import com.tecsup.smartcatalog.domain.model.Sesion

object FabricaAuthFirebase {
    fun crear(context: Context): AuthRepository {
        if (!BuildConfig.FIREBASE_CONFIGURADO) return AuthRepositorySinConfigurar()
        return try {
            val app = FirebaseApp.getApps(context.applicationContext).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                ?: FirebaseApp.initializeApp(context.applicationContext)
                ?: return AuthRepositorySinConfigurar()
            AuthRepositoryFirebase(FirebaseAuth.getInstance(app), FirebaseFirestore.getInstance(app))
        } catch (_: Exception) {
            AuthRepositorySinConfigurar()
        }
    }
}

class AuthRepositorySinConfigurar : AuthRepository {
    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> =
        Result.failure(ErrorAutenticacion(TipoErrorAutenticacion.CONFIGURACION))
    override suspend fun sesionActual(): Sesion? = null
    override fun cerrarSesion() = Unit
}
