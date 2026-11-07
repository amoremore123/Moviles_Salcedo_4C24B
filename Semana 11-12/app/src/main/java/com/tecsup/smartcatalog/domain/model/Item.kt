package com.tecsup.smartcatalog.domain.model

data class Item(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagen: String,
    val categoria: String,
    val favorito: Boolean = false
)
