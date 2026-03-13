package com.apple101012.voicessh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0A6C63),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8F3EE),
    onPrimaryContainer = Color(0xFF062C28),
    secondary = Color(0xFF3B5B92),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE6FF),
    onSecondaryContainer = Color(0xFF172847),
    background = Color(0xFFF4F0E8),
    onBackground = Color(0xFF1B1B1B),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF181C19),
    surfaceVariant = Color(0xFFE5E0D6),
    onSurfaceVariant = Color(0xFF464238),
    inverseSurface = Color(0xFF10211E),
    inverseOnSurface = Color(0xFFEAF5F1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7AD2C5),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005049),
    onPrimaryContainer = Color(0xFFD8F3EE),
    secondary = Color(0xFFAFC3FF),
    onSecondary = Color(0xFF0A214F),
    secondaryContainer = Color(0xFF233A68),
    onSecondaryContainer = Color(0xFFDDE6FF),
    background = Color(0xFF121515),
    onBackground = Color(0xFFE6E2DC),
    surface = Color(0xFF171B1A),
    onSurface = Color(0xFFE6E2DC),
    surfaceVariant = Color(0xFF302F2A),
    onSurfaceVariant = Color(0xFFCAC5BA),
    inverseSurface = Color(0xFFEAF5F1),
    inverseOnSurface = Color(0xFF10211E),
)

@Composable
fun VoiceSshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
