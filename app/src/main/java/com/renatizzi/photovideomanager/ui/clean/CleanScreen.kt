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
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.DuplicateMember
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.ScrapCandidate
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun CleanScreen(
    state: CleanUiState,
    onAnalyze: () -> Unit,
    onRequestTrashSelected: () -> Unit,
    onConfirmTrashSelected: (deleteSourceFiles: Boolean) -> Unit,
    onDismissTrashSelected: () -> Unit,
    onToggleScrap: (String) -> Unit,
    onSelectAllScraps: (Boolean) -> Unit,
    onRequestTrashScraps: () -> Unit,
    onConfirmTrashScraps: (deleteSourceFiles: Boolean) -> Unit,
    onDismissTrashScraps: () -> Unit,
    onRequestTrash: (DuplicateGroup) -> Unit,
    onConfirmTrash: (deleteSourceFiles: Boolean) -> Unit,
    onDismissTrash: () -> Unit,
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
            text = stringResource(R.string.clean_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        // —— Funzione 1: eliminazione elementi selezionati (✓ da Aggiorna) ——
        Text(
            text = stringResource(R.string.clean_section_selected),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = if (state.selectedCount > 0) {
                stringResource(R.string.clean_selected_summary, state.selectedCount)
            } else {
                stringResource(R.string.clean_selected_empty_hint)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Button(
            onClick = onRequestTrashSelected,
            enabled = state.selectedCount > 0 && !state.analyzing && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clean_trash_selected))
        }

        HorizontalDivider()

        // —— Funzione 2: possibili scarti (BL-01) ——
        Text(
            text = stringResource(R.string.clean_section_scraps),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.clean_scraps_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        if (state.scrapsLoaded && state.scraps.isEmpty()) {
            Text(
                text = stringResource(R.string.clean_scraps_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        } else if (state.scraps.isNotEmpty()) {
            val allScrapsOn = state.scrapSelectedCount == state.scraps.size
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    enabled = !state.busy && !state.analyzing,
                    onClick = { onSelectAllScraps(!allScrapsOn) },
                ),
            ) {
                Checkbox(
                    checked = allScrapsOn,
                    onCheckedChange = { onSelectAllScraps(it) },
                    enabled = !state.busy && !state.analyzing,
                )
                Text(stringResource(R.string.acquisisci_tutti))
            }
            state.scraps.forEach { scrap ->
                ScrapRow(
                    scrap = scrap,
                    checked = scrap.mediaItem.id in state.scrapSelectedIds,
                    enabled = !state.busy && !state.analyzing,
                    onToggle = { onToggleScrap(scrap.mediaItem.id) },
                )
            }
            Button(
                onClick = onRequestTrashScraps,
                enabled = state.scrapSelectedCount > 0 && !state.busy && !state.analyzing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.clean_trash_scraps))
            }
        }

        HorizontalDivider()

        // —— Funzione 3: duplicati ——
        Text(
            text = stringResource(R.string.clean_section_duplicates),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.clean_subtitle_duplicates),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Button(
            onClick = onAnalyze,
            enabled = !state.analyzing && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.analyzing) {
                    stringResource(R.string.clean_analyzing)
                } else {
                    stringResource(R.string.clean_analyze)
                },
            )
        }

        if (state.analyzing || state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (state.hasAnalyzed && !state.analyzing) {
            Text(
                text = stringResource(
                    R.string.clean_stats,
                    state.groups.size,
                    state.copiesScanned,
                    state.hashesComputed,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.clean_groups_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (state.hasAnalyzed && state.groups.isEmpty() && !state.analyzing) {
            Text(
                text = stringResource(R.string.clean_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }

        state.groups.forEachIndexed { index, group ->
            DuplicateGroupBlock(
                index = index + 1,
                group = group,
                enabled = !state.busy && !state.analyzing,
                onTrashExtras = { onRequestTrash(group) },
            )
            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (state.pendingTrashSelected) {
        var deleteSource by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissTrashSelected,
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(
                            R.string.clean_confirm_selected_body,
                            state.selectedCount,
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
                TextButton(onClick = { onConfirmTrashSelected(deleteSource) }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrashSelected) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }

    if (state.pendingTrashScraps) {
        var deleteSource by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissTrashScraps,
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(
                            R.string.clean_confirm_scraps_body,
                            state.scrapSelectedCount,
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
                TextButton(onClick = { onConfirmTrashScraps(deleteSource) }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrashScraps) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }

    val pending = state.pendingTrashGroup
    if (pending != null) {
        var deleteSource by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = onDismissTrash,
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(
                            R.string.clean_confirm_body,
                            pending.extraItemCount,
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
                TextButton(onClick = { onConfirmTrash(deleteSource) }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrash) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }
}

@Composable
private fun ScrapRow(
    scrap: ScrapCandidate,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val title = scrap.mediaItem.displayTitle?.ifBlank { null }
        ?: scrap.mediaItem.id
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
        )
        MediaThumbnail(
            copy = scrap.previewCopy,
            kind = scrap.mediaItem.kind,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = kindLabel(scrap.mediaItem.kind),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = scrap.reason,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun DuplicateGroupBlock(
    index: Int,
    group: DuplicateGroup,
    enabled: Boolean,
    onTrashExtras: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(
                R.string.clean_group_header,
                index,
                group.distinctItemCount,
                group.extraItemCount,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.clean_group_hash,
                group.fingerprintValue.take(12),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        group.members.forEach { member ->
            MemberRow(member)
        }
        OutlinedButton(
            onClick = onTrashExtras,
            enabled = enabled && group.extraItemCount > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clean_trash_extras))
        }
    }
}

@Composable
private fun MemberRow(member: DuplicateMember) {
    val title = member.mediaItem.displayTitle?.ifBlank { null }
        ?: member.mediaItem.id
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaThumbnail(
            copy = member.mediaCopy,
            kind = member.mediaItem.kind,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = kindLabel(member.mediaItem.kind),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.clean_member_location, member.locationName),
                style = MaterialTheme.typography.bodySmall,
            )
            if (member.isSuggestedKeep) {
                Text(
                    text = stringResource(R.string.clean_suggested_keep),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun kindLabel(kind: MediaKind): String = when (kind) {
    MediaKind.PHOTO -> stringResource(R.string.media_kind_photo)
    MediaKind.VIDEO -> stringResource(R.string.media_kind_video)
}
