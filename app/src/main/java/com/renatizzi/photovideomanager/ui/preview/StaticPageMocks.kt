package com.renatizzi.photovideomanager.ui.preview

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.ui.acquire.AcquireUiState
import com.renatizzi.photovideomanager.ui.census.CensusUiState
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

/**
 * Layout congelato (Nota v5.3 §5.6) — azioni in alto.
 * Acquisisci/Importa ricevono dati reali; Aggiorna/Componi restano mock fino al wiring.
 */

@Composable
fun AcquisisciStaticScreen(
    state: CensusUiState,
    onAddSource: (Uri, String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onSetAllSelected: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onConferma: () -> Unit,
    onBrowse: (String) -> Unit = {},
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
    val sources = state.visibleSources
    val selectedCount = sources.count {
        state.selectionOf(it.locationId) == SourceCensusSelection.SELECTED
    }
    val allChecked = sources.isNotEmpty() && selectedCount == sources.size
    val busy = state.loading || state.censusBusyLocationIds.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_acquisisci),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.acquisisci_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Text(
            text = stringResource(R.string.acquisisci_elenco_fonti),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(
                R.string.acquisisci_riepilogo,
                selectedCount,
                sources.size,
                state.catalogCount,
            ),
            style = MaterialTheme.typography.bodySmall,
        )

        // Azioni in alto (layout congelato): refresh, +, CONFERMA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefresh, enabled = !busy) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
            }
            IconButton(
                onClick = { openTree.launch(null) },
                enabled = !busy,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_source),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Button(
                onClick = onConferma,
                enabled = !busy && selectedCount > 0,
                modifier = Modifier.weight(1f),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.acquisisci_conferma))
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = allChecked,
                onCheckedChange = { onSetAllSelected(it) },
                enabled = !busy && sources.isNotEmpty(),
            )
            Text(stringResource(R.string.acquisisci_tutti))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.loading && sources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (sources.isEmpty()) {
                Text(
                    text = stringResource(R.string.acquisisci_empty_sources),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                sources.forEach { source ->
                    HorizontalDivider()
                    SourceRow(
                        source = source,
                        selected = state.selectionOf(source.locationId) ==
                            SourceCensusSelection.SELECTED,
                        busy = source.locationId in state.censusBusyLocationIds,
                        enabled = !busy,
                        onToggle = { onToggleSelection(source.locationId) },
                        onBrowse = { onBrowse(source.locationId) },
                    )
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SourceRow(
    source: SourceSummary,
    selected: Boolean,
    busy: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onBrowse: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onToggle() },
            enabled = enabled,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
        ) {
            Text(source.deviceLabel, fontWeight = FontWeight.SemiBold)
            Text(
                text = adapterKindLabel(source.adapterKind),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(source.pathLabel, style = MaterialTheme.typography.bodySmall)
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
                    Availability.UNKNOWN ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                },
            )
        }
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onBrowse, enabled = enabled) {
                Text(">", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun adapterKindLabel(kind: StorageAdapterKind): String = when (kind) {
    StorageAdapterKind.LOCAL_FS -> stringResource(R.string.adapter_local)
    StorageAdapterKind.SAF_TREE -> stringResource(R.string.adapter_saf)
    StorageAdapterKind.MEDIA_STORE -> stringResource(R.string.adapter_mediastore)
    StorageAdapterKind.SMB -> stringResource(R.string.adapter_smb)
}

@Composable
fun ImportaStaticScreen(
    state: AcquireUiState,
    onToggle: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
    onRefresh: () -> Unit,
    onImporta: () -> Unit,
    onBackToAcquisisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = state.visibleCandidates
    val selected = visible.count { it.mediaItem.id in state.selectedIds }
    val photos = visible.count { it.mediaItem.kind == MediaKind.PHOTO }
    val videos = visible.count { it.mediaItem.kind == MediaKind.VIDEO }
    val already = visible.count { it.alreadyInPersonalArchive }
    val busy = state.loading || state.acquiring

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.importa_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.importa_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Text(
            text = stringResource(R.string.importa_elenco),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(
                R.string.importa_riepilogo,
                visible.size,
                photos,
                videos,
                already,
                selected,
            ),
            style = MaterialTheme.typography.bodySmall,
        )

        // Azioni in alto (layout congelato): refresh + IMPORTA; Annulla = torna ad Acquisisci
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefresh, enabled = !busy) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
            }
            Button(
                onClick = onImporta,
                enabled = !busy && state.selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                if (state.acquiring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.importa_action))
                }
            }
            OutlinedButton(onClick = onBackToAcquisisci, enabled = !busy) {
                Text(stringResource(R.string.importa_annulla))
            }
        }

        KindFilters(
            selected = state.kindFilter,
            enabled = !busy,
            onSelect = onKindFilter,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.loading && visible.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (visible.isEmpty()) {
                Text(
                    text = stringResource(R.string.importa_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                visible.forEach { candidate ->
                    HorizontalDivider()
                    ImportCandidateRow(
                        candidate = candidate,
                        checked = candidate.mediaItem.id in state.selectedIds ||
                            candidate.alreadyInPersonalArchive,
                        enabled = !busy && !candidate.alreadyInPersonalArchive,
                        onToggle = { onToggle(candidate.mediaItem.id) },
                    )
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ImportCandidateRow(
    candidate: AcquireCandidate,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val title = candidate.mediaItem.displayTitle?.ifBlank { null }
        ?: candidate.sourceCopy.opaqueLocator.substringAfterLast('/')
            .substringAfterLast(':')
            .ifBlank { candidate.mediaItem.id }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
        )
        MediaThumbnail(
            copy = candidate.sourceCopy,
            kind = candidate.mediaItem.kind,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.importa_riga_meta, candidate.sourceLocationName),
                style = MaterialTheme.typography.bodySmall,
            )
            if (candidate.alreadyInPersonalArchive) {
                Text(
                    text = stringResource(R.string.acquire_already_personal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
fun AggiornaStaticScreen(
    state: com.renatizzi.photovideomanager.ui.search.SearchUiState,
    onQueryChange: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
    onToggleSelection: (String) -> Unit = {},
    onRefresh: () -> Unit,
    onPulisci: () -> Unit,
    onRowMenu: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val entries = state.entries
    val photos = entries.count { it.mediaItem.kind == MediaKind.PHOTO }
    val videos = entries.count { it.mediaItem.kind == MediaKind.VIDEO }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_aggiorna),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.aggiorna_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        CatalogSearchField(query = state.query, onQueryChange = onQueryChange)

        Text(
            text = stringResource(R.string.aggiorna_catalogo_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.aggiorna_riepilogo, entries.size, photos, videos),
            style = MaterialTheme.typography.bodySmall,
        )

        // Azioni in alto: refresh (Allinea) + PULISCI
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefresh, enabled = !state.loading) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.aggiorna_refresh_cd),
                )
            }
            Button(onClick = onPulisci, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.feature_pulisci))
            }
        }

        KindFilters(
            selected = state.kindFilter,
            enabled = !state.loading,
            onSelect = onKindFilter,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.loading && entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.aggiorna_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                entries.forEach { entry ->
                    HorizontalDivider()
                    val title = entry.mediaItem.displayTitle?.ifBlank { null }
                        ?: entry.previewCopy?.opaqueLocator?.substringAfterLast('/')
                            ?.substringAfterLast(':')
                        ?: entry.mediaItem.id
                    val loc = entry.locationNames.firstOrNull().orEmpty()
                    MediaRow(
                        title = title,
                        subtitle = stringResource(R.string.importa_riga_meta, loc.ifBlank { "—" }),
                        checked = entry.mediaItem.id in state.selectedIds,
                        showMenu = true,
                        previewCopy = entry.previewCopy,
                        kind = entry.mediaItem.kind,
                        onCheckedChange = { onToggleSelection(entry.mediaItem.id) },
                        onMenu = { onRowMenu(entry.mediaItem.id) },
                    )
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Componi landing (template): ricerca, + ALBUM / + RACCOLTA, elenco con mini-cover e menu ⋮.
 * Crea/Edita/Pubblica non sono bottoni hub separati sulla landing.
 */
@Composable
fun ComponiLandingStaticScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val albums = listOf(
        "Vacanze 2024" to "Album · ultima modifica 12/08/2026",
        "Compleanno Marco" to "Raccolta · ultima modifica 03/09/2026",
        "Ricordi famiglia" to "Album · ultima modifica 01/10/2026",
        "Evento scuola" to "Raccolta · ultima modifica 20/09/2026",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.componi),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.componi_landing_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        CatalogSearchField(query = query, onQueryChange = { query = it })

        Text(
            text = stringResource(R.string.componi_elenco_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.componi_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        // Template: (+) ALBUM / RACCOLTA — creazione; modifica via menu contestuale riga
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.componi_add_cd),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.componi_album))
            }
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.componi_raccolta))
            }
        }

        KindFilters()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            albums.forEach { (name, meta) ->
                HorizontalDivider()
                MediaRow(
                    title = name,
                    subtitle = meta,
                    checked = false,
                    showMenu = true,
                )
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CatalogSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_query_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        label = { Text(stringResource(R.string.feature_ricerca)) },
    )
}

@Composable
private fun KindFilters(
    selected: SearchKindFilter = SearchKindFilter.ALL,
    enabled: Boolean = true,
    onSelect: (SearchKindFilter) -> Unit = {},
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == SearchKindFilter.ALL,
            onClick = { onSelect(SearchKindFilter.ALL) },
            enabled = enabled,
            label = { Text(stringResource(R.string.search_filter_all)) },
        )
        FilterChip(
            selected = selected == SearchKindFilter.PHOTO,
            onClick = { onSelect(SearchKindFilter.PHOTO) },
            enabled = enabled,
            label = { Text(stringResource(R.string.search_filter_photo)) },
        )
        FilterChip(
            selected = selected == SearchKindFilter.VIDEO,
            onClick = { onSelect(SearchKindFilter.VIDEO) },
            enabled = enabled,
            label = { Text(stringResource(R.string.search_filter_video)) },
        )
    }
}

@Composable
private fun MediaRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    showMenu: Boolean = false,
    previewCopy: com.renatizzi.photovideomanager.domain.model.MediaCopy? = null,
    kind: MediaKind = MediaKind.PHOTO,
    onCheckedChange: (() -> Unit)? = null,
    onMenu: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (onCheckedChange != null) {
                { onCheckedChange() }
            } else {
                null
            },
        )
        if (previewCopy != null) {
            MediaThumbnail(copy = previewCopy, kind = kind)
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        if (showMenu) {
            IconButton(onClick = onMenu) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.componi_menu_cd))
            }
        }
    }
}
