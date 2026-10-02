package com.amoresalcedom.semana06.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CrearCuenta : Screen("crear_cuenta")
    object Inicio : Screen("inicio")
    object DetalleProducto : Screen("detalle/{productoId}") {
        fun createRoute(productoId: Int) = "detalle/$productoId"
    }
    object Carrito : Screen("carrito")
    object DatosEntrega : Screen("datos_entrega")
    object Confirmacion : Screen("confirmacion")
}
