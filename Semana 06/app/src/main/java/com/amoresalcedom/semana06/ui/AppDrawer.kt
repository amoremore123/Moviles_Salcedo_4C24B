package com.amoresalcedom.semana06.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class DrawerScreen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : DrawerScreen("inicio", "Inicio", Icons.Default.Home)
    object Pedidos : DrawerScreen("pedidos", "Mis pedidos", Icons.Default.ShoppingBag)
    object Favoritos : DrawerScreen("favoritos", "Favoritos", Icons.Default.Favorite)
    object Perfil : DrawerScreen("perfil", "Perfil", Icons.Default.Person)
    object Logout : DrawerScreen("logout", "Cerrar sesión", Icons.Default.Logout)
}

@Composable
fun AppDrawer(
    currentRoute: String,
    favoritesCount: Int,
    onNavigate: (DrawerScreen) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val items = listOf(
        DrawerScreen.Inicio,
        DrawerScreen.Pedidos,
        DrawerScreen.Favoritos,
        DrawerScreen.Perfil,
        DrawerScreen.Logout
    )

    ModalDrawerSheet {
        // Drawer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MR",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Maria Rojas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "maria@tecsup.edu.pe",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Drawer Items
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationDrawerItem(
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = screen.title)
                        if (screen is DrawerScreen.Favoritos && favoritesCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text(text = favoritesCount.toString())
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                },
                selected = selected,
                onClick = {
                    onNavigate(screen)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
