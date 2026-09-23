package com.tecsup.mibodega.ui.cliente.modelo

import androidx.compose.ui.graphics.vector.ImageVector

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val icono: ImageVector
)
