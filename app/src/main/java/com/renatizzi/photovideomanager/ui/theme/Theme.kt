package com.renatizzi.photovideomanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B4F72),
    onPrimary = Color.White,
    secondary = Color(0xFF148F77),
    background = Color(0xFFF4F7F9),
    surface = Color.White,
    onSurface = Color(0xFF1A2330),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5DADE2),
    onPrimary = Color(0xFF0B1C28),
    secondary = Color(0xFF48C9B0),
    background = Color(0xFF0F1720),
    surface = Color(0xFF1A2330),
    onSurface = Color(0xFFE8EEF4),
)

@Composable
fun PvmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
