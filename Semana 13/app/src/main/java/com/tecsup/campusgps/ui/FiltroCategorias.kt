package com.tecsup.campusgps.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Chips de categoría. Solo dibuja: la categoría activa y el filtro los decide el ViewModel.
 */
@Composable
fun FiltroCategorias(
    categorias: List<String>,
    activa: String?,
    onSeleccionar: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = activa == null,
            onClick = { onSeleccionar(null) },
            label = { Text("Todas") }
        )
        categorias.forEach { cat ->
            FilterChip(
                selected = activa == cat,
                onClick = { onSeleccionar(cat) },
                label = { Text(cat) }
            )
        }
    }
}
