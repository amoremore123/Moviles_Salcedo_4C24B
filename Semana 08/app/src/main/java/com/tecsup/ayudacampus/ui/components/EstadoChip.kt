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

@Composable
fun EstadoChip(estado: String) {
    val (texto, color) = when (estado) {
        "ABIERTO" -> "Abierto" to Color(0xFFD97706)
        "EN_PROCESO" -> "En proceso" to Color(0xFF2563EB)
        "RESUELTO" -> "Resuelto" to Color(0xFF16A34A)
        else -> "Cerrado" to Color(0xFF6B7280)
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = .12f)) {
        Text(texto, Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = if (MaterialTheme.colorScheme.background.luminance() < .5f) color.copy(red = (color.red + 1f) / 2f, green = (color.green + 1f) / 2f, blue = (color.blue + 1f) / 2f) else color, style = MaterialTheme.typography.labelSmall)
    }
}
