package com.amoresalcedom.semana06.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.amoresalcedom.semana06.model.CartItem
import com.amoresalcedom.semana06.model.Product
import com.amoresalcedom.semana06.ui.*

@Composable
fun AppNavegacion(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // State for products in memory
    var products by remember {
        mutableStateOf(
            listOf(
                Product(1, "Inca Kola 1.5L", 7.50, "Bebidas", "Gaseosa sabor nacional de 1.5 litros"),
                Product(2, "Coca Cola 1.5L", 7.80, "Bebidas", "Gaseosa refrescante de 1.5 litros"),
                Product(3, "Arroz Superior 1kg", 4.50, "Abarrotes", "Arroz grano selecto extra"),
                Product(4, "Azúcar Rubia 1kg", 4.20, "Abarrotes", "Azúcar rubia de caña pura"),
                Product(5, "Galletas Soda Field", 2.50, "Snacks", "Galletas saladas crujientes paquete x6"),
                Product(6, "Papas Lays Clásica", 3.00, "Snacks", "Papas fritas crocantes bolsa mediana")
            )
        )
    }

    // Cart items state
    var cartItems by remember { mutableStateOf(listOf<CartItem>()) }

    // Delivery info state
    var deliveryDireccion by remember { mutableStateOf("") }
    var deliveryTelefono by remember { mutableStateOf("") }

    val bottomNavItems = listOf(
        Triple("inicio", "Inicio", Icons.Default.Home),
        Triple("carrito", "Carrito", Icons.Default.ShoppingCart),
        Triple("pedidos", "Pedidos", Icons.Default.Receipt),
        Triple("perfil", "Perfil", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on main tabs
    val showBottomBar = currentRoute in listOf("inicio", "carrito", "pedidos", "perfil")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (route == "carrito" && cartItems.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text(cartItems.sumOf { it.quantity }.toString()) }
                                        }
                                    ) {
                                        Icon(icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = label)
                                }
                            },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                PantallaLogin(
                    onLoginSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.CrearCuenta.route)
                    }
                )
            }

            composable(Screen.CrearCuenta.route) {
                PantallaCrearCuenta(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable("inicio") {
                PantallaInicio(
                    products = products,
                    onProductClick = { productId ->
                        navController.navigate(Screen.DetalleProducto.createRoute(productId))
                    }
                )
            }

            composable("pedidos") {
                PedidosScreen()
            }

            composable("perfil") {
                PerfilScreen()
            }

            composable(
                route = Screen.DetalleProducto.route,
                arguments = listOf(navArgument("productoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                PantallaDetalleProducto(
                    productoId = productoId,
                    products = products,
                    onAddToCart = { product, qty ->
                        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
                        cartItems = if (existingIndex >= 0) {
                            cartItems.mapIndexed { index, item ->
                                if (index == existingIndex) item.copy(quantity = item.quantity + qty) else item
                            }
                        } else {
                            cartItems + CartItem(product, qty)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("carrito") {
                PantallaCarrito(
                    cartItems = cartItems,
                    onUpdateQuantity = { productId, newQty ->
                        cartItems = cartItems.map {
                            if (it.product.id == productId) it.copy(quantity = newQty) else it
                        }
                    },
                    onRemoveItem = { productId ->
                        cartItems = cartItems.filter { it.product.id != productId }
                    },
                    onProceedToCheckout = {
                        navController.navigate(Screen.DatosEntrega.route)
                    }
                )
            }

            composable(Screen.DatosEntrega.route) {
                PantallaDatosEntrega(
                    onConfirmOrder = { dir, tlf, _ ->
                        deliveryDireccion = dir
                        deliveryTelefono = tlf
                        cartItems = emptyList() // clear cart on order
                        navController.navigate(Screen.Confirmacion.route) {
                            popUpTo(Screen.Inicio.route) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Confirmacion.route) {
                PantallaConfirmacion(
                    direccion = deliveryDireccion,
                    telefono = deliveryTelefono,
                    onBackToHome = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
