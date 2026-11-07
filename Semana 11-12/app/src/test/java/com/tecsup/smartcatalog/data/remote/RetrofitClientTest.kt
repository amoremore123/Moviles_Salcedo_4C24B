package com.tecsup.smartcatalog.data.remote

import com.google.gson.Gson
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RetrofitClientTest {
    @Test
    fun construyeUnServicioReutilizableSinEjecutarSolicitudes() {
        assertEquals("https://dummyjson.com/", RetrofitClient.BASE_URL)
        assertSame(RetrofitClient.apiService, RetrofitClient.apiService)
    }

    @Test
    fun configuraRutasNumericasYConvierteAmbasRespuestasSinInternet() = runBlocking {
        val jsonLista = requireNotNull(javaClass.getResource("/dummyjson/smartphones-response.json")).readText()
        val primero = Gson().fromJson(jsonLista, ProductosResponseDto::class.java).products.orEmpty().first()
        val rutas = mutableListOf<String>()
        val cliente = OkHttpClient.Builder().addInterceptor { chain ->
            // No se llama a proceed: la respuesta se entrega antes de DNS o conexiones de red.
            val request = chain.request()
            val ruta = request.url.encodedPath
            rutas.add(ruta)
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body((if (ruta == "/products/121") Gson().toJson(primero) else jsonLista)
                    .toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        try {
            val api = RetrofitClient.crearApiService("https://example.invalid/", cliente)
            assertEquals(16, api.obtenerSmartphones().products.orEmpty().size)
            val detalle = api.obtenerDetalle(121)
            assertEquals(121, detalle.id)
            assertEquals("iPhone 5s", detalle.title)
            assertEquals(listOf("/products/category/smartphones", "/products/121"), rutas)
        } finally {
            cliente.dispatcher.executorService.shutdown()
            cliente.connectionPool.evictAll()
        }
    }
}
