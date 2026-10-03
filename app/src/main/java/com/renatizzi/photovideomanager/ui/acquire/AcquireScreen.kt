package com.renatizzi.photovideomanager.ui.acquire

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun AcquireScreen(
    state: AcquireUiState,
    onToggle: (String) -> Unit,
    onSelectPending: () -> Unit,
    onClearSelection: () -> Unit,
    onAcquire: () -> Unit,
    onRefresh: () -> Unit,
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
            text = stringResource(R.string.acquire_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.acquire_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Text(
            text = stringResource(R.string.catalog_status, state.catalogCount),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onSelectPending,
                enabled = !state.loading && !state.acquiring,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.acquire_select_pending))
            }
            OutlinedButton(
                onClick = onClearSelection,
                enabled = !state.loading && !state.acquiring && state.selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.acquire_clear_selection))
            }
        }

        Button(
            onClick = onAcquire,
            enabled = !state.loading && !state.acquiring && state.selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.acquiring) {
                    stringResource(R.string.acquire_in_progress)
                } else {
                    stringResource(R.string.acquire_selected, state.selectedIds.size)
                },
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.loading && !state.acquiring,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.refresh_sources))
        }

        if (state.loading || state.acquiring) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.acquire_candidates),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (!state.loading && state.candidates.isEmpty()) {
            Text(
                text = stringResource(R.string.acquire_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            if (state.catalogCount > 0) {
                Text(
                    text = stringResource(R.string.acquire_empty_but_catalog, state.catalogCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        state.candidates.forEach { candidate ->
            CandidateRow(
                candidate = candidate,
                selected = candidate.mediaItem.id in state.selectedIds,
                enabled = !state.acquiring && !candidate.alreadyInPersonalArchive,
                onToggle = { onToggle(candidate.mediaItem.id) },
            )
            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.acquire_shared_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun CandidateRow(
    candidate: AcquireCandidate,
    selected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val title = candidate.mediaItem.displayTitle?.ifBlank { null }
        ?: candidate.mediaItem.id
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = selected || candidate.alreadyInPersonalArchive,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
        )
        MediaThumbnail(
            copy = candidate.sourceCopy,
            kind = candidate.mediaItem.kind,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = kindLabel(candidate.mediaItem.kind),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.acquire_from_source, candidate.sourceLocationName),
                style = MaterialTheme.typography.bodySmall,
            )
            if (candidate.alreadyInPersonalArchive) {
                Text(
                    text = stringResource(R.string.acquire_already_personal),
                    style = MaterialTheme.typography.bodySmall,
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
