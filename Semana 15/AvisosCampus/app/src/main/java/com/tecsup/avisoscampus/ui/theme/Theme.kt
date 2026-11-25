package com.tecsup.avisoscampus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = Morado,
    onPrimary = Blanco,
    primaryContainer = Lila,
    onPrimaryContainer = MoradoOscuro,
    secondary = Morado,
    secondaryContainer = Lila,
    onSecondaryContainer = MoradoOscuro,
    background = LilaFondo,
    onBackground = MoradoOscuro,
    surface = Blanco,
    onSurface = MoradoOscuro,
    // Las Card de Material 3 usan este color: tarjetas blancas
    surfaceContainerHighest = Blanco,
    surfaceContainer = Blanco
)

private val EsquemaOscuro = darkColorScheme(
    primary = MoradoClaro,
    primaryContainer = MoradoNoche,
    secondary = MoradoClaro,
    secondaryContainer = MoradoNoche,
    background = FondoNoche,
    surface = TarjetaNoche,
    surfaceContainerHighest = TarjetaNoche,
    surfaceContainer = TarjetaNoche
)

// Esquinas redondeadas: las tarjetas (medium) usan 20dp
private val Formas = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun AvisosCampusTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        shapes = Formas,
        content = content
    )
}
