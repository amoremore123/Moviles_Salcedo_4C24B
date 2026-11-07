package com.tecsup.smartcatalog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4055C8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E8FF),
    onPrimaryContainer = Color(0xFF223172),
    secondary = Color(0xFF566176),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF182238),
    surface = Color.White,
    onSurface = Color(0xFF182238),
    onSurfaceVariant = Color(0xFF606A7D),
    outlineVariant = Color(0xFFD8DEEB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB4C0FF),
    primaryContainer = Color(0xFF26386C),
    onPrimaryContainer = Color(0xFFE1E6FF),
    secondary = Color(0xFFBBC7DB),
    background = Color(0xFF101622),
    surface = Color(0xFF192233),
    onSurface = Color(0xFFEAF0FC),
    onSurfaceVariant = Color(0xFFB9C3D6)
)

@Composable
fun SmartCatalogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
