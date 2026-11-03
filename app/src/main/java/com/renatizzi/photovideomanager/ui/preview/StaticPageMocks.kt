package com.renatizzi.photovideomanager.ui.preview

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FilterChipDefaults
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
import com.renatizzi.photovideomanager.ui.acquire.AcquireViewModel
import com.renatizzi.photovideomanager.ui.census.CensusUiState
import com.renatizzi.photovideomanager.ui.census.SourceSelectionBox
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail
import com.renatizzi.photovideomanager.ui.common.MediaThumbnailModel
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
    onOpenCatalogSeatWizard: () -> Unit = {},
    onOpenCatalogUpdate: () -> Unit = {},
    seatConfigured: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var renameTarget by remember { mutableStateOf<SourceSummary?>(null) }
    var removeTarget by remember { mutableStateOf<SourceSummary?>(null) }
    var pcGuideOpen by remember { mutableStateOf(false) }
    var pcHelpOpen by remember { mutableStateOf(false) }
    var addDeviceOpen by remember { mutableStateOf(false) }
    var setupSorgentiStep by remember { mutableStateOf<String?>(null) } // avvio | done
    var sorgentiKind by remember {
        mutableStateOf(com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.LOCAL_DEVICE)
    }
    var sorgentiModel by remember { mutableStateOf("") }
    var sorgentiAwaitingPick by remember { mutableStateOf(false) }
    var addTip by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
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
            if (sorgentiAwaitingPick) {
                sorgentiAwaitingPick = false
                setupSorgentiStep = "done"
                addDeviceOpen = false
            }
        } else {
            sorgentiAwaitingPick = false
        }
    }
    // Fonti con X (escluse in attesa di conferma) restano visibili con la X nel riquadro.
    val sources = state.visibleSources
    val selectedCount = sources.count {
        state.selectionOf(it.locationId) == SourceCensusSelection.SELECTED
    }
    val busy = state.loading || state.censusBusyLocationIds.isNotEmpty()

    fun cycleSourceSelection(source: SourceSummary) {
        // Bug 3: setSelection esplicito (niente toggle ambiguo dopo X/dialog).
        val current = state.selectionOf(source.locationId)
        val next = current.cycleNext()
        onSetSelection(source.locationId, next)
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
            text = stringResource(R.string.acquisisci_static_subtitle),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )

        val hasCatalog = state.catalogSeatConfigured || seatConfigured
        if (!hasCatalog) {
            Text(
                text = stringResource(R.string.acquisisci_needs_catalog_body),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onOpenCatalogSeatWizard,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.acquisisci_needs_catalog_crea))
            }
            return@Column
        }

        Text(
            text = stringResource(R.string.acquisisci_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        com.renatizzi.photovideomanager.ui.common.CatalogSeatStatusRow(
            status = state.catalogSeatStatus,
            onOpenSeat = onOpenCatalogSeatWizard,
            showWhenMissing = false,
        )

        if (state.linkedFolderCount > 0) {
            Text(
                text = stringResource(R.string.acquisisci_n_cartelle, state.linkedFolderCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.catalogPhotoCount > 0L) {
            Text(
                text = stringResource(R.string.acquisisci_n_foto, state.catalogPhotoCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.catalogVideoCount > 0L) {
            Text(
                text = stringResource(R.string.acquisisci_n_video, state.catalogVideoCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text(
            text = stringResource(R.string.acquisisci_hint_plus_confirm),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        // Azioni: refresh, [+], CONFERMA (niente procedura finta Sorgenti — bug 6).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onRefresh, enabled = !busy) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
            }
            IconButton(
                onClick = {
                    sorgentiAwaitingPick = false
                    openTree.launch(null)
                },
                enabled = !busy,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.sorgenti_choose_folder),
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

        if (sources.isNotEmpty()) {
            Text(
                text = stringResource(R.string.acquisisci_elenco_collegate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
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
                // Nota bis: riga = dispositivo (titolo) + SD?/path - stato; niente header gruppo né alias.
                sources.forEach { source ->
                    HorizontalDivider()
                    SourceRow(
                        source = source,
                        selection = state.selectionOf(source.locationId),
                        busy = source.locationId in state.censusBusyLocationIds,
                        enabled = !busy && removeTarget == null,
                        showDeviceLabel = true,
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

    if (addDeviceOpen && setupSorgentiStep == "avvio") {
        AlertDialog(
            onDismissRequest = {
                addDeviceOpen = false
                setupSorgentiStep = null
            },
            title = { Text(stringResource(R.string.sorgenti_wizard_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.sorgenti_avvio_subtitle),
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                    Text(
                        text = stringResource(R.string.sorgenti_avvio_desc),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    com.renatizzi.photovideomanager.ui.hub.DeviceKindDropdown(
                        selected = sorgentiKind,
                        onSelectKind = { sorgentiKind = it },
                        enabled = true,
                    )
                    OutlinedTextField(
                        value = sorgentiModel,
                        onValueChange = { sorgentiModel = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.hub_avvio_model_hint)) },
                        label = { Text(stringResource(R.string.hub_avvio_model_q)) },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (sorgentiKind) {
                            com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.PC_NOTEBOOK -> {
                                addDeviceOpen = false
                                setupSorgentiStep = null
                                sorgentiAwaitingPick = true
                                pcGuideOpen = true
                            }
                            com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.CLOUD_DRIVE -> {
                                addDeviceOpen = false
                                setupSorgentiStep = null
                                sorgentiAwaitingPick = true
                                addTip = "drive"
                            }
                            com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.NETWORK_NAS -> {
                                addDeviceOpen = false
                                setupSorgentiStep = null
                                sorgentiAwaitingPick = true
                                addTip = "nas"
                            }
                            else -> {
                                addDeviceOpen = false
                                setupSorgentiStep = null
                                sorgentiAwaitingPick = true
                                openTree.launch(null)
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.sorgenti_choose_folder))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        addDeviceOpen = false
                        setupSorgentiStep = null
                    },
                ) {
                    Text(stringResource(R.string.hub_avvio_esc))
                }
            },
        )
    }
    if (setupSorgentiStep == "done") {
        AlertDialog(
            onDismissRequest = { setupSorgentiStep = null },
            title = { Text(stringResource(R.string.sorgenti_done_title)) },
            text = { Text(stringResource(R.string.sorgenti_done_body)) },
            confirmButton = {
                Button(onClick = { setupSorgentiStep = null }) {
                    Text(stringResource(R.string.hub_done_torna_accesso))
                }
            },
        )
    }

    if (addTip != null) {
        val tipText = when (addTip) {
            "drive" -> stringResource(R.string.acquisisci_add_drive_tip)
            else -> stringResource(R.string.acquisisci_add_nas_tip)
        }
        AlertDialog(
            onDismissRequest = { addTip = null },
            title = {
                Text(
                    if (addTip == "drive") {
                        stringResource(R.string.acquisisci_add_drive)
                    } else {
                        stringResource(R.string.acquisisci_add_nas)
                    },
                )
            },
            text = { Text(tipText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        addTip = null
                        openTree.launch(null)
                    },
                ) {
                    Text(stringResource(R.string.acquisisci_da_pc_choose))
                }
            },
            dismissButton = {
                TextButton(onClick = { addTip = null }) {
                    Text(stringResource(R.string.importa_annulla))
                }
            },
        )
    }

    if (pcGuideOpen) {
        AlertDialog(
            onDismissRequest = { pcGuideOpen = false },
            title = { Text(stringResource(R.string.acquisisci_da_pc_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(stringResource(R.string.acquisisci_da_pc_step1))
                    Text(stringResource(R.string.acquisisci_da_pc_step2))
                    Text(stringResource(R.string.acquisisci_da_pc_step3))
                    Text(
                        text = stringResource(R.string.acquisisci_da_pc_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    TextButton(
                        onClick = {
                            pcGuideOpen = false
                            pcHelpOpen = true
                        },
                    ) {
                        Text(stringResource(R.string.acquisisci_da_pc_help_link))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pcGuideOpen = false
                        openTree.launch(null)
                    },
                ) {
                    Text(stringResource(R.string.acquisisci_da_pc_choose))
                }
            },
            dismissButton = {
                TextButton(onClick = { pcGuideOpen = false }) {
                    Text(stringResource(R.string.importa_annulla))
                }
            },
        )
    }
    if (pcHelpOpen) {
        AlertDialog(
            onDismissRequest = { pcHelpOpen = false },
            title = { Text(stringResource(R.string.acquisisci_da_pc_help_title)) },
            text = {
                Text(
                    text = stringResource(R.string.acquisisci_da_pc_help_body),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pcHelpOpen = false
                        openTree.launch(null)
                    },
                ) {
                    Text(stringResource(R.string.acquisisci_da_pc_choose))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pcHelpOpen = false
                        pcGuideOpen = true
                    },
                ) {
                    Text(stringResource(R.string.acquisisci_da_pc_help_back))
                }
            },
        )
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
    // Nota bis: niente riga/dialog «Questo tablet: … (tocca per rinominare)».
    @Suppress("UNUSED_PARAMETER")
    val unusedRenameDevice = onRenameDevice
    removeTarget?.let { target ->
        AlertDialog(
            onDismissRequest = {
                // NO / fuori dialogo: torna al riquadro vuoto (non selezionata).
                onSetSelection(target.locationId, SourceCensusSelection.NOT_SELECTED)
                removeTarget = null
            },
            title = {
                Text(
                    stringResource(
                        R.string.acquisisci_remove_confirm_title,
                        target.displayName,
                    ),
                )
            },
            text = {
                Text(stringResource(R.string.acquisisci_remove_confirm_body))
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
    showDeviceLabel: Boolean = true,
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
            // Nota bis: <Dispositivo> blu bold corsivo
            val device = source.deviceLabel.ifBlank {
                source.displayName.ifBlank { stringResource(R.string.external_source_default_name) }
            }
            Text(
                text = if (showDeviceLabel) device else source.displayName.ifBlank {
                    stringResource(R.string.external_source_default_name)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            // Meta: [SD] \path - stato (corsivo blu, non bold)
            val meta = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat.metaLine(
                deviceLabel = "",
                pathLabel = source.pathLabel.ifBlank { source.displayName },
                statusLabel = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
                    .availabilityLabel(source.availability),
                secondary = source.isSecondaryVolume,
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = when (source.availability) {
                        Availability.AVAILABLE -> MaterialTheme.colorScheme.primary
                        Availability.UNAVAILABLE -> MaterialTheme.colorScheme.error
                        Availability.UNKNOWN ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    },
                )
            }
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
    onToggleExclusion: (storageLocationId: String, opaqueLocator: String) -> Unit = { _, _ -> },
    onRestoreSelectedExclusions: () -> Unit = {},
    onRestoreExclusion: (storageLocationId: String, opaqueLocator: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val pool = state.kindFiltered
    // Conteggio = solo ✓ in selectedIds (non i già in Catalogo, lasciati deselezionati).
    val selected = pool.count {
        it.mediaItem.id in state.selectedIds && !it.alreadyInPersonalArchive
    }
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
            } else if (pool.isEmpty()) {
                Text(
                    text = stringResource(R.string.importa_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                // BL-04: mostra anche i deselezionati (✓ off) per multi-esclusione dall’import.
                pool.forEach { candidate ->
                    HorizontalDivider()
                    ImportCandidateRow(
                        candidate = candidate,
                        // Solo ✓ effettivi in selectedIds; già in Catalogo restano deselezionati.
                        checked = candidate.mediaItem.id in state.selectedIds,
                        enabled = !busy && !candidate.alreadyInPersonalArchive,
                        onToggle = { onToggle(candidate.mediaItem.id) },
                    )
                }
                HorizontalDivider()
            }

            if (state.exclusions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.importa_esclusi_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = onRestoreSelectedExclusions,
                        enabled = !busy && state.selectedExclusionCount > 0,
                    ) {
                        Text(stringResource(R.string.importa_esclusi_restore))
                    }
                }
                state.exclusions.forEach { exclusion ->
                    HorizontalDivider()
                    ExclusionRow(
                        exclusion = exclusion,
                        checked = AcquireViewModel.exclusionKey(exclusion) in
                            state.selectedExclusionKeys,
                        enabled = !busy,
                        onToggle = {
                            onToggleExclusion(
                                exclusion.storageLocationId,
                                exclusion.opaqueLocator,
                            )
                        },
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
    val selection = if (checked) {
        SourceCensusSelection.SELECTED
    } else {
        SourceCensusSelection.NOT_SELECTED
    }
    // Un solo clickable sulla riga: evitare doppio toggle (riquadro + Row) che annulla il tocco.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Riquadro solo visuale: il tocco è gestito dalla Row.
        SourceSelectionBox(
            selection = selection,
            enabled = false,
            onClick = {},
        )
        MediaThumbnail(
            copy = candidate.sourceCopy,
            kind = candidate.mediaItem.kind,
        )
        Column(modifier = Modifier.weight(1f)) {
            // Nota bis: <Nome File> blu bold corsivo
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            val device = candidate.sourceDeviceLabel.ifBlank {
                candidate.sourceLocationName.substringBefore('·').trim()
            }
            val path = candidate.sourcePathLabel
            val meta = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat.metaLine(
                deviceLabel = device,
                pathLabel = path,
                statusLabel = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
                    .availabilityLabel(Availability.AVAILABLE),
                secondary = candidate.isSecondaryVolume,
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
private fun ExclusionRow(
    exclusion: com.renatizzi.photovideomanager.domain.model.ImportExclusionSummary,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val kind = com.renatizzi.photovideomanager.domain.policy.MediaKindDetector.detect(
        fileName = exclusion.displayName,
        mimeType = null,
        locatorHint = exclusion.opaqueLocator,
    ) ?: MediaKind.PHOTO
    val model = runCatching { android.net.Uri.parse(exclusion.opaqueLocator) }.getOrNull()
    val selection = if (checked) {
        SourceCensusSelection.SELECTED
    } else {
        SourceCensusSelection.NOT_SELECTED
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SourceSelectionBox(
            selection = selection,
            enabled = false,
            onClick = {},
        )
        MediaThumbnailModel(model = model, kind = kind)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exclusion.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            val meta = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat.metaLine(
                deviceLabel = exclusion.deviceLabel,
                pathLabel = exclusion.pathLabel,
                statusLabel = stringResource(R.string.importa_esclusi_status),
                secondary = exclusion.isSecondaryVolume,
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
fun AggiornaStaticScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onKindFilter: (SearchKindFilter) -> Unit,
    onToggleSelection: (String) -> Unit = {},
    onSetSelection: (String, SourceCensusSelection) -> Unit = { _, _ -> },
    onRefresh: () -> Unit,
    onPulisci: () -> Unit,
    onOpenCatalogSeat: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onRename: (String, String) -> Unit = { _, _ -> },
    onTrash: (mediaItemId: String, deleteSourceFiles: Boolean) -> Unit = { _, _ -> },
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
            text = stringResource(R.string.aggiorna_static_subtitle),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )
        Text(
            text = stringResource(R.string.aggiorna_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        com.renatizzi.photovideomanager.ui.common.CatalogSeatStatusRow(
            status = state.seatStatus,
            showWhenMissing = false,
            onOpenSeat = onOpenCatalogSeat,
            compact = true,
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
                    val device = entry.deviceLabel.ifBlank {
                        entry.locationNames
                            .firstOrNull { it != "Spazio app" }
                            ?: entry.locationNames.firstOrNull()
                            ?: ""
                    }
                    val meta = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat.metaLine(
                        deviceLabel = device,
                        pathLabel = entry.pathLabel,
                        statusLabel = com.renatizzi.photovideomanager.domain.hub.CatalogListRowFormat
                            .availabilityLabel(entry.availability),
                        secondary = entry.isSecondaryVolume,
                    )
                    val id = entry.mediaItem.id
                    MediaRow(
                        title = title,
                        subtitle = meta.ifBlank { "—" },
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
        var deleteSource by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = {
                onSetSelection(target.mediaItem.id, SourceCensusSelection.NOT_SELECTED)
                trashTarget = null
            },
            title = { Text(stringResource(R.string.aggiorna_trash_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.aggiorna_trash_confirm_body))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deleteSource,
                            onCheckedChange = { deleteSource = it },
                        )
                        Text(stringResource(R.string.trash_also_delete_source))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTrash(target.mediaItem.id, deleteSource)
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
 * Componi landing TO-BE: switch ALBUM/RACCOLTA (default ALBUM), + contestuale, elenco filtrato.
 */
@Composable
fun ComponiLandingStaticScreen(
    seatStatus: com.renatizzi.photovideomanager.domain.hub.CatalogSeatStatus? = null,
    onOpenCatalogSeat: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var albumMode by remember { mutableStateOf(true) } // true = ALBUM (default)
    val albums = listOf(
        "Vacanze 2024" to "Album · ultima modifica 12/08/2026",
        "Compleanno Marco" to "Raccolta · ultima modifica 03/09/2026",
        "Ricordi famiglia" to "Album · ultima modifica 01/10/2026",
        "Evento scuola" to "Raccolta · ultima modifica 20/09/2026",
    )
    val filtered = albums.filter { (_, meta) ->
        if (albumMode) meta.startsWith("Album") else meta.startsWith("Raccolta")
    }.filter { (name, _) ->
        query.isBlank() || name.contains(query, ignoreCase = true)
    }

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
            text = stringResource(R.string.componi_static_subtitle),
            style = MaterialTheme.typography.titleMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )
        Text(
            text = stringResource(R.string.componi_landing_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        com.renatizzi.photovideomanager.ui.common.CatalogSeatStatusRow(
            status = seatStatus,
            onOpenSeat = onOpenCatalogSeat,
            compact = true,
            showWhenMissing = false,
        )

        CatalogSearchField(query = query, onQueryChange = { query = it })

        Text(
            text = stringResource(R.string.componi_elenco_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.componi_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        // Switch ALBUM / RACCOLTA + singolo +
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
            FilterChip(
                selected = albumMode,
                onClick = { albumMode = true },
                label = { Text(stringResource(R.string.componi_album)) },
            )
            FilterChip(
                selected = !albumMode,
                onClick = { albumMode = false },
                label = { Text(stringResource(R.string.componi_raccolta)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (albumMode) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(
                        if (albumMode) R.string.componi_empty_album else R.string.componi_empty_raccolta,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                filtered.forEach { (name, meta) ->
                    HorizontalDivider()
                    MediaRow(
                        title = name,
                        subtitle = meta,
                        selection = SourceCensusSelection.NOT_SELECTED,
                        showMenu = true,
                    )
                }
                HorizontalDivider()
            }
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
            // Nota bis: <Nome File> blu bold corsivo + meta Catalogo
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary,
            )
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
