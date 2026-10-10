package com.renatizzi.photovideomanager.ui.config

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind

@Composable
fun ArchiveSourcesScreen(
    state: ArchiveSourcesUiState,
    onAddFolder: (Uri, String) -> Unit,
    onRemove: (String) -> Unit,
    onCensus: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openTree = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            val name = com.renatizzi.photovideomanager.data.storage.SafPathLabels.folderTitle(
                context,
                uri,
                context.getString(R.string.external_folder_default_name),
            )
            onAddFolder(uri, name)
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
            text = stringResource(R.string.archivio),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.archivio_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Text(
            text = stringResource(R.string.catalog_status, state.catalogCount),
            style = MaterialTheme.typography.bodyMedium,
        )

        Button(
            onClick = { openTree.launch(null) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.add_folder))
        }

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading && state.censusInProgressLocationId == null,
        ) {
            Text(stringResource(R.string.refresh_sources))
        }

        if (state.loading || state.censusInProgressLocationId != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            if (state.censusInProgressLocationId != null) {
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

        if (!state.loading && state.sources.isEmpty()) {
            Text(stringResource(R.string.no_sources))
        }

        state.sources.forEach { source ->
            SourceRow(
                source = source,
                censusBusy = state.censusInProgressLocationId == source.locationId,
                censusEnabled = state.censusInProgressLocationId == null &&
                    source.availability == Availability.AVAILABLE &&
                    !source.isBuiltInPersonal,
                onCensus = { onCensus(source.locationId) },
                onRemove = { onRemove(source.locationId) },
            )
            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.shared_archive_placeholder),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun SourceRow(
    source: SourceSummary,
    censusBusy: Boolean,
    censusEnabled: Boolean,
    onCensus: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = source.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = adapterLabel(source.adapterKind),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = availabilityLabel(source.availability),
            style = MaterialTheme.typography.bodySmall,
            color = when (source.availability) {
                Availability.AVAILABLE -> MaterialTheme.colorScheme.secondary
                Availability.UNAVAILABLE -> MaterialTheme.colorScheme.error
                Availability.UNKNOWN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
        )
        if (!source.isBuiltInPersonal) {
            Button(
                onClick = onCensus,
                enabled = censusEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (censusBusy) {
                        stringResource(R.string.census_in_progress)
                    } else {
                        stringResource(R.string.census_folder)
                    },
                )
            }
            OutlinedButton(onClick = onRemove, enabled = !censusBusy) {
                Text(stringResource(R.string.remove_source))
            }
        } else {
            Text(
                text = stringResource(R.string.builtin_source_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun adapterLabel(kind: StorageAdapterKind): String = when (kind) {
    StorageAdapterKind.LOCAL_FS -> stringResource(R.string.adapter_local)
    StorageAdapterKind.SAF_TREE -> stringResource(R.string.adapter_saf)
    StorageAdapterKind.MEDIA_STORE -> stringResource(R.string.adapter_mediastore)
    StorageAdapterKind.SMB -> stringResource(R.string.adapter_smb)
}

@Composable
private fun availabilityLabel(availability: Availability): String = when (availability) {
    Availability.AVAILABLE -> stringResource(R.string.status_available)
    Availability.UNAVAILABLE -> stringResource(R.string.status_unavailable)
    Availability.UNKNOWN -> stringResource(R.string.status_unknown)
}
