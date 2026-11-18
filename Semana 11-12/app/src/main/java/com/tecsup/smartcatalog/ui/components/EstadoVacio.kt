package com.tecsup.smartcatalog.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tecsup.smartcatalog.R

@Composable
fun EstadoVacio(
    titulo: String = "Sin elementos",
    descripcion: String = "No hay elementos para mostrar.",
    modifier: Modifier = Modifier
) {
    EstadoConGatito(R.drawable.gatito_listo, modifier, tamano = 150.dp) {
        Text(
            text = titulo,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
