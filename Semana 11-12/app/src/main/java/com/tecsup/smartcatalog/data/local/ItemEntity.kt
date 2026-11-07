package com.tecsup.smartcatalog.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagen: String,
    val categoria: String,
    @ColumnInfo(defaultValue = "0") val favorito: Boolean = false
)
