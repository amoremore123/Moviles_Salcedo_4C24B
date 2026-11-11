package com.tecsup.smartcatalog.ui.theme

import org.junit.Assert.*
import org.junit.Test

class TemaSmartCatalogTest {
    @Test fun soloSeguirSistemaUsaLaConfiguracionDeAndroid() {
        assertFalse(TemaSmartCatalog.SISTEMA.esOscuro(false))
        assertTrue(TemaSmartCatalog.SISTEMA.esOscuro(true))
        assertFalse(TemaSmartCatalog.CLARO.esOscuro(true))
        assertTrue(TemaSmartCatalog.OSCURO.esOscuro(false))
    }

    @Test fun leeLasOpcionesGuardadasYUsaSistemaPorDefecto() {
        TemaSmartCatalog.entries.forEach { assertEquals(it, TemaSmartCatalog.desdeValor(it.valor)) }
        assertEquals(TemaSmartCatalog.SISTEMA, TemaSmartCatalog.desdeValor(null))
        assertEquals(TemaSmartCatalog.SISTEMA, TemaSmartCatalog.desdeValor("valor-no-reconocido"))
    }
}
