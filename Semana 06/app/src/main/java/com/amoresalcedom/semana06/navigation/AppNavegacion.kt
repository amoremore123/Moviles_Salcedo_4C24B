package com.amoresalcedom.semana06.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.amoresalcedom.semana06.model.Product
import com.amoresalcedom.semana06.ui.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavegacion(
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Sample products matching Lab requirements
    var products by remember {
        mutableStateOf(
            listOf(
                Product(1, "Audifonos", 89.00),
                Product(2, "Smartwatch", 199.00),
                Product(3, "Funda celular", 25.00),
                Product(4, "Teclado Mecánico", 150.00),
                Product(5, "Mouse Inalámbrico", 65.00)
            )
        )
    }

    var currentRoute by remember { mutableStateOf(DrawerScreen.Inicio.route) }

    val favoritesCount = products.count { it.isFavorite }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                favoritesCount = favoritesCount,
                onNavigate = { screen ->
                    if (screen is DrawerScreen.Logout) {
                        // Handle logout or exit action
                    } else {
                        currentRoute = screen.route
                    }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = "TECSUP Store", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = when (currentRoute) {
                                    "inicio" -> "Más vendidos"
                                    "pedidos" -> "Mis pedidos"
                                    "favoritos" -> "Favoritos"
                                    "perfil" -> "Perfil"
                                    else -> "Tienda"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú principal"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            val onToggleFavorite: (Product) -> Unit = { productToToggle ->
                products = products.map {
                    if (it.id == productToToggle.id) it.copy(isFavorite = !it.isFavorite) else it
                }
            }

            val onShare: (Product) -> Unit = { _ ->
                // Simulate share action
            }

            val onReport: (Product) -> Unit = { _ ->
                // Simulate report action
            }

            when (currentRoute) {
                DrawerScreen.Inicio.route -> {
                    InicioScreen(
                        products = products,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare,
                        onReport = onReport,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                DrawerScreen.Pedidos.route -> {
                    PedidosScreen(modifier = Modifier.padding(innerPadding))
                }
                DrawerScreen.Favoritos.route -> {
                    FavoritosScreen(
                        products = products,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare,
                        onReport = onReport,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                DrawerScreen.Perfil.route -> {
                    PerfilScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
