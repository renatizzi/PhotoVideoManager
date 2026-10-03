package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.ui.theme.LocalShellTokens

/**
 * Bottom Bar: Home + Organizza + Componi + Pubblica + Gestisci + Impostazioni.
 * Impostazioni: solo icona (stile BoxManager), senza etichetta testuale.
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
            val iconOnly = tab == ShellTab.CONFIGURA
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    if (iconOnly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = stringResource(R.string.configura),
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    } else {
                        Icon(tab.icon(), contentDescription = null)
                    }
                },
                label = if (iconOnly) {
                    null
                } else {
                    {
                        Text(
                            text = stringResource(tab.labelRes()),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                alwaysShowLabel = !iconOnly,
                colors = itemColors,
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
    ShellTab.CONFIGURA -> R.string.configura
}

private fun ShellTab.icon(): ImageVector = when (this) {
    ShellTab.HOME -> Icons.Outlined.Home
    ShellTab.ORGANIZZA -> Icons.Outlined.FolderOpen
    ShellTab.COMPONI -> Icons.Outlined.Create
    ShellTab.PUBBLICA -> Icons.Outlined.Publish
    ShellTab.GESTISCI -> Icons.Outlined.Tune
    ShellTab.CONFIGURA -> Icons.Outlined.Settings
}
