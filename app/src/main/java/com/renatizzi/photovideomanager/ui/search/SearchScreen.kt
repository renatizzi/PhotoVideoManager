package com.renatizzi.photovideomanager.ui.search

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
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
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.search_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.search_inline_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.search_query_label)) },
            placeholder = { Text(stringResource(R.string.search_query_hint)) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.kindFilter == SearchKindFilter.ALL,
                onClick = { onKindFilter(SearchKindFilter.ALL) },
                label = { Text(stringResource(R.string.search_filter_all)) },
            )
            FilterChip(
                selected = state.kindFilter == SearchKindFilter.PHOTO,
                onClick = { onKindFilter(SearchKindFilter.PHOTO) },
                label = { Text(stringResource(R.string.search_filter_photo)) },
            )
            FilterChip(
                selected = state.kindFilter == SearchKindFilter.VIDEO,
                onClick = { onKindFilter(SearchKindFilter.VIDEO) },
                label = { Text(stringResource(R.string.search_filter_video)) },
            )
        }

        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.refresh_sources))
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
            text = stringResource(R.string.search_results_title, state.entries.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (!state.loading && state.entries.isEmpty()) {
            Text(
                text = if (state.query.isBlank()) {
                    stringResource(R.string.search_empty_catalog)
                } else {
                    stringResource(R.string.search_empty_query)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }

        state.entries.forEach { entry ->
            SearchRow(entry)
            HorizontalDivider()
        }
    }
}

@Composable
private fun SearchRow(entry: CatalogSearchEntry) {
    val title = entry.mediaItem.displayTitle?.ifBlank { null } ?: entry.mediaItem.id
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = when (entry.mediaItem.kind) {
                MediaKind.PHOTO -> stringResource(R.string.media_kind_photo)
                MediaKind.VIDEO -> stringResource(R.string.media_kind_video)
            },
            style = MaterialTheme.typography.bodySmall,
        )
        if (entry.locationNames.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.search_locations,
                    entry.locationNames.joinToString(", "),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = stringResource(R.string.search_copies, entry.activeCopyCount),
            style = MaterialTheme.typography.bodySmall,
        )
        if (entry.inPersonalArchive) {
            Text(
                text = stringResource(R.string.search_in_personal),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
