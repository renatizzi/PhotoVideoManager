package com.renatizzi.photovideomanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalShellTokens = staticCompositionLocalOf { ShellPalettes.PvmBlueLight }

@Composable
fun PvmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tokens = ShellPalettes.tokens(darkTheme)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = tokens.accent,
            onPrimary = tokens.topBarTitle,
            secondary = tokens.accentDark,
            background = tokens.pageBackground,
            surface = tokens.bottomBarBackground,
            onSurface = tokens.onPage,
        )
    } else {
        lightColorScheme(
            primary = tokens.topBarBackground,
            onPrimary = tokens.topBarTitle,
            secondary = tokens.accent,
            background = tokens.pageBackground,
            surface = tokens.bottomBarBackground,
            onSurface = tokens.onPage,
        )
    }

    CompositionLocalProvider(LocalShellTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
