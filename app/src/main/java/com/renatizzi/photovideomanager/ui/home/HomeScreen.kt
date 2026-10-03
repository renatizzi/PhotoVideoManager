package com.renatizzi.photovideomanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.ui.shell.ShellTab
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    snapshot: DashboardSnapshot?,
    onOpenTab: (ShellTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        if (snapshot != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KpiCard(
                    title = stringResource(R.string.kpi_photos),
                    value = snapshot.photoCount.toString(),
                    detail = duplicateLabel(snapshot.duplicatePhotoCount),
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title = stringResource(R.string.kpi_videos),
                    value = snapshot.videoCount.toString(),
                    detail = duplicateLabel(snapshot.duplicateVideoCount),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KpiCard(
                    title = stringResource(R.string.kpi_space),
                    value = formatBytes(snapshot.personalUsedBytes),
                    detail = spaceAvailabilityLabel(snapshot.localAvailability),
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title = stringResource(R.string.kpi_last_update),
                    value = formatLastUpdate(snapshot.lastUpdatedEpochMs),
                    detail = stringResource(R.string.kpi_trash, snapshot.trashCount),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Text(
            text = stringResource(R.string.quick_access),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickAccessButton(
                title = stringResource(R.string.organizza),
                color = MacroNavigation.organizaAccent,
                onClick = { onOpenTab(ShellTab.ORGANIZZA) },
                modifier = Modifier.weight(1f),
            )
            QuickAccessButton(
                title = stringResource(R.string.componi),
                color = MacroNavigation.componiAccent,
                onClick = { onOpenTab(ShellTab.COMPONI) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickAccessButton(
                title = stringResource(R.string.pubblica),
                color = MacroNavigation.pubblicaAccent,
                onClick = { onOpenTab(ShellTab.PUBBLICA) },
                modifier = Modifier.weight(1f),
            )
            QuickAccessButton(
                title = stringResource(R.string.gestisci),
                color = MacroNavigation.gestisciAccent,
                onClick = { onOpenTab(ShellTab.GESTISCI) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.dashboard_inline_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .background(color, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun duplicateLabel(count: Long?): String =
    if (count == null) {
        stringResource(R.string.kpi_duplicates_pending)
    } else {
        stringResource(R.string.kpi_duplicates, count)
    }

@Composable
private fun spaceAvailabilityLabel(availability: Availability): String = when (availability) {
    Availability.AVAILABLE -> stringResource(R.string.kpi_space_personal)
    Availability.UNAVAILABLE -> stringResource(R.string.storage_local_unavailable)
    Availability.UNKNOWN -> stringResource(R.string.storage_local_unknown)
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}

private fun formatLastUpdate(epochMs: Long?): String {
    if (epochMs == null || epochMs <= 0L) return "—"
    val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
