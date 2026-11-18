package com.tecsup.centrotareas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Diseño claro: fondo lavanda, tarjetas blancas y color principal índigo
private val Esquema = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = LavandaSuave,
    onPrimaryContainer = IndigoOscuro,
    secondary = Violeta,
    background = FondoApp,
    onBackground = TextoFuerte,
    surface = Color.White,
    onSurface = TextoFuerte,
    surfaceVariant = PistaBarra,
    onSurfaceVariant = GrisTexto,
    outline = LavandaBorde,
    error = RojoError,
    errorContainer = RojoFondo,
    onErrorContainer = RojoTexto
)

@Composable
fun CentroTareasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}
