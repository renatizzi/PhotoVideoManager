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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.application.LiveMediaPreview
import com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatRowFormatter
import com.renatizzi.photovideomanager.domain.model.ArchiveEntry
import com.renatizzi.photovideomanager.domain.model.MediaBrowseSortMode
import android.net.Uri
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail
import com.renatizzi.photovideomanager.ui.common.MediaThumbnailModel

@Composable
fun SourceBrowseScreen(
    state: SourceBrowseUiState,
    onSortMode: (MediaBrowseSortMode) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val itemCount = state.itemCount
    val pathDisplay = CatalogSeatRowFormatter.displayPath(
        state.pathLabel.ifBlank { state.displayName }.ifBlank { "—" },
    )
    val deviceLine = when {
        state.deviceLabel.isBlank() -> null
        state.isSecondaryVolume ->
            stringResource(R.string.browse_source_on_device_sd, state.deviceLabel)
        else -> stringResource(R.string.browse_source_on_device, state.deviceLabel)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack) {
            Text(stringResource(R.string.browse_source_back))
        }
        Text(
            text = stringResource(R.string.browse_source_title, pathDisplay),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (deviceLine != null) {
            Text(
                text = deviceLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
        }

        if (itemCount > 0) {
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
        }

        if (state.loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (!state.loading && itemCount > 0) {
            Text(
                text = stringResource(R.string.browse_source_count, itemCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            if (state.showingLivePreview) {
                Text(
                    text = stringResource(R.string.browse_source_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }

        if (!state.loading && itemCount == 0) {
            Text(stringResource(R.string.browse_source_empty))
        }

        if (state.entries.isNotEmpty()) {
            state.visibleEntries.forEach { entry ->
                BrowseRow(
                    entry = entry,
                    deviceLabel = state.deviceLabel,
                    pathLabel = state.pathLabel.ifBlank { state.displayName },
                    secondary = state.isSecondaryVolume,
                )
                HorizontalDivider()
            }
        } else {
            state.visibleLivePreviews.forEach { preview ->
                LiveBrowseRow(
                    preview = preview,
                    deviceLabel = state.deviceLabel,
                    pathLabel = preview.folderPathLabel.ifBlank {
                        state.pathLabel.ifBlank { state.displayName }
                    },
                    secondary = state.isSecondaryVolume,
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun BrowseRow(
    entry: ArchiveEntry,
    deviceLabel: String,
    pathLabel: String,
    secondary: Boolean,
) {
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            // LOCK #6: path cartella uniforme (come Catalogo), non solo «Tipo:».
            val meta = CatalogListRowFormat.metaLine(
                deviceLabel = deviceLabel,
                pathLabel = pathLabel,
                statusLabel = CatalogListRowFormat.availabilityLabel(
                    com.renatizzi.photovideomanager.domain.model.Availability.AVAILABLE,
                ),
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

@Composable
private fun LiveBrowseRow(
    preview: LiveMediaPreview,
    deviceLabel: String,
    pathLabel: String,
    secondary: Boolean,
) {
    val model = runCatching { Uri.parse(preview.opaqueLocator) }.getOrNull()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaThumbnailModel(model = model, kind = preview.kind)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = preview.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            val meta = CatalogListRowFormat.metaLine(
                deviceLabel = deviceLabel,
                pathLabel = pathLabel,
                statusLabel = CatalogListRowFormat.availabilityLabel(
                    com.renatizzi.photovideomanager.domain.model.Availability.AVAILABLE,
                ),
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
