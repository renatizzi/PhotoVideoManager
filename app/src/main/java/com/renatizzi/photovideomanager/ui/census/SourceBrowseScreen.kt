package com.renatizzi.photovideomanager.ui.census

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.ArchiveEntry
import com.renatizzi.photovideomanager.domain.model.MediaBrowseSortMode
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun SourceBrowseScreen(
    state: SourceBrowseUiState,
    onSortMode: (MediaBrowseSortMode) -> Unit,
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
            text = stringResource(R.string.browse_source_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        // Stesso blocco identità della riga in Acquisisci (cartella · dispositivo · percorso).
        if (state.displayName.isNotBlank()) {
            Text(state.displayName, fontWeight = FontWeight.SemiBold)
        }
        if (state.deviceLabel.isNotBlank()) {
            Text(state.deviceLabel, style = MaterialTheme.typography.labelMedium)
        }
        if (state.pathLabel.isNotBlank()) {
            Text(state.pathLabel, style = MaterialTheme.typography.bodyMedium)
        }

        if (state.childFolders.isNotEmpty()) {
            Text(
                text = stringResource(R.string.browse_source_folders_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = state.childFolders.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.browse_source_folders_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.sortMode == MediaBrowseSortMode.NEWEST,
                onClick = { onSortMode(MediaBrowseSortMode.NEWEST) },
                label = { Text(stringResource(R.string.sort_newest)) },
            )
            FilterChip(
                selected = state.sortMode == MediaBrowseSortMode.NAME,
                onClick = { onSortMode(MediaBrowseSortMode.NAME) },
                label = { Text(stringResource(R.string.sort_name)) },
            )
            FilterChip(
                selected = state.sortMode == MediaBrowseSortMode.KIND,
                onClick = { onSortMode(MediaBrowseSortMode.KIND) },
                label = { Text(stringResource(R.string.sort_kind)) },
            )
        }

        if (state.loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        Text(
            text = stringResource(R.string.browse_source_count, state.visibleEntries.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (!state.loading && state.visibleEntries.isEmpty()) {
            Text(stringResource(R.string.browse_source_empty))
        }

        state.visibleEntries.forEach { entry ->
            BrowseRow(entry)
            HorizontalDivider()
        }
    }
}

@Composable
private fun BrowseRow(entry: ArchiveEntry) {
    val title = entry.mediaItem.displayTitle?.ifBlank { null } ?: entry.mediaItem.id
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaThumbnail(copy = entry.mediaCopy, kind = entry.mediaItem.kind)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = when (entry.mediaItem.kind) {
                    MediaKind.PHOTO -> stringResource(R.string.media_kind_photo)
                    MediaKind.VIDEO -> stringResource(R.string.media_kind_video)
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
