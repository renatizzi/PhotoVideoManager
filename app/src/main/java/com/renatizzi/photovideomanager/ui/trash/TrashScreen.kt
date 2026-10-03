package com.renatizzi.photovideomanager.ui.trash

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun TrashScreen(
    state: TrashUiState,
    onRefresh: () -> Unit,
    onRestore: (String) -> Unit,
    onPurge: (String) -> Unit,
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
        Text(
            text = stringResource(R.string.trash_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.trash_inline_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.loading && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.refresh_sources))
        }
        Button(
            onClick = onRequestEmpty,
            enabled = !state.loading && !state.busy && state.entries.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.trash_empty))
        }

        if (state.loading || state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        HorizontalDivider()
        Text(
            text = stringResource(R.string.trash_list_title, state.entries.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (!state.loading && state.entries.isEmpty()) {
            Text(stringResource(R.string.trash_empty_list))
        }

        state.entries.forEach { entry ->
            TrashRow(
                entry = entry,
                enabled = !state.busy,
                onRestore = { onRestore(entry.mediaCopy.id) },
                onPurge = { onPurge(entry.mediaCopy.id) },
            )
            HorizontalDivider()
        }
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
    enabled: Boolean,
    onRestore: () -> Unit,
    onPurge: () -> Unit,
) {
    val title = entry.mediaItem.displayTitle?.ifBlank { null } ?: entry.mediaItem.id
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaThumbnail(
                copy = entry.mediaCopy,
                kind = entry.mediaItem.kind,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.clean_member_location, entry.locationName),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (entry.isPersonalArchive) {
                    Text(
                        stringResource(R.string.trash_personal_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onRestore, enabled = enabled) {
                Text(stringResource(R.string.trash_restore))
            }
            Button(onClick = onPurge, enabled = enabled) {
                Text(stringResource(R.string.trash_purge))
            }
        }
    }
}
