package com.tecsup.ayudacampus.viewmodel

import com.tecsup.ayudacampus.data.local.entity.Usuario
import com.tecsup.ayudacampus.data.repository.AyudaRepository
import kotlinx.coroutines.flow.StateFlow

internal class ReglaSoporte(mensaje: String) : Exception(mensaje)
internal fun exigirMismaSesion(sesion: StateFlow<Usuario?>, usuario: Usuario) {
    if (sesion.value?.id != usuario.id || sesion.value?.rol != "ADMIN")
        throw ReglaSoporte("Necesitas una sesión válida de Soporte TI")
}
internal suspend fun AyudaRepository.validarSoporte(sesion: StateFlow<Usuario?>): Usuario {
    val usuario = sesion.value?.takeIf { it.id > 0 && it.rol == "ADMIN" }
        ?: throw ReglaSoporte("Acceso exclusivo para Soporte TI")
    val registrado = obtenerUsuario(usuario.id)?.takeIf { it.rol == "ADMIN" }
        ?: throw ReglaSoporte("La sesión de soporte ya no es válida")
    exigirMismaSesion(sesion, usuario)
    return registrado
}
