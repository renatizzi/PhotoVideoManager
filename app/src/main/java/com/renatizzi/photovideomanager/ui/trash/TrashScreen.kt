package com.renatizzi.photovideomanager.ui.trash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import com.renatizzi.photovideomanager.ui.census.SourceSelectionBox
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun TrashScreen(
    state: TrashUiState,
    onRefresh: () -> Unit,
    onToggle: (String) -> Unit,
    onRestoreSelected: () -> Unit,
    onRequestEmpty: () -> Unit,
    onConfirmEmpty: () -> Unit,
    onDismissEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.trash_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (state.entries.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.trash_restore_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )
                Button(
                    onClick = onRestoreSelected,
                    enabled = !state.loading && !state.busy && state.selectedCount > 0,
                ) {
                    Text(stringResource(R.string.trash_restore))
                }
            }
        } else if (!state.loading) {
            Text(
                text = stringResource(R.string.trash_empty_list),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onRefresh,
                enabled = !state.loading && !state.busy,
            ) {
                Text(stringResource(R.string.refresh_sources))
            }
            OutlinedButton(
                onClick = onRequestEmpty,
                enabled = !state.loading && !state.busy && state.entries.isNotEmpty(),
            ) {
                Text(stringResource(R.string.trash_empty))
            }
        }

        if (state.loading || state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.entries.forEach { entry ->
            HorizontalDivider()
            TrashRow(
                entry = entry,
                checked = entry.mediaItem.id in state.selectedIds,
                enabled = !state.busy,
                onToggle = { onToggle(entry.mediaItem.id) },
            )
        }
        if (state.entries.isNotEmpty()) HorizontalDivider()
    }

    if (state.pendingPurgeAll) {
        AlertDialog(
            onDismissRequest = onDismissEmpty,
            title = { Text(stringResource(R.string.trash_empty_confirm_title)) },
            text = { Text(stringResource(R.string.trash_empty_confirm_body)) },
            confirmButton = {
                TextButton(onClick = onConfirmEmpty) {
                    Text(stringResource(R.string.clean_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEmpty) {
                    Text(stringResource(R.string.clean_confirm_no))
                }
            },
        )
    }
}

@Composable
private fun TrashRow(
    entry: TrashEntry,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val title = entry.mediaItem.displayTitle?.ifBlank { null } ?: entry.mediaItem.id
    val device = entry.locationName.takeUnless {
        it.equals("Catalogo", ignoreCase = true) || it.equals("Spazio app", ignoreCase = true)
    }.orEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SourceSelectionBox(
            selection = if (checked) {
                SourceCensusSelection.SELECTED
            } else {
                SourceCensusSelection.NOT_SELECTED
            },
            enabled = false,
            onClick = {},
        )
        MediaThumbnail(
            copy = entry.mediaCopy,
            kind = entry.mediaItem.kind,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            val meta = CatalogListRowFormat.metaLine(
                deviceLabel = device,
                pathLabel = "",
                statusLabel = CatalogListRowFormat.availabilityLabel(Availability.AVAILABLE),
                secondary = false,
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
