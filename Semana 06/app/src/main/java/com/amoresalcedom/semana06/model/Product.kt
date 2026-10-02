package com.amoresalcedom.semana06.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val description: String = "Producto fresco y de calidad de Mi Bodega.",
    val isFavorite: Boolean = false
)
