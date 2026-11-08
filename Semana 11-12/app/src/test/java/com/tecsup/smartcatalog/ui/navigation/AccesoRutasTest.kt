package com.tecsup.smartcatalog.ui.navigation

import com.tecsup.smartcatalog.domain.model.Rol
import com.tecsup.smartcatalog.domain.model.Sesion
import org.junit.Assert.*
import org.junit.Test

class AccesoRutasTest {
    private val usuario = Sesion("uid-simulado", "igual@example.invalid", Rol.USUARIO)
    @Test fun ningunaRutaPermiteEntrarSinSesion() {
        DestinoProtegido.entries.forEach { assertFalse(AccesoRutas.permite(null, it)) }
    }
    @Test fun usuarioPuedeExplorarPeroNoAdministrarInclusoConRutaDirecta() {
        assertTrue(AccesoRutas.permite(usuario, DestinoProtegido.CATALOGO))
        assertTrue(AccesoRutas.permite(usuario, DestinoProtegido.DETALLE))
        assertTrue(AccesoRutas.permite(usuario, DestinoProtegido.FAVORITOS))
        assertFalse(AccesoRutas.permite(usuario, DestinoProtegido.ADMIN))
    }
    @Test fun adminPuedeAccederATodasLasRutas() {
        DestinoProtegido.entries.forEach { assertTrue(AccesoRutas.permite(usuario.copy(rol = Rol.ADMIN), it)) }
    }
    @Test fun destinoInicialDependeDelRolConElMismoCorreo() {
        assertEquals(DestinoProtegido.CATALOGO, AccesoRutas.inicial(usuario))
        assertEquals(DestinoProtegido.ADMIN, AccesoRutas.inicial(usuario.copy(rol = Rol.ADMIN)))
    }
}
