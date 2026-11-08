package com.tecsup.smartcatalog.testing

import androidx.room.Room
import com.tecsup.smartcatalog.AppContainer
import com.tecsup.smartcatalog.SmartCatalogApplication
import com.tecsup.smartcatalog.data.local.AppDatabase
import com.tecsup.smartcatalog.data.remote.ApiService
import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import java.util.concurrent.atomic.AtomicInteger
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
        override suspend fun obtenerSmartphones(): ProductosResponseDto {
            consultasLista.incrementAndGet()
            pendiente?.await()
            return ProductosResponseDto(listOf(ProductoDto(id = 1, title = "Modelo de prueba", category = "smartphones")))
        }
        override suspend fun obtenerDetalle(id: Int): ProductoDto {
            consultasDetalle.incrementAndGet()
            return ProductoDto(id = id, title = "Detalle de prueba", category = "smartphones")
        }
    }
}
