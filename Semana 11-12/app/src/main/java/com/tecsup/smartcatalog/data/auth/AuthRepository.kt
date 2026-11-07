package com.tecsup.smartcatalog.data.auth

import com.tecsup.smartcatalog.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    suspend fun sesionActual(): Sesion?
    fun cerrarSesion()
}
