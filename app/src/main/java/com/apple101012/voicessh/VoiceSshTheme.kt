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

private val GreenPrimary = Color(0xFF13EC5B)
private val GreenDark = Color(0xFF0D8A33)
private val NavyBackground = Color(0xFF050B13)
private val NavySurface = Color(0xFF0C1626)
private val NavyOnSurface = Color(0xFFEAF3FF)
private val NavySecondary = Color(0xFF92ABCA)

private val LightColors = lightColorScheme(
    primary = GreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFFFDF),
    onPrimaryContainer = Color(0xFF02340E),
    secondary = NavySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE6F2),
    onSecondaryContainer = Color(0xFF0C1626),
    background = Color(0xFFF2F6FC),
    onBackground = Color(0xFF050B13),
    surface = Color.White,
    onSurface = Color(0xFF050B13),
    surfaceVariant = Color(0xFFDDE6F2),
    onSurfaceVariant = Color(0xFF3A4E65),
    outline = Color(0xFF6E829A),
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color(0xFF022101),
    primaryContainer = Color(0xFF054817),
    onPrimaryContainer = Color(0xFFABFFAF),
    secondary = NavySecondary,
    onSecondary = Color(0xFF0C1626),
    secondaryContainer = Color(0xFF18273B),
    onSecondaryContainer = Color(0xFFD7E6F8),
    background = NavyBackground,
    onBackground = NavyOnSurface,
    surface = NavySurface,
    onSurface = NavyOnSurface,
    surfaceVariant = Color(0xFF18273B),
    onSurfaceVariant = Color(0xFFA0B6CD),
    outline = Color(0xFF385372),
)

private val VoiceSshTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
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
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
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
