package com.renatizzi.photovideomanager.ui.theme

import android.content.Context

/**
 * Persistenza night mode (parity comportamento BoxManager ThemeManager).
 * null = non impostato dall'utente → si può seguire il sistema.
 */
class ThemePreferences(
    context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun storedDarkMode(): Boolean? =
        when (prefs.getInt(KEY_NIGHT_MODE, NIGHT_UNSET)) {
            NIGHT_YES -> true
            NIGHT_NO -> false
            else -> null
        }

    fun setDarkMode(dark: Boolean) {
        prefs.edit()
            .putInt(KEY_NIGHT_MODE, if (dark) NIGHT_YES else NIGHT_NO)
            .apply()
    }

    companion object {
        private const val PREFS = "pvm_theme"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val NIGHT_UNSET = -1
        private const val NIGHT_NO = 0
        private const val NIGHT_YES = 1
    }
}
