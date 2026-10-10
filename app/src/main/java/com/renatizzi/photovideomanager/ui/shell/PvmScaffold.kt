package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.renatizzi.photovideomanager.ui.theme.LocalShellTokens

/**
 * Cornice applicativa globale (equivalente concettuale di BaseActivity.setupAppShell).
 */
@Composable
fun PvmScaffold(
    selectedTab: ShellTab?,
    darkTheme: Boolean,
    userLabel: String,
    snackbarHostState: SnackbarHostState,
    onSelectTab: (ShellTab) -> Unit,
    onToggleTheme: () -> Unit,
    onHelp: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val tokens = LocalShellTokens.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = tokens.pageBackground,
        topBar = {
            PvmTopBar(
                darkTheme = darkTheme,
                userLabel = userLabel,
                onToggleTheme = onToggleTheme,
                onHelp = onHelp,
            )
        },
        bottomBar = {
            PvmBottomBar(
                selected = selectedTab,
                onSelect = onSelectTab,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content,
    )
}
