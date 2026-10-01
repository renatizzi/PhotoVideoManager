package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.ui.theme.LocalShellTokens

/**
 * Bottom Bar globale minimale (parity cornice BoxManager, vincolo Nota §5.2):
 * solo Home e Impostazioni/CONFIGURA — nessuna scorciatoia alle macrofunzioni.
 */
@Composable
fun PvmBottomBar(
    selected: ShellTab,
    onSelect: (ShellTab) -> Unit,
) {
    val tokens = LocalShellTokens.current
    NavigationBar(containerColor = tokens.bottomBarBackground) {
        NavigationBarItem(
            selected = selected == ShellTab.HOME,
            onClick = { onSelect(ShellTab.HOME) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = tokens.bottomBarActive,
                selectedTextColor = tokens.bottomBarActive,
                unselectedIconColor = tokens.bottomBarInactive,
                unselectedTextColor = tokens.bottomBarInactive,
                indicatorColor = tokens.accent.copy(alpha = 0.18f),
            ),
        )
        NavigationBarItem(
            selected = selected == ShellTab.SETTINGS,
            onClick = { onSelect(ShellTab.SETTINGS) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = tokens.bottomBarActive,
                selectedTextColor = tokens.bottomBarActive,
                unselectedIconColor = tokens.bottomBarInactive,
                unselectedTextColor = tokens.bottomBarInactive,
                indicatorColor = tokens.accent.copy(alpha = 0.18f),
            ),
        )
    }
}
