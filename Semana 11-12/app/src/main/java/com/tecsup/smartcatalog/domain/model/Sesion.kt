package com.tecsup.smartcatalog.domain.model

enum class Rol {
    ADMIN,
    USUARIO
}

data class Sesion(
    val uid: String,
    val correo: String,
    val rol: Rol,
    val nombre: String? = null
)
