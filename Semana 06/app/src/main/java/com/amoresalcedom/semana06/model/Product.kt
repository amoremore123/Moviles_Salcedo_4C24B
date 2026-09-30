package com.amoresalcedom.semana06.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String = "Más vendidos",
    val isFavorite: Boolean = false
)
