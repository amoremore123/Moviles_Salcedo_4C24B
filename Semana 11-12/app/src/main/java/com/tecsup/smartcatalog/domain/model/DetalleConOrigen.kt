package com.tecsup.smartcatalog.domain.model

enum class OrigenDetalle { REMOTO, LOCAL }

/** El origen es explícito: un respaldo local nunca representa una consulta HTTP exitosa. */
data class DetalleConOrigen(val item: Item, val origen: OrigenDetalle)
