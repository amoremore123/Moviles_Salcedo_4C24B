package com.tecsup.smartcatalog.testing

import androidx.room.Room
import com.tecsup.smartcatalog.AppContainer
import com.tecsup.smartcatalog.SmartCatalogApplication
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import java.util.concurrent.atomic.AtomicInteger
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred

class SmartCatalogTestApplication : SmartCatalogApplication() {
    val api = ApiDePrueba()
    val auth = AuthDePrueba()
    override val container by lazy {
        AppContainer(this, api, crearDatabase = { Room.inMemoryDatabaseBuilder(it, AppDatabase::class.java).build() }, crearAuth = { auth })
    }

    class ApiDePrueba : ApiService {
        val consultasLista = AtomicInteger()
        val consultasDetalle = AtomicInteger()
        var pendiente: CompletableDeferred<Unit>? = null
        var errorLista: IOException? = null
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            consultasLista.incrementAndGet()
            pendiente?.await()
            errorLista?.let { throw it }
            return ProductosResponseDto(listOf(producto(1, "Modelo de prueba")))
        }
        override suspend fun obtenerDetalle(id: Int): ProductoDto {
            consultasDetalle.incrementAndGet()
            return producto(id, "Detalle de prueba")
        }

        private fun producto(id: Int, nombre: String) = ProductoDto(
            id = id,
            title = nombre,
            description = "Smartphone de ejemplo con ilustración local.",
            price = 449.99,
            thumbnail = "android.resource://com.tecsup.smartcatalog/drawable/smartphone_aurora",
            category = "smartphones"
        )
    }
}
