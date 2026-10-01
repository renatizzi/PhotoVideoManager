package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.renatizzi.photovideomanager.R

enum class BottomTab { Home, Settings }

/**
 * Bottom Bar globale minimale: solo Home e Impostazioni/CONFIGURA.
 * Nessuna scorciatoia alle macrofunzioni (vincolo Nota §5.2).
 */
@Composable
fun PvmBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == BottomTab.Home,
            onClick = { onSelect(BottomTab.Home) },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.home)) },
        )
        NavigationBarItem(
            selected = selected == BottomTab.Settings,
            onClick = { onSelect(BottomTab.Settings) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.settings)) },
        )
    }
}
