package com.tecsup.smartcatalog.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Contenedor observado en products/category/smartphones. Consumir products con orEmpty(). */
data class ProductosResponseDto(
    @SerializedName("products") val products: List<ProductoDto?>? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("skip") val skip: Int? = null,
    @SerializedName("limit") val limit: Int? = null
)
