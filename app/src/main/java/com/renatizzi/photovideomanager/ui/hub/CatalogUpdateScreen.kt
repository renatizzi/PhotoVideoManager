package com.renatizzi.photovideomanager.ui.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubCardChange
@Composable
fun CatalogUpdateScreen(
    state: CatalogUpdateUiState,
    onRefresh: () -> Unit,
    onLoadPreview: () -> Unit,
    onCancelPreview: () -> Unit,
    onApply: () -> Unit,
    onResolve: (String, ConflictResolution) -> Unit,
    onOpenWizard: () -> Unit,
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onRefresh, enabled = !state.busy) {
                Text(stringResource(R.string.refresh_sources))
            }
            Button(
                onClick = onLoadPreview,
                enabled = !state.busy && state.reachable == true,
            ) {
                Text(stringResource(R.string.hub_update_action))
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancelPreview,
                    enabled = !state.busy,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.importa_annulla))
                }
                val conflictsResolved = preview.conflicts.all {
                    val r = state.resolutions[it.cardId]
                    r == ConflictResolution.KEEP_LOCAL || r == ConflictResolution.TAKE_REMOTE
                }
                Button(
                    onClick = onApply,
                    enabled = !state.busy &&
                        conflictsResolved &&
                        (!preview.isEmpty || preview.conflicts.isNotEmpty()),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.hub_preview_confirm))
                }
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(change.title, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = selected == ConflictResolution.KEEP_LOCAL,
                onClick = { onResolve(change.cardId, ConflictResolution.KEEP_LOCAL) },
                label = { Text(stringResource(R.string.hub_conflict_keep_local)) },
            )
            FilterChip(
                selected = selected == ConflictResolution.TAKE_REMOTE,
                onClick = { onResolve(change.cardId, ConflictResolution.TAKE_REMOTE) },
                label = { Text(stringResource(R.string.hub_conflict_take_remote)) },
            )
        }
    }
}
