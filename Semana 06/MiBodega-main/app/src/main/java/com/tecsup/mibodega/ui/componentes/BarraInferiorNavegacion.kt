package com.tecsup.mibodega.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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
