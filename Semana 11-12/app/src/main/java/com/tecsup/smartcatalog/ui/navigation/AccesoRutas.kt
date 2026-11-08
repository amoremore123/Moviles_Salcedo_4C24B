package com.tecsup.smartcatalog.ui.navigation

import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion

enum class DestinoProtegido { CATALOGO, DETALLE, FAVORITOS, ADMIN }

/** La autorización se aplica al navegar y al componer el destino, no solo al mostrar botones. */
object AccesoRutas {
    fun permite(sesion: Sesion?, destino: DestinoProtegido): Boolean =
        sesion != null && (destino != DestinoProtegido.ADMIN || sesion.rol == Rol.ADMIN)

    fun inicial(sesion: Sesion): DestinoProtegido =
        if (sesion.rol == Rol.ADMIN) DestinoProtegido.ADMIN else DestinoProtegido.CATALOGO
}
