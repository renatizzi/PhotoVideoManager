package com.renatizzi.photovideomanager.ui.clean

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.ui.census.SourceSelectionBox
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun CleanScreen(
    state: CleanUiState,
    onBack: () -> Unit = {},
    onRequestTrashPending: () -> Unit,
    onConfirmTrashPending: (deleteSourceFiles: Boolean) -> Unit,
    onDismissTrashPending: () -> Unit,
    onToggleScrap: (String) -> Unit,
    onToggleDuplicate: (String) -> Unit,
    onDuplicateAgeFilter: (DuplicateAgeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.clean_back))
        }

        Text(
            text = stringResource(R.string.clean_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.clean_subtitle),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )
        Text(
            text = stringResource(R.string.clean_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        if (state.selectedCount > 0) {
            Text(
                text = stringResource(R.string.clean_section_selected),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.clean_selected_summary, state.selectedCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.clean_section_scraps, state.scraps.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.clean_scraps_manual_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
        if (state.scrapsLoaded && state.scraps.isEmpty()) {
            Text(
                text = stringResource(R.string.clean_scraps_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        } else {
            state.scraps.forEach { scrap ->
                CatalogSelectableRow(
                    title = scrap.mediaItem.displayTitle?.ifBlank { null }
                        ?: scrap.mediaItem.id,
                    deviceLabel = scrap.deviceLabel,
                    pathLabel = scrap.pathLabel,
                    statusLabel = scrap.reason,
                    secondary = scrap.isSecondaryVolume,
                    selection = if (scrap.mediaItem.id in state.scrapSelectedIds) {
                        SourceCensusSelection.SELECTED
                    } else {
                        SourceCensusSelection.NOT_SELECTED
                    },
                    previewCopy = scrap.previewCopy,
                    kind = scrap.mediaItem.kind,
                    enabled = !state.busy && !state.analyzing,
                    onToggle = { onToggleScrap(scrap.mediaItem.id) },
                )
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.clean_section_duplicates, state.groups.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.duplicateAgeFilter == DuplicateAgeFilter.OLDER,
                onClick = { onDuplicateAgeFilter(DuplicateAgeFilter.OLDER) },
                enabled = !state.busy && !state.analyzing,
                label = { Text(stringResource(R.string.clean_dup_filter_older)) },
            )
            FilterChip(
                selected = state.duplicateAgeFilter == DuplicateAgeFilter.NEWER,
                onClick = { onDuplicateAgeFilter(DuplicateAgeFilter.NEWER) },
                enabled = !state.busy && !state.analyzing,
                label = { Text(stringResource(R.string.clean_dup_filter_newer)) },
            )
        }
        Text(
            text = stringResource(R.string.clean_dup_filter_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )

        if (state.analyzing || state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (state.hasAnalyzed && state.groups.isEmpty() && !state.analyzing) {
            Text(
                text = stringResource(R.string.clean_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }

        state.groups.forEach { group ->
            group.members
                .distinctBy { it.mediaItem.id }
                .forEach { member ->
                    CatalogSelectableRow(
                        title = member.mediaItem.displayTitle?.ifBlank { null }
                            ?: member.mediaItem.id,
                        deviceLabel = member.deviceLabel.ifBlank { member.locationName },
                        pathLabel = member.pathLabel,
                        statusLabel = if (member.isSuggestedKeep) {
                            stringResource(R.string.clean_suggested_keep)
                        } else {
                            "Online"
                        },
                        secondary = member.isSecondaryVolume,
                        selection = if (member.mediaItem.id in state.duplicateSelectedIds) {
                            SourceCensusSelection.SELECTED
                        } else {
                            SourceCensusSelection.NOT_SELECTED
                        },
                        previewCopy = member.mediaCopy,
                        kind = member.mediaItem.kind,
                        enabled = !state.busy && !state.analyzing,
                        onToggle = { onToggleDuplicate(member.mediaItem.id) },
                    )
                }
            HorizontalDivider()
        }

        Button(
            onClick = onRequestTrashPending,
            enabled = state.totalPendingTrashCount > 0 && !state.analyzing && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clean_move_to_trash))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.pendingTrash) {
        var deleteSource by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissTrashPending,
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(
                            R.string.clean_confirm_combined_body,
                            state.totalPendingTrashCount,
                        ),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deleteSource,
                            onCheckedChange = { deleteSource = it },
                        )
                        Text(stringResource(R.string.trash_also_delete_source))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirmTrashPending(deleteSource) }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrashPending) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }
}

@Composable
private fun CatalogSelectableRow(
    title: String,
    deviceLabel: String,
    pathLabel: String,
    statusLabel: String,
    secondary: Boolean,
    selection: SourceCensusSelection,
    previewCopy: com.renatizzi.photovideomanager.domain.model.MediaCopy?,
    kind: com.renatizzi.photovideomanager.domain.model.MediaKind,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SourceSelectionBox(
            selection = selection,
            enabled = false,
            onClick = {},
        )
        if (previewCopy != null) {
            MediaThumbnail(copy = previewCopy, kind = kind, size = 48.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            val meta = CatalogListRowFormat.metaLine(
                deviceLabel = deviceLabel,
                pathLabel = pathLabel,
                statusLabel = statusLabel,
                secondary = secondary,
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
