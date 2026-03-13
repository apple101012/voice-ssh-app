package com.apple101012.voicessh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0E7C71),
    onPrimary = Color(0xFFFFFCF7),
    primaryContainer = Color(0xFFC8F1EC),
    onPrimaryContainer = Color(0xFF073934),
    secondary = Color(0xFFA45A2A),
    onSecondary = Color(0xFFFFF8F2),
    secondaryContainer = Color(0xFFF5DDCC),
    onSecondaryContainer = Color(0xFF4A240D),
    tertiary = Color(0xFF395E72),
    onTertiary = Color(0xFFF7FAFC),
    tertiaryContainer = Color(0xFFD7EAF4),
    onTertiaryContainer = Color(0xFF153443),
    background = Color(0xFFF7F1E8),
    onBackground = Color(0xFF1C1A17),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF1A1815),
    surfaceVariant = Color(0xFFE8DDD0),
    onSurfaceVariant = Color(0xFF4F453B),
    outline = Color(0xFF877565),
    outlineVariant = Color(0xFFD4C3B4),
    inverseSurface = Color(0xFF24211D),
    inverseOnSurface = Color(0xFFF8EFE5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DD8CB),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF0D5A51),
    onPrimaryContainer = Color(0xFFC8F1EC),
    secondary = Color(0xFFF0B78F),
    onSecondary = Color(0xFF51260A),
    secondaryContainer = Color(0xFF78401A),
    onSecondaryContainer = Color(0xFFFBE4D4),
    tertiary = Color(0xFFA8CDDE),
    onTertiary = Color(0xFF0D3445),
    tertiaryContainer = Color(0xFF254C5F),
    onTertiaryContainer = Color(0xFFD7EAF4),
    background = Color(0xFF111311),
    onBackground = Color(0xFFEAE1D7),
    surface = Color(0xFF181A18),
    onSurface = Color(0xFFF3EADF),
    surfaceVariant = Color(0xFF312B25),
    onSurfaceVariant = Color(0xFFD8C7B8),
    outline = Color(0xFFA2907F),
    outlineVariant = Color(0xFF4C433A),
    inverseSurface = Color(0xFFF8EFE5),
    inverseOnSurface = Color(0xFF24211D),
)

private val VoiceSshTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun VoiceSshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = VoiceSshTypography,
        content = content,
    )
}
