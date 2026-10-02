package com.amoresalcedom.semana06.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amoresalcedom.semana06.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleProducto(
    productoId: Int,
    products: List<Product>,
    onAddToCart: (Product, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val product = products.find { it.id == productoId }
    var quantity by remember { mutableStateOf(1) }
    var showAddedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (product == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Producto no encontrado")
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "S/ %.2f".format(product.price), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Categoría: ${product.category}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = product.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(32.dp))

                    // Quantity Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Cantidad:", style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = { if (quantity > 1) quantity-- }) {
                            Text("-")
                        }
                        Text(text = quantity.toString(), style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = { quantity++ }) {
                            Text("+")
                        }
                    }

                    if (showAddedMessage) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Producto agregado al carrito!",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        onAddToCart(product, quantity)
                        showAddedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar al Carrito (S/ %.2f)".format(product.price * quantity))
                }
            }
        }
    }
}
