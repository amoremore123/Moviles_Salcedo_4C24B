package com.tecsup.smartcatalog.data.remote

import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.data.remote.dto.ProductosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("products/category/smartphones")
    suspend fun obtenerSmartphones(): ProductosResponseDto

    @GET("products/{id}")
    suspend fun obtenerDetalle(@Path("id") id: Int): ProductoDto
}
