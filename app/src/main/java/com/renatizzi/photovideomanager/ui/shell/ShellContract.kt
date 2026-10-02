package com.renatizzi.photovideomanager.ui.shell

/**
 * Contratto shared-shell (parity BoxManager).
 * Bottom Bar: Home + 4 macro-aree. Configura via Top Bar.
 */
enum class ShellTab {
    HOME,
    ORGANIZZA,
    COMPONI,
    PUBBLICA,
    GESTISCI,
}

data class ShellTopBarModel(
    val appTitle: String,
    val versionLabel: String,
    val userLabel: String,
    val dateTimeLabel: String,
    val helpEnabled: Boolean = true,
    val darkTheme: Boolean = false,
)

/**
 * Stati UI espliciti previsti dalla Nota §5 — nomi stabili per parity.
 */
enum class UiScreenState {
    INITIAL,
    LOADING,
    EMPTY,
    IN_PROGRESS,
    COMPLETED,
    ERROR,
    SOURCE_UNAVAILABLE,
    NEEDS_AUTHORIZATION,
    CONFIRM_DESTRUCTIVE,
}
