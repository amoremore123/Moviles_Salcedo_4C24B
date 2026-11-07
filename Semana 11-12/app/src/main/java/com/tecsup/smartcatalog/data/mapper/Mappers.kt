package com.tecsup.smartcatalog.data.mapper

import com.tecsup.smartcatalog.data.remote.dto.ProductoDto
import com.tecsup.smartcatalog.domain.model.Item

/** El ID positivo es obligatorio; los campos descriptivos ausentes reciben valores seguros. */
fun ProductoDto.toItem(): Item {
    val idValido = requireNotNull(id?.takeIf { it > 0 }) { "El producto necesita un ID positivo." }
    return Item(
        id = idValido,
        nombre = title?.trim().orEmpty().ifBlank { "Producto sin nombre" },
        descripcion = description.orEmpty(),
        precio = price?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0,
        imagen = thumbnail.orEmpty(),
        categoria = category.orEmpty(),
        favorito = false
    )
}

/** Entrada segura para DTO inválidos: informa el fallo sin inventar un ID ni lanzar al consumidor. */
fun ProductoDto.toItemSeguro(): Result<Item> = runCatching { toItem() }
