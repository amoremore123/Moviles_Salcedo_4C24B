package com.tecsup.ayudacampus.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

internal fun colorPrioridad(prioridad: String): Color = when (prioridad) {
    "ALTA" -> Color(0xFFDC2626)
    "MEDIA" -> Color(0xFFB45309)
    else -> Color(0xFF6B7280)
}

@Composable
fun PrioridadChip(prioridad: String) {
    val color = colorPrioridad(prioridad)
    val texto = when (prioridad) { "ALTA" -> "Alta"; "MEDIA" -> "Media"; else -> "Baja" }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = .12f)) {
        Text(texto, Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = if (MaterialTheme.colorScheme.background.luminance() < .5f) color.copy(red = (color.red + 1f) / 2f, green = (color.green + 1f) / 2f, blue = (color.blue + 1f) / 2f) else color, style = MaterialTheme.typography.labelSmall)
    }
}
