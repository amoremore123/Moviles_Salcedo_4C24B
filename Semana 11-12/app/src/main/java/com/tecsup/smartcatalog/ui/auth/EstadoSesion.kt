package com.tecsup.smartcatalog.ui.auth

import com.tecsup.smartcatalog.domain.model.Sesion

sealed interface EstadoSesion {
    data object Verificando : EstadoSesion
    data object SinSesion : EstadoSesion
    data object Iniciando : EstadoSesion
    data class Error(val mensaje: String, val errorCorreo: String? = null, val errorClave: String? = null) : EstadoSesion
    sealed interface Autenticado : EstadoSesion { val sesion: Sesion }
    data class Usuario(override val sesion: Sesion) : Autenticado
    data class Administrador(override val sesion: Sesion) : Autenticado
}
