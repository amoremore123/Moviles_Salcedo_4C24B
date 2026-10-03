package com.tecsup.mibodega.ui.cliente

object Rutas {
    const val LOGIN = "login"
    const val CREAR_CUENTA = "crear_cuenta"
    const val INICIO = "inicio"
    const val DETALLE = "detalle/{productoId}"
    const val CARRITO = "carrito"
    const val DATOS_ENTREGA = "datos_entrega"
    const val CONFIRMACION = "confirmacion"

    fun detalle(productoId: Int) = "detalle/$productoId"
}
