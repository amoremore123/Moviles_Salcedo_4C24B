package com.tecsup.smartcatalog.data.auth

/** Contratos pequeños para probar la política de autenticación sin Android, Internet ni cuentas reales. */
data class IdentidadFirebase(val uid: String, val correo: String)
data class PerfilFirebase(val correo: String?, val rol: String?)

interface ProveedorAutenticacion {
    val uidActual: String?
    suspend fun autenticar(correo: String, clave: String): IdentidadFirebase
    suspend fun usuarioActual(): IdentidadFirebase?
    fun cerrarSesion()
}

interface ProveedorPerfiles {
    suspend fun obtener(uid: String): PerfilFirebase?
}
