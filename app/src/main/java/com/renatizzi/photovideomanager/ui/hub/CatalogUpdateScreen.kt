package com.renatizzi.photovideomanager.ui.hub

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.data.storage.OpenDocumentTreeAdvanced
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubCard
import com.renatizzi.photovideomanager.domain.hub.HubCardChange
import com.renatizzi.photovideomanager.domain.hub.HubSourceDescriptor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CatalogUpdateScreen(
    state: CatalogUpdateUiState,
    onRefresh: () -> Unit,
    onLoadPreview: () -> Unit,
    onCancelPreview: () -> Unit,
    onApply: () -> Unit,
    onReceiveOnly: () -> Unit,
    onResolve: (String, ConflictResolution) -> Unit,
    onRestoreBackup: () -> Unit,
    onLinkSource: (sourceKey: String, treeUri: String, displayName: String) -> Unit,
    onOpenWizard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingSourceKey by remember { mutableStateOf<String?>(null) }
    val openTree = rememberLauncherForActivityResult(
        contract = OpenDocumentTreeAdvanced(),
    ) { uri: Uri? ->
        val key = pendingSourceKey
        pendingSourceKey = null
        if (uri != null && key != null) {
            val label = SafPathLabels.folderTitle(
                context,
                uri,
                context.getString(R.string.hub_seat_folder_fallback),
            )
            onLinkSource(key, uri.toString(), label)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.hub_update_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (!state.seat.isConfigured) {
            Text(stringResource(R.string.hub_update_no_seat))
            Button(onClick = onOpenWizard) {
                Text(stringResource(R.string.hub_wizard_entry))
            }
            return@Column
        }
        Text(
            stringResource(
                R.string.hub_update_seat_line,
                state.seat.displayLabel.ifBlank { state.seat.kind.name },
            ),
        )
        when (state.reachable) {
            false -> Text(
                stringResource(R.string.hub_update_unreachable),
                color = MaterialTheme.colorScheme.error,
            )
            true -> Text(stringResource(R.string.hub_update_reachable))
            null -> Text(stringResource(R.string.hub_update_checking))
        }
        if (state.unverified) {
            Text(
                stringResource(R.string.hub_unverified),
                color = MaterialTheme.colorScheme.error,
            )
            Button(
                onClick = onRestoreBackup,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.hub_restore_backup))
            }
        }
        OutlinedButton(onClick = onRefresh, enabled = !state.busy) {
            Text(stringResource(R.string.refresh_sources))
        }
        Button(
            onClick = onLoadPreview,
            enabled = !state.busy && state.reachable == true && !state.unverified,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.hub_update_action))
        }

        HorizontalDivider()
        Text(stringResource(R.string.hub_sources_title), fontWeight = FontWeight.Medium)
        Text(stringResource(R.string.hub_sources_hint), style = MaterialTheme.typography.bodySmall)
        if (state.sources.isEmpty()) {
            Text(stringResource(R.string.hub_sources_absent))
        } else {
            state.sources.forEach { source ->
                SourceRow(
                    source = source,
                    linked = source.sourceKey in state.linkedKeys,
                    enabled = !state.busy,
                    onLink = {
                        pendingSourceKey = source.sourceKey
                        openTree.launch(null)
                    },
                )
            }
        }

        state.preview?.let { preview ->
            HorizontalDivider()
            Text(stringResource(R.string.hub_preview_title), fontWeight = FontWeight.Medium)
            PreviewSection(
                title = stringResource(R.string.hub_preview_publish, preview.toPublish.size),
                items = preview.toPublish,
            )
            PreviewSection(
                title = stringResource(R.string.hub_preview_receive, preview.toReceive.size),
                items = preview.toReceive,
            )
            if (preview.conflicts.isNotEmpty()) {
                Text(
                    stringResource(R.string.hub_preview_conflicts, preview.conflicts.size),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(stringResource(R.string.hub_preview_held), style = MaterialTheme.typography.bodySmall)
                preview.conflicts.forEach { change ->
                    ConflictRow(
                        change = change,
                        selected = state.resolutions[change.cardId],
                        onResolve = onResolve,
                    )
                }
            }
            if (preview.isEmpty) {
                Text(stringResource(R.string.hub_preview_empty))
            }
            val hasReceive = preview.toReceive.isNotEmpty() || preview.conflicts.any {
                state.resolutions[it.cardId] == ConflictResolution.TAKE_REMOTE
            }
            val hasWork = preview.toPublish.isNotEmpty() || hasReceive || preview.conflicts.any {
                state.resolutions[it.cardId] == ConflictResolution.KEEP_LOCAL
            }
            if (preview.toPublish.isNotEmpty()) {
                OutlinedButton(
                    onClick = onReceiveOnly,
                    enabled = !state.busy && !state.unverified && hasReceive,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.hub_preview_receive_only))
                }
            }
            OutlinedButton(
                onClick = onCancelPreview,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.importa_annulla))
            }
            Button(
                onClick = onApply,
                enabled = !state.busy && !state.unverified && hasWork,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.hub_preview_confirm))
            }
        }

        state.successMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        if (state.busy) {
            CircularProgressIndicator()
        }
        OutlinedButton(onClick = onOpenWizard, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.hub_wizard_entry))
        }
    }
}

@Composable
private fun SourceRow(
    source: HubSourceDescriptor,
    linked: Boolean,
    enabled: Boolean,
    onLink: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(source.displayName.ifBlank { source.sourceKey }, fontWeight = FontWeight.Medium)
        Text(
            listOf(source.provider, source.humanPath).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
        )
        when {
            !source.shareable -> Text(
                stringResource(R.string.hub_sources_not_shareable),
                style = MaterialTheme.typography.bodySmall,
            )
            linked -> Text(
                stringResource(R.string.hub_sources_linked),
                style = MaterialTheme.typography.bodySmall,
            )
            else -> OutlinedButton(onClick = onLink, enabled = enabled) {
                Text(stringResource(R.string.hub_sources_link))
            }
        }
    }
}

@Composable
private fun PreviewSection(title: String, items: List<HubCardChange>) {
    Text(title, fontWeight = FontWeight.Medium)
    if (items.isEmpty()) {
        Text("—", style = MaterialTheme.typography.bodySmall)
    } else {
        items.take(30).forEach {
            Text("• ${it.title}", style = MaterialTheme.typography.bodySmall)
        }
        if (items.size > 30) {
            Text("… +${items.size - 30}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ConflictRow(
    change: HubCardChange,
    selected: ConflictResolution?,
    onResolve: (String, ConflictResolution) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        Text(change.title, fontWeight = FontWeight.Medium)
        Text(stringResource(R.string.hub_conflict_whole), style = MaterialTheme.typography.bodySmall)
        CardSide(stringResource(R.string.hub_conflict_local_heading), change.local)
        CardSide(stringResource(R.string.hub_conflict_remote_heading), change.remote)
        OutlinedButton(
            onClick = { onResolve(change.cardId, ConflictResolution.KEEP_LOCAL) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (selected == ConflictResolution.KEEP_LOCAL) {
                    "✓ ${stringResource(R.string.hub_conflict_keep_local)}"
                } else {
                    stringResource(R.string.hub_conflict_keep_local)
                },
            )
        }
        OutlinedButton(
            onClick = { onResolve(change.cardId, ConflictResolution.TAKE_REMOTE) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (selected == ConflictResolution.TAKE_REMOTE) {
                    "✓ ${stringResource(R.string.hub_conflict_take_remote)}"
                } else {
                    stringResource(R.string.hub_conflict_take_remote)
                },
            )
        }
    }
}

@Composable
private fun CardSide(heading: String, card: HubCard?) {
    Text(heading, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    if (card == null) {
        Text(stringResource(R.string.hub_conflict_missing_side), style = MaterialTheme.typography.bodySmall)
        return
    }
    Text(card.displayTitle ?: card.id, style = MaterialTheme.typography.bodySmall)
    Text(
        if (card.trashed) {
            stringResource(R.string.hub_conflict_trashed_yes)
        } else {
            stringResource(R.string.hub_conflict_trashed_no)
        },
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        stringResource(
            R.string.hub_conflict_meta,
            formatEpoch(card.updatedAtEpochMs),
            card.updatedByDeviceId,
        ),
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun formatEpoch(epochMs: Long): String {
    if (epochMs <= 0L) return "—"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(epochMs))
}
