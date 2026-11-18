package com.tecsup.campusgps.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.campusgps.model.LugarConDistancia
import com.tecsup.campusgps.util.formatearDistancia

/**
 * Solo dibuja la lista que recibe. No conoce al ViewModel:
 * avisa del toque con onClick(id) y la pantalla decide qué hacer.
 */
@Composable
fun ListaLugares(
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // Mejora 2: ids dentro del radio (ya calculados por el ViewModel) y el radio actual
    dentroDelRadio: Set<Int> = emptySet(),
    radio: Int? = null
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lugares, key = { it.lugar.id }) { item ->
            TarjetaLugar(
                item = item,
                esSeleccionado = item.lugar.id == seleccionado,
                dentro = if (item.lugar.id in dentroDelRadio) radio else null,
                onClick = { onClick(item.lugar.id) }
            )
        }
    }
}

@Composable
private fun TarjetaLugar(
    item: LugarConDistancia,
    esSeleccionado: Boolean,
    dentro: Int?, // radio en metros si el lugar está dentro; null si no
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esSeleccionado) colores.primaryContainer else colores.surfaceVariant
        ),
        border = if (esSeleccionado) BorderStroke(2.dp, colores.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    // Verde si está dentro del radio
                    .background(if (dentro != null) colores.secondary else colores.primary, CircleShape)
            )
            Column(Modifier.weight(1f)) {
                Text(item.lugar.nombre, fontWeight = FontWeight.Bold)
                Text(
                    item.lugar.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.metros?.let { formatearDistancia(it) } ?: "Sin calcular",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (dentro != null) {
                    Text(
                        "Dentro de $dentro m",
                        style = MaterialTheme.typography.labelSmall,
                        color = colores.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
