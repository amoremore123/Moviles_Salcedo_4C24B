package com.tecsup.smartcatalog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colores de marca: fucsia con degradado a morado para cabeceras y botones destacados. */
object ColoresMarca {
    val Fucsia = Color(0xFFE91E8C)
    val Morado = Color(0xFF9C27B0)
    val Corazon = Color(0xFFFF1744)
    val degradado: Brush get() = Brush.horizontalGradient(listOf(Fucsia, Morado))
}

private val LightColors = lightColorScheme(
    primary = ColoresMarca.Fucsia,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0EF),
    onPrimaryContainer = Color(0xFF6B0040),
    secondary = ColoresMarca.Morado,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E0F7),
    onSecondaryContainer = Color(0xFF3E0A4A),
    tertiary = ColoresMarca.Corazon,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF8E1F9E),
    onTertiaryContainer = Color.White,
    background = Color(0xFFFFF2F8),
    onBackground = Color(0xFF2A1020),
    surface = Color.White,
    onSurface = Color(0xFF2A1020),
    surfaceVariant = Color(0xFFFFE6F1),
    onSurfaceVariant = Color(0xFF6E4A5E),
    outline = Color(0xFFB98AA3),
    outlineVariant = Color(0xFFF3CFE0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF7AB8),
    onPrimary = Color(0xFF5C0033),
    primaryContainer = Color(0xFF8A1758),
    onPrimaryContainer = Color(0xFFFFD9E8),
    secondary = Color(0xFFE29BF0),
    onSecondary = Color(0xFF4A0E57),
    secondaryContainer = Color(0xFF5E1F6B),
    onSecondaryContainer = Color(0xFFF8D8FF),
    tertiary = Color(0xFFFF6B81),
    onTertiary = Color(0xFF5C0012),
    tertiaryContainer = Color(0xFF6A1B7A),
    onTertiaryContainer = Color(0xFFFCE4FF),
    background = Color(0xFF1A0E15),
    onBackground = Color(0xFFF7E3EC),
    surface = Color(0xFF26151F),
    onSurface = Color(0xFFF7E3EC),
    surfaceVariant = Color(0xFF3A2230),
    onSurfaceVariant = Color(0xFFDDBFCD),
    outline = Color(0xFFA6899A),
    outlineVariant = Color(0xFF55394A)
)

private val BaseTypography = Typography()
private val CatalogTypography = Typography(
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
fun SmartCatalogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CatalogTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
