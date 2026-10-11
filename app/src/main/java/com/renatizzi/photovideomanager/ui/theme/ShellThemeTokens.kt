package com.renatizzi.photovideomanager.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Token shell ispirati a BoxManager ThemeManager (struttura),
 * con branding Photo&VideoManager (palette blue di prodotto).
 */
data class ShellThemeTokens(
    val topBarBackground: Color,
    val topBarTitle: Color,
    val topBarSubtitle: Color,
    val bottomBarBackground: Color,
    val bottomBarActive: Color,
    val bottomBarInactive: Color,
    val accent: Color,
    val accentDark: Color,
    val pageBackground: Color,
    val onPage: Color,
)

object ShellPalettes {
    val PvmBlueLight = ShellThemeTokens(
        topBarBackground = Color(0xFF1565C0),
        topBarTitle = Color.White,
        topBarSubtitle = Color(0xFFE3F2FD),
        bottomBarBackground = Color.White,
        bottomBarActive = Color(0xFF0D47A1),
        bottomBarInactive = Color(0xFF607D8B),
        accent = Color(0xFF42A5F5),
        accentDark = Color(0xFF0D47A1),
        pageBackground = Color(0xFFF4F7F9),
        onPage = Color(0xFF1A2330),
    )

    val PvmBlueDark = ShellThemeTokens(
        topBarBackground = Color(0xFF0D47A1),
        topBarTitle = Color.White,
        topBarSubtitle = Color(0xFFBBDEFB),
        bottomBarBackground = Color(0xFF102027),
        bottomBarActive = Color(0xFF64B5F6),
        bottomBarInactive = Color(0xFF90A4AE),
        accent = Color(0xFF64B5F6),
        accentDark = Color(0xFF42A5F5),
        pageBackground = Color(0xFF0F1720),
        onPage = Color(0xFFE8EEF4),
    )

    fun tokens(darkTheme: Boolean): ShellThemeTokens =
        if (darkTheme) PvmBlueDark else PvmBlueLight
}
