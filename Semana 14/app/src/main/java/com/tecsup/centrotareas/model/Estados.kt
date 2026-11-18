package com.tecsup.centrotareas.model

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    data object Calculando : PrimosUi
    // Mejora 5: también lleva la suma de los primos encontrados
    data class Listo(val cantidad: Int, val milisegundos: Long, val suma: Long = 0) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}

// Mejora 5: resultado de recorrer 2..N una sola vez (cantidad y suma de primos)
data class ResultadoPrimos(val cantidad: Int, val suma: Long)

// Mejora 4: tiempos acumulados de cada modo de carga (en milisegundos)
data class ComparacionTiempos(
    val unaPorUna: List<Long> = emptyList(),
    val aLaVez: List<Long> = emptyList()
)
