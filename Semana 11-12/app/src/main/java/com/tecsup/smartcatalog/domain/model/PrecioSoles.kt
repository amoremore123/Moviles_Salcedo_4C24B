package com.tecsup.smartcatalog.domain.model

import java.math.RoundingMode

// Referencia fija para el catálogo offline. BCRP, serie PD04640PD, 10/11/2026.
private val solesPorDolar = "3.421".toBigDecimal()

internal const val NOTA_PRECIOS = "Precios referenciales en soles · US\$ 1 = S/ 3.421"

internal fun precioEnSoles(precio: Double): Double =
    precio.toBigDecimal().multiply(solesPorDolar).setScale(2, RoundingMode.HALF_UP).toDouble()

internal fun solesADolares(soles: Double): Double =
    soles.toBigDecimal().divide(solesPorDolar, 2, RoundingMode.HALF_UP).toDouble()
