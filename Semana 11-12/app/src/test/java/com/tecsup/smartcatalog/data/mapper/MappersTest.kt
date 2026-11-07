package com.tecsup.smartcatalog.data.mapper

import com.google.gson.Gson
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {
    @Test
    fun convierteCadaCampoDelProductoYNoImportaFavoritosDeLaApi() {
        val dto = Gson().fromJson(
            """{"id":121,"title":"iPhone 5s","description":"Descripción ilustrativa","price":199.99,"thumbnail":"https://example.com/imagen.webp","category":"smartphones","favorito":true}""",
            ProductoDto::class.java
        )
        val item = dto.toItem()
        assertEquals(121, item.id)
        assertEquals("iPhone 5s", item.nombre)
        assertEquals("Descripción ilustrativa", item.descripcion)
        assertEquals(199.99, item.precio, 0.0)
        assertEquals("https://example.com/imagen.webp", item.imagen)
        assertEquals("smartphones", item.categoria)
        assertFalse(item.favorito)
    }

    @Test
    fun normalizaCamposOpcionalesAusentesYTitulosVacios() {
        val item = ProductoDto(id = 121, title = "  ").toItem()
        assertEquals("Producto sin nombre", item.nombre)
        assertEquals("", item.descripcion)
        assertEquals(0.0, item.precio, 0.0)
        assertEquals("", item.imagen)
        assertEquals("", item.categoria)
        assertFalse(item.favorito)
    }

    @Test
    fun conversionSeguraRechazaIdentificadoresInvalidosSinLanzarAlConsumidor() {
        for (id in listOf(null, 0, -1)) {
            assertTrue(ProductoDto(id = id).toItemSeguro().isFailure)
        }
        assertTrue(ProductoDto(id = 121).toItemSeguro().isSuccess)
    }

    @Test
    fun noPropagaPreciosNegativosONoFinitos() {
        for (precio in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals(0.0, ProductoDto(id = 121, price = precio).toItem().precio, 0.0)
        }
    }
}
