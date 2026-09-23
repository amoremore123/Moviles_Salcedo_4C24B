package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tecsup.mibodega.ui.cliente.modelo.listaCategorias
import com.tecsup.mibodega.ui.theme.BodegaTheme
import com.tecsup.mibodega.ui.theme.GrisClaro
import com.tecsup.mibodega.ui.theme.VerdeBodega

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCategorias(
    onNavigateInicio: () -> Unit,
    onNavigateCategorias: () -> Unit,
    onNavigatePedidos: () -> Unit,
    onNavigatePerfil: () -> Unit,
    onCategoriaClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías de Productos", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            BarraInferiorNavegacion(
                selectedIndex = 1,
                onInicio = onNavigateInicio,
                onCategorias = onNavigateCategorias,
                onPedidos = onNavigatePedidos,
                onPerfil = onNavigatePerfil
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Explora nuestros productos por categoría",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(listaCategorias) { categoria ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoriaClick(categoria) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(VerdeBodega.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = categoria,
                                tint = VerdeBodega
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = categoria,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (categoria == "Todos") "Ver todos los productos disponibles" else "Productos de la sección $categoria",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ver productos",
                            tint = VerdeBodega
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BarraInferiorNavegacion(
    selectedIndex: Int,
    onInicio: () -> Unit,
    onCategorias: () -> Unit,
    onPedidos: () -> Unit,
    onPerfil: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = onInicio,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = onCategorias,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Categorías") },
            label = { Text("Categorías") }
        )
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = onPedidos,
            icon = { Icon(Icons.Default.Receipt, contentDescription = "Pedidos") },
            label = { Text("Pedidos") }
        )
        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = onPerfil,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaCategoriasPreview() {
    BodegaTheme {
        PantallaCategorias({}, {}, {}, {}, {})
    }
}
