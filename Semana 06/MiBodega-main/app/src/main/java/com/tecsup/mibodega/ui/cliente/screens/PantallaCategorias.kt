package com.tecsup.mibodega.ui.cliente.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
                title = { Text("Categorías", fontWeight = FontWeight.Bold) }
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(listaCategorias.filter { it != "Todos" }) { categoria ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoriaClick(categoria) },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoria,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
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
            icon = { Icon(Icons.Default.List, contentDescription = "Categorías") },
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
