package com.renatizzi.photovideomanager.ui.census

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSortMode
import com.renatizzi.photovideomanager.domain.model.SourceSummary

@Composable
fun CensusSourcesScreen(
    state: CensusUiState,
    onAddSource: (Uri, String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onRefreshStatus: () -> Unit,
    onCensusSelected: () -> Unit,
    onCensusOne: (String) -> Unit,
    onRemove: (String) -> Unit,
    onBrowse: (String) -> Unit,
    onSortMode: (SourceSortMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openTree = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment
                ?.substringAfterLast(':')
                ?.substringAfterLast('/')
                ?: context.getString(R.string.external_source_default_name)
            onAddSource(uri, name)
        }
    }
    val busy = state.loading || state.censusBusyLocationIds.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.census_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.census_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.census_inline_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        Text(
            text = stringResource(R.string.catalog_status, state.catalogCount),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { openTree.launch(null) }, enabled = !busy) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_source))
                }
                IconButton(onClick = onRefreshStatus, enabled = !busy) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
                }
                IconButton(onClick = onCensusSelected, enabled = !busy) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.census_selected))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = state.sortMode == SourceSortMode.NAME,
                    onClick = { onSortMode(SourceSortMode.NAME) },
                    label = { Text(stringResource(R.string.sort_name)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.SortByAlpha, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                )
                FilterChip(
                    selected = state.sortMode == SourceSortMode.DEVICE,
                    onClick = { onSortMode(SourceSortMode.DEVICE) },
                    label = { Text(stringResource(R.string.sort_device)) },
                )
            }
        }

        if (busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            if (state.censusBusyLocationIds.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.census_in_progress),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        HorizontalDivider()
        Text(
            text = stringResource(R.string.registered_sources),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (!state.loading && state.visibleSources.isEmpty()) {
            Text(stringResource(R.string.no_sources))
        }

        state.visibleSources.forEach { source ->
            SourceCensusRow(
                source = source,
                selection = state.selectionOf(source.locationId),
                censusBusy = source.locationId in state.censusBusyLocationIds,
                enabled = !busy,
                onToggleSelection = { onToggleSelection(source.locationId) },
                onRefreshOne = onRefreshStatus,
                onCensusOne = { onCensusOne(source.locationId) },
                onBrowse = { onBrowse(source.locationId) },
                onRemove = { onRemove(source.locationId) },
            )
            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.census_legend),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun SourceCensusRow(
    source: SourceSummary,
    selection: SourceCensusSelection,
    censusBusy: Boolean,
    enabled: Boolean,
    onToggleSelection: () -> Unit,
    onRefreshOne: () -> Unit,
    onCensusOne: () -> Unit,
    onBrowse: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Checkbox(
                checked = selection == SourceCensusSelection.SELECTED,
                onCheckedChange = { onToggleSelection() },
                enabled = enabled && !censusBusy,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.deviceLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = source.pathLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Text(
                    text = when (source.availability) {
                        Availability.AVAILABLE -> stringResource(R.string.status_available)
                        Availability.UNAVAILABLE -> stringResource(R.string.status_unavailable)
                        Availability.UNKNOWN -> stringResource(R.string.status_unknown)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (source.availability) {
                        Availability.AVAILABLE -> MaterialTheme.colorScheme.secondary
                        Availability.UNAVAILABLE -> MaterialTheme.colorScheme.error
                        Availability.UNKNOWN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefreshOne, enabled = enabled && !censusBusy) {
                Text("🔄", fontSize = 18.sp)
            }
            IconButton(
                onClick = onCensusOne,
                enabled = enabled && !censusBusy && source.availability == Availability.AVAILABLE,
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = stringResource(R.string.census_one))
            }
            IconButton(onClick = onBrowse, enabled = enabled && !censusBusy) {
                Icon(Icons.Outlined.PhotoLibrary, contentDescription = stringResource(R.string.browse_source_media))
            }
            IconButton(onClick = onRemove, enabled = enabled && !censusBusy) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.remove_source))
            }
        }
    }
}
