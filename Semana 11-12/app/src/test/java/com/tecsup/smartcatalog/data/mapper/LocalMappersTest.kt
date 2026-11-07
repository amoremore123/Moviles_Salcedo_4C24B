package com.tecsup.smartcatalog.data.mapper

import com.tecsup.smartcatalog.data.local.ItemEntity
import com.tecsup.smartcatalog.domain.model.Item
import org.junit.Assert.*
import org.junit.Test

class LocalMappersTest {
    @Test fun itemAEntidadConservaTodosLosCamposYElFavorito() {
        val item = Item(51, "Teléfono", "Descripción completa", 325.75, "https://example.com/imagen.webp", "smartphones", true)
        assertEquals(ItemEntity(51, "Teléfono", "Descripción completa", 325.75, "https://example.com/imagen.webp", "smartphones", true), item.toEntity())
    }

    @Test fun entidadADominioConservaFavoritoVerdaderoYFalso() {
        for (favorito in listOf(false, true)) {
            val entity = ItemEntity(73, "Modelo", "Descripción", 215.50, "imagen", "smartphones", favorito)
            assertEquals(Item(73, "Modelo", "Descripción", 215.50, "imagen", "smartphones", favorito), entity.toItem())
        }
    }

    @Test fun lasConversionesDeIdaYVueltaNoPierdenDatos() {
        val item = Item(91, "Modelo Ñ", "Descripción sin recortes", 999.99, "imagen", "smartphones", true)
        assertEquals(item, item.toEntity().toItem())
        val entity = ItemEntity(92, "Otro modelo", "", 0.0, "", "smartphones", false)
        assertEquals(entity, entity.toItem().toEntity())
    }
}
