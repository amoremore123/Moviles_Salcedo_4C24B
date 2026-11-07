package com.tecsup.smartcatalog.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Solo los campos de DummyJSON que necesita el catálogo; acepta ausencias y null del JSON. */
data class ProductoDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("category") val category: String? = null
)
