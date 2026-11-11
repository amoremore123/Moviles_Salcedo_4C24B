package com.tecsup.smartcatalog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA8F2E4),
    onPrimaryContainer = Color(0xFF003B35),
    secondary = Color(0xFF47645F),
    secondaryContainer = Color(0xFFD5EAE4),
    onSecondaryContainer = Color(0xFF203530),
    tertiaryContainer = Color(0xFF173630),
    onTertiaryContainer = Color(0xFFE8F7F1),
    background = Color(0xFFF4F8F6),
    onBackground = Color(0xFF182824),
    surface = Color.White,
    onSurface = Color(0xFF182824),
    surfaceVariant = Color(0xFFE5EFEB),
    onSurfaceVariant = Color(0xFF425852),
    outline = Color(0xFF708780),
    outlineVariant = Color(0xFFCBDCD6)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CD6C9),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = Color(0xFFA8F2E4),
    secondary = Color(0xFFB7CCC5),
    secondaryContainer = Color(0xFF304B43),
    onSecondaryContainer = Color(0xFFD5EAE4),
    tertiaryContainer = Color(0xFF23453D),
    onTertiaryContainer = Color(0xFFE8F7F1),
    background = Color(0xFF101B18),
    onBackground = Color(0xFFE6F0EC),
    surface = Color(0xFF182622),
    onSurface = Color(0xFFE6F0EC),
    surfaceVariant = Color(0xFF273B35),
    onSurfaceVariant = Color(0xFFBDCEC8),
    outline = Color(0xFF879D94),
    outlineVariant = Color(0xFF435A51)
)

private val BaseTypography = Typography()
private val CatalogTypography = Typography(
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
)

@Composable
fun SmartCatalogTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CatalogTypography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(22.dp),
            extraLarge = RoundedCornerShape(30.dp)
        ),
        content = content
    )
}
