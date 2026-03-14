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
    primary = Color(0xFF1F4E83),
    onPrimary = Color(0xFFF4F9FF),
    primaryContainer = Color(0xFFD4E6FA),
    onPrimaryContainer = Color(0xFF0D2943),
    secondary = Color(0xFF335B88),
    onSecondary = Color(0xFFF3F8FF),
    secondaryContainer = Color(0xFFDCE7F6),
    onSecondaryContainer = Color(0xFF152C44),
    tertiary = Color(0xFF006C8E),
    onTertiary = Color(0xFFF1FBFF),
    tertiaryContainer = Color(0xFFC8EDFF),
    onTertiaryContainer = Color(0xFF00344A),
    background = Color(0xFFF2F6FC),
    onBackground = Color(0xFF111C29),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111C29),
    surfaceVariant = Color(0xFFDDE6F2),
    onSurfaceVariant = Color(0xFF3A4E65),
    outline = Color(0xFF6E829A),
    outlineVariant = Color(0xFFC1CDDA),
    inverseSurface = Color(0xFF162231),
    inverseOnSurface = Color(0xFFE7F0FC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF75B5FF),
    onPrimary = Color(0xFF03213D),
    primaryContainer = Color(0xFF12304A),
    onPrimaryContainer = Color(0xFFD8E9FF),
    secondary = Color(0xFFAAC8E8),
    onSecondary = Color(0xFF0A2237),
    secondaryContainer = Color(0xFF1A2A3E),
    onSecondaryContainer = Color(0xFFD7E6F8),
    tertiary = Color(0xFF67D5FF),
    onTertiary = Color(0xFF003546),
    tertiaryContainer = Color(0xFF004C66),
    onTertiaryContainer = Color(0xFFC7F0FF),
    background = Color(0xFF091220),
    onBackground = Color(0xFFE6F2FF),
    surface = Color(0xFF0F1B2D),
    onSurface = Color(0xFFE6F2FF),
    surfaceVariant = Color(0xFF18273B),
    onSurfaceVariant = Color(0xFFA0B6CD),
    outline = Color(0xFF385372),
    outlineVariant = Color(0xFF25394F),
    inverseSurface = Color(0xFFE6F2FF),
    inverseOnSurface = Color(0xFF0C1726),
)

private val VoiceSshTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
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
