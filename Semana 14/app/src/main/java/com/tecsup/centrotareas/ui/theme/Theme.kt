package com.tecsup.centrotareas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// El rediseño es claro (fondo lavanda y tarjetas blancas), así que usa un solo esquema
private val Esquema = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulOscuro,
    secondary = Morado,
    background = FondoApp,
    onBackground = TextoFuerte,
    surface = Color.White,
    onSurface = TextoFuerte,
    surfaceVariant = GrisPista,
    onSurfaceVariant = GrisTexto,
    error = RojoError,
    errorContainer = RojoFondo,
    onErrorContainer = RojoTexto
)

@Composable
fun CentroTareasTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}
