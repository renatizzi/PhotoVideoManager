package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.ui.theme.LocalShellTokens

/**
 * Bottom Bar globale: Home + Organizza + Componi + Pubblica + Gestisci.
 * CONFIGURA resta fuori (icona Top Bar), come nel disegno di navigazione.
 */
@Composable
fun PvmBottomBar(
    selected: ShellTab?,
    onSelect: (ShellTab) -> Unit,
) {
    val tokens = LocalShellTokens.current
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = tokens.bottomBarActive,
        selectedTextColor = tokens.bottomBarActive,
        unselectedIconColor = tokens.bottomBarInactive,
        unselectedTextColor = tokens.bottomBarInactive,
        indicatorColor = tokens.accent.copy(alpha = 0.18f),
    )
    NavigationBar(containerColor = tokens.bottomBarBackground) {
        ShellTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon(), contentDescription = null) },
                label = {
                    Text(
                        text = stringResource(tab.labelRes()),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = itemColors,
                alwaysShowLabel = true,
            )
        }
    }
}

private fun ShellTab.labelRes(): Int = when (this) {
    ShellTab.HOME -> R.string.home
    ShellTab.ORGANIZZA -> R.string.organizza
    ShellTab.COMPONI -> R.string.componi
    ShellTab.PUBBLICA -> R.string.pubblica
    ShellTab.GESTISCI -> R.string.gestisci
}

private fun ShellTab.icon(): ImageVector = when (this) {
    ShellTab.HOME -> Icons.Outlined.Home
    ShellTab.ORGANIZZA -> Icons.Outlined.FolderOpen
    ShellTab.COMPONI -> Icons.Outlined.Create
    ShellTab.PUBBLICA -> Icons.Outlined.Publish
    ShellTab.GESTISCI -> Icons.Outlined.Tune
}
