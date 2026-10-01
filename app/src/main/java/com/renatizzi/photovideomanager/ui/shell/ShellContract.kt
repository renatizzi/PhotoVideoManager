package com.renatizzi.photovideomanager.ui.shell

/**
 * Contratto shared-shell (parity BoxManager, vincoli Nota PVM).
 * I slot Top Bar restano fissi; la Bottom Bar PVM ha solo HOME/SETTINGS.
 */
enum class ShellTab {
    HOME,
    SETTINGS,
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
