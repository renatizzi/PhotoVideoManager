package com.renatizzi.photovideomanager.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renatizzi.photovideomanager.BuildConfig
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.ui.theme.LocalShellTokens
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Top Bar globale — parity slot BoxManager:
 * titolo a sinistra (una riga), versione+utente sotto, azioni a destra.
 */
@Composable
fun PvmTopBar(
    darkTheme: Boolean,
    userLabel: String,
    onOpenConfig: () -> Unit,
    onToggleTheme: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalShellTokens.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalDateTime.now()
        }
    }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val subtitle = "v${BuildConfig.VERSION_NAME} · $userLabel - ${formatter.format(now)}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.topBarBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = tokens.topBarTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = tokens.topBarSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                IconButton(
                    onClick = onOpenConfig,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.configura),
                        tint = tokens.topBarTitle,
                    )
                }
                IconButton(
                    onClick = onHelp,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.help),
                        tint = tokens.topBarTitle,
                    )
                }
                Switch(
                    checked = darkTheme,
                    onCheckedChange = { onToggleTheme() },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = tokens.accent,
                        checkedTrackColor = tokens.accentDark,
                        uncheckedThumbColor = tokens.topBarTitle,
                        uncheckedTrackColor = tokens.topBarSubtitle.copy(alpha = 0.35f),
                    ),
                )
            }
        }
    }
}
