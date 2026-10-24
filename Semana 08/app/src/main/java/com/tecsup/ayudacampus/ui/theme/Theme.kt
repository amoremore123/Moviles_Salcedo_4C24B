package com.tecsup.ayudacampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AyudaTheme(rol: String, modoOscuro: Boolean, content: @Composable () -> Unit) {
    val soporte = rol == "ADMIN"
    val principal = Color(if (soporte) 0xFF7C3AED else 0xFF0891B2)
    val colores = if (modoOscuro) {
        darkColorScheme(
            primary = Color(if (soporte) 0xFFD0BCFF else 0xFF67E8F9), onPrimary = Color(0xFF17212B),
            primaryContainer = Color(if (soporte) 0xFF38205F else 0xFF123B49),
            onPrimaryContainer = Color(if (soporte) 0xFFEDE4FF else 0xFFCFFAFE),
            background = Color(0xFF101820), surface = Color(0xFF17212B),
            onBackground = Color(0xFFE5EDF4), onSurface = Color(0xFFE5EDF4),
            onSurfaceVariant = Color(0xFFB9C9D7), outline = Color(0xFF8B9EAF)
        )
    } else {
        lightColorScheme(
            primary = principal, onPrimary = Color.White,
            primaryContainer = Color(if (soporte) 0xFFEDE4FF else 0xFFCFFAFE),
            onPrimaryContainer = Color(if (soporte) 0xFF4C1D95 else 0xFF155E75),
            background = Color(if (soporte) 0xFFFAF8FF else 0xFFF7FDFE),
            surface = Color.White, onSurface = Color(0xFF172A3A),
            onBackground = Color(0xFF172A3A), onSurfaceVariant = Color(0xFF526375),
            outline = Color(0xFF7C8995), outlineVariant = Color(0xFFDAE3E9)
        )
    }
    MaterialTheme(colorScheme = colores, content = content)
}
