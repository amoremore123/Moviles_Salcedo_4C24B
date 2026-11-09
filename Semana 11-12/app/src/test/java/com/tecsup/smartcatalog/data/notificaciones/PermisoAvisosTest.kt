package com.tecsup.smartcatalog.data.notificaciones

import org.junit.Assert.*
import org.junit.Test

class PermisoAvisosTest {
    @Test fun android13PermiteConPermisoConcedido() {
        assertTrue(permiteAviso(33, permiso = true, habilitadas = true))
    }

    @Test fun android13BloqueaConPermisoDenegado() {
        assertFalse(permiteAviso(33, permiso = false, habilitadas = true))
    }

    @Test fun android12YMinSdkNoNecesitanElPermisoDeAndroid13() {
        assertTrue(permiteAviso(32, permiso = false, habilitadas = true))
        assertTrue(permiteAviso(24, permiso = false, habilitadas = true))
    }

    @Test fun respetaLosAvisosDesactivadosEnElSistema() {
        assertFalse(permiteAviso(32, permiso = false, habilitadas = false))
        assertFalse(permiteAviso(36, permiso = true, habilitadas = false))
    }
}
