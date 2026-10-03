package com.tecsup.mibodega.ui.cliente

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.mibodega.ui.cliente.modelo.ItemCarrito
import com.tecsup.mibodega.ui.cliente.modelo.Producto
import com.tecsup.mibodega.ui.cliente.modelo.listaProductosFake
import com.tecsup.mibodega.ui.cliente.screens.PantallaLogin
import com.tecsup.mibodega.ui.cliente.screens.PantallaCrearCuenta
import com.tecsup.mibodega.ui.cliente.screens.PantallaInicio
import com.tecsup.mibodega.ui.cliente.screens.PantallaCategorias
import com.tecsup.mibodega.ui.cliente.screens.PantallaPedidos
import com.tecsup.mibodega.ui.cliente.screens.PantallaPerfil
import com.tecsup.mibodega.ui.cliente.screens.PantallaDetalleProducto
import com.tecsup.mibodega.ui.cliente.screens.PantallaCarrito
import com.tecsup.mibodega.ui.cliente.screens.PantallaDatosEntrega
import com.tecsup.mibodega.ui.cliente.screens.PantallaConfirmacion

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    var carrito by remember { mutableStateOf<List<ItemCarrito>>(emptyList()) }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            PantallaLogin(
                onRegistrarse = { navController.navigate(Rutas.CREAR_CUENTA) },
                onLoginExitoso = {
                    navController.navigate(Rutas.inicio("Todos")) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onTerminos = { /* TODO */ }
            )
        }

        composable(Rutas.CREAR_CUENTA) {
            PantallaCrearCuenta(
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = {
                    navController.navigate(Rutas.inicio("Todos")) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.INICIO,
            arguments = listOf(navArgument("categoria") { defaultValue = "Todos" })
        ) { backStackEntry ->
            val categoriaInicial = backStackEntry.arguments?.getString("categoria") ?: "Todos"
            PantallaInicio(
                categoriaInicial = categoriaInicial,
                cantidadCarrito = carrito.sumOf { it.cantidad },
                onVerCarrito = { navController.navigate(Rutas.CARRITO) },
                onProductoClick = { producto ->
                    navController.navigate(Rutas.detalle(producto.id))
                },
                onAgregarProducto = { producto ->
                    carrito = agregarOSumarProducto(carrito, producto, 1)
                },
                onNavigateInicio = { navController.navigate(Rutas.inicio("Todos")) { popUpTo(0) { inclusive = true } } },
                onNavigateCategorias = { navController.navigate(Rutas.CATEGORIAS) { launchSingleTop = true } },
                onNavigatePedidos = { navController.navigate(Rutas.PEDIDOS) { launchSingleTop = true } },
                onNavigatePerfil = { navController.navigate(Rutas.PERFIL) { launchSingleTop = true } }
            )
        }

        composable(Rutas.CATEGORIAS) {
            PantallaCategorias(
                onNavigateInicio = { navController.navigate(Rutas.inicio("Todos")) { popUpTo(0) { inclusive = true } } },
                onNavigateCategorias = { },
                onNavigatePedidos = { navController.navigate(Rutas.PEDIDOS) { launchSingleTop = true } },
                onNavigatePerfil = { navController.navigate(Rutas.PERFIL) { launchSingleTop = true } },
                onCategoriaClick = { categoria ->
                    navController.navigate(Rutas.inicio(categoria)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.PEDIDOS) {
            PantallaPedidos(
                onNavigateInicio = { navController.navigate(Rutas.inicio("Todos")) { popUpTo(0) { inclusive = true } } },
                onNavigateCategorias = { navController.navigate(Rutas.CATEGORIAS) { launchSingleTop = true } },
                onNavigatePedidos = { },
                onNavigatePerfil = { navController.navigate(Rutas.PERFIL) { launchSingleTop = true } }
            )
        }

        composable(Rutas.PERFIL) {
            PantallaPerfil(
                onNavigateInicio = { navController.navigate(Rutas.inicio("Todos")) { popUpTo(0) { inclusive = true } } },
                onNavigateCategorias = { navController.navigate(Rutas.CATEGORIAS) { launchSingleTop = true } },
                onNavigatePedidos = { navController.navigate(Rutas.PEDIDOS) { launchSingleTop = true } },
                onNavigatePerfil = { },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
            val producto = listaProductosFake.first { it.id == productoId }

            PantallaDetalleProducto(
                producto = producto,
                onVolver = { navController.popBackStack() },
                onAgregarAlCarrito = { productoSeleccionado, cantidad ->
                    carrito = agregarOSumarProducto(carrito, productoSeleccionado, cantidad)
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.CARRITO) {
            PantallaCarrito(
                carrito = carrito,
                onVolver = { navController.popBackStack() },
                onIncrementar = { producto ->
                    carrito = carrito.map {
                        if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1) else it
                    }
                },
                onDecrementar = { producto ->
                    carrito = carrito.mapNotNull {
                        when {
                            it.producto.id != producto.id -> it
                            it.cantidad > 1 -> it.copy(cantidad = it.cantidad - 1)
                            else -> null
                        }
                    }
                },
                onEliminar = { producto ->
                    carrito = carrito.filterNot { it.producto.id == producto.id }
                },
                onContinuarPedido = { navController.navigate(Rutas.DATOS_ENTREGA) }
            )
        }

        composable(Rutas.DATOS_ENTREGA) {
            PantallaDatosEntrega(
                carrito = carrito,
                onVolver = { navController.popBackStack() },
                onConfirmarPedido = {
                    carrito = emptyList()
                    navController.navigate(Rutas.CONFIRMACION) {
                        popUpTo(Rutas.CARRITO) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.CONFIRMACION) {
            PantallaConfirmacion(
                onVerEstado = {
                    navController.navigate(Rutas.PEDIDOS) {
                        popUpTo(Rutas.CONFIRMACION) { inclusive = true }
                    }
                },
                onVolverInicio = {
                    navController.navigate(Rutas.inicio("Todos")) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun agregarOSumarProducto(
    carrito: List<ItemCarrito>,
    producto: Producto,
    cantidad: Int
): List<ItemCarrito> {
    val itemExistente = carrito.find { it.producto.id == producto.id }
    return if (itemExistente != null) {
        carrito.map {
            if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + cantidad) else it
        }
    } else {
        carrito + ItemCarrito(producto = producto, cantidad = cantidad)
    }
}
