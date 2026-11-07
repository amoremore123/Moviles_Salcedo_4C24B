package com.tecsup.smartcatalog.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductosDtoTest {
    private val gson = Gson()

    @Test
    fun convierteLaRespuestaDeListaConsultadaSinAccesoAInternet() {
        val json = requireNotNull(javaClass.getResource("/dummyjson/smartphones-response.json")).readText()
        val response = gson.fromJson(json, ProductosResponseDto::class.java)
        assertEquals(16, response.products.orEmpty().size)
        assertEquals(16, response.total)
        assertEquals(0, response.skip)
        val primero = requireNotNull(response.products.orEmpty().first())
        assertEquals(121, primero.id)
        assertEquals("iPhone 5s", primero.title)
        assertEquals(199.99, requireNotNull(primero.price), 0.0)
        assertEquals("smartphones", primero.category)
    }

    @Test
    fun aceptaCamposAusentesYNullSinCrearValoresNoNulosFalsos() {
        val ausente = gson.fromJson("""{"id":121}""", ProductoDto::class.java)
        val nulo = gson.fromJson("""{"id":121,"title":null,"price":null,"thumbnail":null}""", ProductoDto::class.java)
        assertNull(ausente.description)
        assertNull(ausente.price)
        assertNull(nulo.title)
        assertNull(nulo.price)
        assertNull(nulo.thumbnail)
    }

    @Test
    fun toleraContenedorAusenteNullYElementosNull() {
        for (json in listOf("{}", """{"products":null}""")) {
            assertTrue(gson.fromJson(json, ProductosResponseDto::class.java).products.orEmpty().isEmpty())
        }
        val response = gson.fromJson("""{"products":[null,{"id":121}]}""", ProductosResponseDto::class.java)
        assertEquals(1, response.products.orEmpty().filterNotNull().size)
    }
}
