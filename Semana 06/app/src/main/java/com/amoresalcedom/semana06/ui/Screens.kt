package com.amoresalcedom.semana06.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amoresalcedom.semana06.model.Product

@Composable
fun InicioScreen(
    products: List<Product>,
    onToggleFavorite: (Product) -> Unit,
    onShare: (Product) -> Unit,
    onReport: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Más vendidos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                TarjetaProducto(
                    product = product,
                    onToggleFavorite = onToggleFavorite,
                    onShare = onShare,
                    onReport = onReport
                )
            }
        }
    }
}

@Composable
fun FavoritosScreen(
    products: List<Product>,
    onToggleFavorite: (Product) -> Unit,
    onShare: (Product) -> Unit,
    onReport: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteProducts = products.filter { it.isFavorite }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Productos Favoritos (${favoriteProducts.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (favoriteProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes productos favoritos aún.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteProducts) { product ->
                    TarjetaProducto(
                        product = product,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare,
                        onReport = onReport
                    )
                }
            }
        }
    }
}

@Composable
fun PedidosScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mis Pedidos (Historial)",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun PerfilScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nombre: Maria Rojas",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Correo: maria@tecsup.edu.pe",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Rol: Estudiante TECSUP",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
