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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.cycleNext
import com.renatizzi.photovideomanager.domain.model.cycleNextCatalog
import com.renatizzi.photovideomanager.ui.acquire.AcquireUiState
import com.renatizzi.photovideomanager.ui.census.CensusUiState
import com.renatizzi.photovideomanager.ui.census.SourceSelectionBox
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail
import com.renatizzi.photovideomanager.ui.common.formatBytes
import com.renatizzi.photovideomanager.ui.search.SearchUiState

/**
 * Layout congelato (Nota v5.3 §5.6) — azioni in alto.
 * Acquisisci/Importa/Aggiorna: dati reali; Componi landing resta template fino a M11.
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AcquisisciStaticScreen(
    state: CensusUiState,
    onAddSource: (Uri, String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onSetSelection: (String, SourceCensusSelection) -> Unit = { _, _ -> },
    onSetAllSelected: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onConferma: () -> Unit,
    onBrowse: (String) -> Unit = {},
    onRenameSource: (String, String) -> Unit = { _, _ -> },
    onRenameDevice: (String) -> Unit = {},
    onRemoveSource: (String) -> Unit = {},
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
                context.getString(R.string.external_source_default_name),
            )
            onAddSource(uri, name)
        }
    }
    // Fonti con X (escluse in attesa di conferma) restano visibili con la X nel riquadro.
    val sources = state.visibleSources
    val selectedCount = sources.count {
        state.selectionOf(it.locationId) == SourceCensusSelection.SELECTED
    }
    val activeCount = sources.count {
        state.selectionOf(it.locationId) != SourceCensusSelection.EXCLUDED
    }
    val allChecked = activeCount > 0 && selectedCount == activeCount
    val busy = state.loading || state.censusBusyLocationIds.isNotEmpty()
    var renameTarget by remember { mutableStateOf<SourceSummary?>(null) }
    var removeTarget by remember { mutableStateOf<SourceSummary?>(null) }
    var renameDeviceOpen by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    fun cycleSourceSelection(source: SourceSummary) {
        val current = state.selectionOf(source.locationId)
        val next = current.cycleNext()
        onToggleSelection(source.locationId)
        // Arrivati a X: mostra conferma; SÌ toglie dall’elenco, NO torna al riquadro vuoto.
        if (next == SourceCensusSelection.EXCLUDED) {
            removeTarget = source
        }
    }

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
                R.string.acquisisci_riepilogo_full,
                selectedCount,
                sources.size,
                state.catalogCount,
                state.acquiredCount,
                formatBytes(state.acquiredUsedBytes),
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        TextButton(
            onClick = {
                renameText = state.deviceAlias
                renameDeviceOpen = true
            },
            enabled = !busy,
        ) {
            Text(stringResource(R.string.acquisisci_rename_device, state.deviceAlias.ifBlank { "…" }))
        }

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
                        selection = state.selectionOf(source.locationId),
                        busy = source.locationId in state.censusBusyLocationIds,
                        enabled = !busy,
                        onToggle = { cycleSourceSelection(source) },
                        onBrowse = { onBrowse(source.locationId) },
                        onLongPress = {
                            renameTarget = source
                            renameText = source.displayName
                        },
                    )
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.acquisisci_rename_source_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.acquisisci_rename_source_label)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameSource(target.locationId, renameText.trim())
                        }
                        renameTarget = null
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.importa_annulla))
                }
            },
        )
    }
    if (renameDeviceOpen) {
        AlertDialog(
            onDismissRequest = { renameDeviceOpen = false },
            title = { Text(stringResource(R.string.acquisisci_rename_device_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.acquisisci_rename_device_label)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameDevice(renameText.trim())
                        renameDeviceOpen = false
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { renameDeviceOpen = false }) {
                    Text(stringResource(R.string.importa_annulla))
                }
            },
        )
    }
    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = {
                // NO / fuori dialogo: torna al riquadro vuoto (non selezionata).
                onSetSelection(target.locationId, SourceCensusSelection.NOT_SELECTED)
                removeTarget = null
            },
            title = { Text(stringResource(R.string.acquisisci_remove_confirm_title)) },
            text = {
                Text(stringResource(R.string.acquisisci_remove_confirm_body, target.displayName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveSource(target.locationId)
                        removeTarget = null
                    },
                ) { Text(stringResource(R.string.dialog_yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onSetSelection(target.locationId, SourceCensusSelection.NOT_SELECTED)
                        removeTarget = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SourceRow(
    source: SourceSummary,
    selection: SourceCensusSelection,
    busy: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    onBrowse: () -> Unit,
    onLongPress: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SourceSelectionBox(
            selection = selection,
            enabled = enabled && !busy,
            onClick = onToggle,
            modifier = Modifier.padding(end = 8.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .combinedClickable(
                    enabled = enabled,
                    onClick = onBrowse,
                    onLongClick = onLongPress,
                ),
        ) {
            // Titolo cartella · dispositivo · percorso (senza «Tipo: …»)
            Text(source.displayName, fontWeight = FontWeight.SemiBold)
            Text(source.deviceLabel, style = MaterialTheme.typography.labelMedium)
            Text(source.pathLabel, style = MaterialTheme.typography.bodyMedium)
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
fun ImportaStaticScreen(
    state: AcquireUiState,
    onToggle: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
    onRefresh: () -> Unit,
    onImporta: () -> Unit,
    onContinue: () -> Unit = {},
    onBackToAcquisisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pool = state.kindFiltered
    val listed = state.listedCandidates
    val selected = pool.count { it.mediaItem.id in state.selectedIds }
    val photos = pool.count { it.mediaItem.kind == MediaKind.PHOTO }
    val videos = pool.count { it.mediaItem.kind == MediaKind.VIDEO }
    val already = pool.count { it.alreadyInPersonalArchive }
    val allAlreadyInCatalog = pool.isNotEmpty() && already == pool.size
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
            text = if (allAlreadyInCatalog) {
                stringResource(R.string.importa_all_in_catalog)
            } else {
                stringResource(R.string.importa_intro)
            },
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
                pool.size,
                photos,
                videos,
                already,
                selected,
            ),
            style = MaterialTheme.typography.bodySmall,
        )

        // Azioni: IMPORTA (o CONTINUA se tutto già in Catalogo); Annulla = torna ad Acquisisci
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefresh, enabled = !busy) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
            }
            Button(
                onClick = if (allAlreadyInCatalog) onContinue else onImporta,
                enabled = !busy && (allAlreadyInCatalog || state.selectedIds.isNotEmpty()),
                modifier = Modifier.weight(1f),
            ) {
                if (state.acquiring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        stringResource(
                            if (allAlreadyInCatalog) R.string.importa_continua
                            else R.string.importa_action,
                        ),
                    )
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
            if (state.loading && pool.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (listed.isEmpty()) {
                Text(
                    text = if (pool.isEmpty()) {
                        stringResource(R.string.importa_empty)
                    } else {
                        stringResource(R.string.importa_empty_deselected)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                listed.forEach { candidate ->
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
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
    onToggleSelection: (String) -> Unit = {},
    onSetSelection: (String, SourceCensusSelection) -> Unit = { _, _ -> },
    onRefresh: () -> Unit,
    onPulisci: () -> Unit,
    onEdit: (String) -> Unit = {},
    onRename: (String, String) -> Unit = { _, _ -> },
    onTrash: (String) -> Unit = {},
    onExportRequest: (mediaItemId: String, suggestedName: String, mime: String) -> Unit = { _, _, _ -> },
    suggestedFileName: (String) -> String = { "media" },
    exportMime: (String) -> String = { "*/*" },
    modifier: Modifier = Modifier,
) {
    val entries = state.entries
    val listed = state.listedEntries
    val photos = entries.count { it.mediaItem.kind == MediaKind.PHOTO }
    val videos = entries.count { it.mediaItem.kind == MediaKind.VIDEO }
    var menuForId by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<CatalogSearchEntry?>(null) }
    var renameText by remember { mutableStateOf("") }
    var trashTarget by remember { mutableStateOf<CatalogSearchEntry?>(null) }

    fun cycleCatalogRow(entry: CatalogSearchEntry) {
        val id = entry.mediaItem.id
        val next = state.selectionOf(id).cycleNextCatalog()
        onToggleSelection(id)
        // X = Elimina dal Catalogo → Cestino (conferma); niente voce menu ridondante.
        if (next == SourceCensusSelection.EXCLUDED) {
            trashTarget = entry
        }
    }

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
            } else if (listed.isEmpty()) {
                Text(
                    text = stringResource(R.string.aggiorna_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                listed.forEach { entry ->
                    HorizontalDivider()
                    val title = entry.mediaItem.displayTitle?.ifBlank { null }
                        ?: entry.previewCopy?.opaqueLocator?.substringAfterLast('/')
                            ?.substringAfterLast(':')
                        ?: entry.mediaItem.id
                    val loc = entry.locationNames.firstOrNull().orEmpty()
                    val id = entry.mediaItem.id
                    MediaRow(
                        title = title,
                        subtitle = stringResource(R.string.importa_riga_meta, loc.ifBlank { "—" }),
                        selection = state.selectionOf(id),
                        showMenu = true,
                        menuExpanded = menuForId == id,
                        onMenuExpandedChange = { open ->
                            menuForId = if (open) id else null
                        },
                        previewCopy = entry.previewCopy,
                        kind = entry.mediaItem.kind,
                        onCycleSelection = { cycleCatalogRow(entry) },
                        onEdit = {
                            menuForId = null
                            onEdit(id)
                        },
                        onRename = {
                            menuForId = null
                            renameTarget = entry
                            renameText = title
                        },
                        onCopyToDevice = {
                            menuForId = null
                            onExportRequest(id, suggestedFileName(id), exportMime(id))
                        },
                    )
                }
                HorizontalDivider()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.aggiorna_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.aggiorna_rename_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(target.mediaItem.id, renameText)
                        renameTarget = null
                    },
                    enabled = renameText.isNotBlank(),
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.importa_annulla))
                }
            },
        )
    }

    trashTarget?.let { target ->
        AlertDialog(
            onDismissRequest = {
                onSetSelection(target.mediaItem.id, SourceCensusSelection.NOT_SELECTED)
                trashTarget = null
            },
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Text(stringResource(R.string.aggiorna_trash_confirm_body))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTrash(target.mediaItem.id)
                        trashTarget = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onSetSelection(target.mediaItem.id, SourceCensusSelection.NOT_SELECTED)
                        trashTarget = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
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
                    selection = SourceCensusSelection.NOT_SELECTED,
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
    selection: SourceCensusSelection,
    showMenu: Boolean = false,
    menuExpanded: Boolean = false,
    onMenuExpandedChange: (Boolean) -> Unit = {},
    previewCopy: com.renatizzi.photovideomanager.domain.model.MediaCopy? = null,
    kind: MediaKind = MediaKind.PHOTO,
    onCycleSelection: (() -> Unit)? = null,
    onEdit: () -> Unit = {},
    onRename: () -> Unit = {},
    onCopyToDevice: () -> Unit = {},
    onMenu: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SourceSelectionBox(
            selection = selection,
            enabled = onCycleSelection != null,
            onClick = { onCycleSelection?.invoke() },
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
            Box {
                IconButton(onClick = { onMenuExpandedChange(true); onMenu() }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.aggiorna_menu_cd),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.aggiorna_menu_edit)) },
                        onClick = onEdit,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.aggiorna_menu_rename)) },
                        onClick = onRename,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.aggiorna_menu_copy_device)) },
                        onClick = onCopyToDevice,
                    )
                }
            }
        }
    }
}
