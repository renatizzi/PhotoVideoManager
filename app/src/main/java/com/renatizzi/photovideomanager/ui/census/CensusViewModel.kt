package com.renatizzi.photovideomanager.ui.census

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.domain.model.DeviceSourceGroup
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSortMode
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.cycleNext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CensusUiState(
    val sources: List<SourceSummary> = emptyList(),
    val selection: Map<String, SourceCensusSelection> = emptyMap(),
    /** Default: per dispositivo (visibilità fonti P0). */
    val sortMode: SourceSortMode = SourceSortMode.DEVICE,
    val loading: Boolean = true,
    val censusBusyLocationIds: Set<String> = emptySet(),
    val catalogCount: Long = 0,
    val acquiredCount: Long = 0,
    /** Byte delle sole copie acquisite (spazio app catalogato), non orfani su disco. */
    val acquiredUsedBytes: Long = 0,
    /** Nome «questo tablet» per rinomina / intestazione gruppi locali. */
    val deviceAlias: String = "",
    /** Posizione del Catalogo (distinct dalle fonti Acquisisci). */
    val catalogSeatConfigured: Boolean = false,
    val catalogSeatLabel: String = "",
    val catalogSeatKindLabel: String = "",
    /** null = non ancora verificato in questo refresh. */
    val catalogSeatReachable: Boolean? = null,
    val message: String? = null,
) {
    val visibleSources: List<SourceSummary>
        get() {
            val censable = sources.filterNot { it.isBuiltInPersonal }
            return when (sortMode) {
                SourceSortMode.NAME -> censable.sortedBy { it.displayName.lowercase() }
                SourceSortMode.DEVICE ->
                    censable.sortedWith(
                        compareBy({ it.deviceLabel.lowercase() }, { it.displayName.lowercase() }),
                    )
                SourceSortMode.AVAILABILITY ->
                    censable.sortedWith(
                        compareBy({ it.availability.name }, { it.displayName.lowercase() }),
                    )
            }
        }

    /** Elenco Acquisisci a due livelli: dispositivo → cartelle/fonti. */
    val deviceGroups: List<DeviceSourceGroup>
        get() = visibleSources
            .groupBy { it.deviceLabel.ifBlank { "Altro dispositivo" } }
            .entries
            .sortedBy { it.key.lowercase() }
            .map { (label, list) ->
                DeviceSourceGroup(
                    deviceLabel = label,
                    sources = list.sortedBy { it.displayName.lowercase() },
                )
            }

    fun selectionOf(locationId: String): SourceCensusSelection =
        selection[locationId] ?: SourceCensusSelection.NOT_SELECTED
}

class CensusViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CensusUiState())
    val state: StateFlow<CensusUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                val sources = catalogFacade.listSources(refresh = true)
                val snap = catalogFacade.dashboardSnapshot()
                val deviceAlias = catalogFacade.deviceAliasOrDefault()
                val seat = catalogFacade.catalogSeat()
                val seatReachable =
                    if (seat.isConfigured) catalogFacade.catalogSeatReachable() else null
                CensusRefreshPayload(
                    sources = sources,
                    snap = snap,
                    deviceAlias = deviceAlias,
                    seatConfigured = seat.isConfigured,
                    seatLabel = seat.displayLabel,
                    seatKindLabel = seatKindLabel(seat.kind),
                    seatReachable = seatReachable,
                )
            }.onSuccess { payload ->
                _state.update { current ->
                    val censableIds = payload.sources
                        .filterNot { it.isBuiltInPersonal }
                        .map { it.locationId }
                        .toSet()
                    // Default: tutto selezionato (Nota); nuove sorgenti entrano selezionate.
                    val selection = buildMap {
                        for (id in censableIds) {
                            put(
                                id,
                                current.selection[id] ?: SourceCensusSelection.SELECTED,
                            )
                        }
                    }
                    current.copy(
                        sources = payload.sources,
                        selection = selection,
                        catalogCount = payload.snap.catalogPhotoCount + payload.snap.catalogVideoCount,
                        acquiredCount = payload.snap.acquiredPhotoCount + payload.snap.acquiredVideoCount,
                        acquiredUsedBytes = payload.snap.acquiredPhotoBytes + payload.snap.acquiredVideoBytes,
                        deviceAlias = payload.deviceAlias,
                        catalogSeatConfigured = payload.seatConfigured,
                        catalogSeatLabel = payload.seatLabel,
                        catalogSeatKindLabel = payload.seatKindLabel,
                        catalogSeatReachable = payload.seatReachable,
                        loading = false,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        message = error.message ?: "Errore nel caricamento delle sorgenti",
                    )
                }
            }
        }
    }

    private fun seatKindLabel(kind: CatalogSeatKind): String =
        when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> "Su questo dispositivo"
            CatalogSeatKind.PC_NOTEBOOK -> "Su PC / notebook"
            CatalogSeatKind.CLOUD_DRIVE -> "Su Google Drive / cloud"
            CatalogSeatKind.NETWORK_NAS -> "Su disco di rete / NAS"
            CatalogSeatKind.NONE -> ""
        }

    private data class CensusRefreshPayload(
        val sources: List<SourceSummary>,
        val snap: DashboardSnapshot,
        val deviceAlias: String,
        val seatConfigured: Boolean,
        val seatLabel: String,
        val seatKindLabel: String,
        val seatReachable: Boolean?,
    )

    fun renameSource(locationId: String, newName: String) {
        viewModelScope.launch {
            runCatching { catalogFacade.renameSource(locationId, newName) }
                .onSuccess {
                    refresh()
                    _state.update { it.copy(message = "Nome sorgente aggiornato") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Impossibile rinominare la sorgente")
                    }
                }
        }
    }

    fun setDeviceAlias(alias: String) {
        catalogFacade.setDeviceAlias(alias.trim().ifEmpty { null })
        refresh()
    }

    fun setSortMode(mode: SourceSortMode) {
        _state.update { it.copy(sortMode = mode) }
    }

    /** Ciclo riquadro Acquisisci: ✓/vuoto → X → vuoto (✓ solo con Tutti). */
    fun toggleSelection(locationId: String) {
        _state.update { current ->
            val next = current.selectionOf(locationId).cycleNext()
            current.copy(selection = current.selection + (locationId to next))
        }
    }

    fun setSelection(locationId: String, value: SourceCensusSelection) {
        _state.update { current ->
            current.copy(selection = current.selection + (locationId to value))
        }
    }

    fun setAllSelected(selected: Boolean) {
        _state.update { current ->
            val value =
                if (selected) SourceCensusSelection.SELECTED else SourceCensusSelection.NOT_SELECTED
            // «Tutti» non mette X: solo selezionato / non selezionato sulle fonti ancora in elenco.
            current.copy(
                selection = current.visibleSources.associate { it.locationId to value },
            )
        }
    }

    /**
     * Conferma (layout congelato): censisce le sorgenti selezionate, poi invoca [onDone].
     * [onDone] riceve successo e gli id delle fonti da usare in Importa.
     */
    fun confirmSelected(onDone: (Boolean, Set<String>) -> Unit) {
        val ids = _state.value.selection
            .filterValues { it == SourceCensusSelection.SELECTED }
            .keys
            .toList()
        if (ids.isEmpty()) {
            _state.update { it.copy(message = "Seleziona almeno una sorgente") }
            onDone(false, emptySet())
            return
        }
        viewModelScope.launch {
            var totalFound = 0
            var totalAdded = 0
            var totalSkipped = 0
            for (id in ids) {
                _state.update {
                    it.copy(censusBusyLocationIds = it.censusBusyLocationIds + id, message = null)
                }
                val result = runCatching { catalogFacade.censusSource(id) }
                result.onSuccess { census ->
                    totalFound += census.mediaFound
                    totalAdded += census.mediaAdded
                    totalSkipped += census.mediaSkippedExisting
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            censusBusyLocationIds = it.censusBusyLocationIds - id,
                            message = error.message ?: "Censimento non riuscito",
                        )
                    }
                    onDone(false, emptySet())
                    return@launch
                }
                _state.update {
                    it.copy(censusBusyLocationIds = it.censusBusyLocationIds - id)
                }
            }
            val snap = runCatching { catalogFacade.dashboardSnapshot() }.getOrNull()
            _state.update {
                it.copy(
                    catalogCount = snap?.let { s -> s.catalogPhotoCount + s.catalogVideoCount }
                        ?: catalogFacade.mediaItemCount(),
                    acquiredCount = snap?.let { s -> s.acquiredPhotoCount + s.acquiredVideoCount }
                        ?: it.acquiredCount,
                    acquiredUsedBytes = snap?.let { s -> s.acquiredPhotoBytes + s.acquiredVideoBytes }
                        ?: it.acquiredUsedBytes,
                    message = "Censimento ok ($totalFound trovati, $totalAdded nuovi in Catalogo). " +
                        "Premi IMPORTA per copiarli nel Catalogo personale.",
                )
            }
            onDone(true, ids.toSet())
        }
    }

    fun addSafSource(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.registerSafFolder(uri, displayName) }
                .onSuccess { result ->
                    val added = result.source
                    val sources = catalogFacade.listSources(refresh = true)
                    _state.update { current ->
                        current.copy(
                            sources = sources,
                            selection = current.selection +
                                (added.locationId to SourceCensusSelection.SELECTED),
                            loading = false,
                            message = if (result.alreadyPresent) {
                                "Sorgente già presente in elenco"
                            } else {
                                "Sorgente aggiunta: ${added.displayName}"
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            message = error.message ?: "Impossibile aggiungere la sorgente",
                        )
                    }
                }
        }
    }

    fun removeSource(locationId: String) {
        viewModelScope.launch {
            runCatching { catalogFacade.removeSource(locationId) }
                .onSuccess {
                    val sources = catalogFacade.listSources(refresh = true)
                    val count = catalogFacade.mediaItemCount()
                    _state.update { current ->
                        current.copy(
                            sources = sources,
                            catalogCount = count,
                            selection = current.selection - locationId,
                            message = "Sorgente rimossa",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Impossibile rimuovere la sorgente")
                    }
                }
        }
    }

    fun censusSelected() {
        val ids = _state.value.selection
            .filterValues { it == SourceCensusSelection.SELECTED }
            .keys
            .toList()
        if (ids.isEmpty()) {
            _state.update { it.copy(message = "Seleziona almeno una sorgente da censire") }
            return
        }
        viewModelScope.launch {
            var totalFound = 0
            var totalAdded = 0
            var totalSkipped = 0
            for (id in ids) {
                _state.update {
                    it.copy(censusBusyLocationIds = it.censusBusyLocationIds + id, message = null)
                }
                runCatching { catalogFacade.censusSource(id) }
                    .onSuccess { result ->
                        totalFound += result.mediaFound
                        totalAdded += result.mediaAdded
                        totalSkipped += result.mediaSkippedExisting
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                censusBusyLocationIds = it.censusBusyLocationIds - id,
                                message = error.message ?: "Censimento non riuscito",
                            )
                        }
                        return@launch
                    }
                _state.update {
                    it.copy(censusBusyLocationIds = it.censusBusyLocationIds - id)
                }
            }
            val count = catalogFacade.mediaItemCount()
            _state.update {
                it.copy(
                    catalogCount = count,
                    message = "Censimento terminato: trovati $totalFound, " +
                        "nuovi $totalAdded, già noti $totalSkipped",
                )
            }
        }
    }

    fun censusOne(locationId: String) {
        _state.update {
            it.copy(
                selection = it.selection + (locationId to SourceCensusSelection.SELECTED),
            )
        }
        viewModelScope.launch {
            _state.update {
                it.copy(censusBusyLocationIds = it.censusBusyLocationIds + locationId, message = null)
            }
            runCatching { catalogFacade.censusSource(locationId) }
                .onSuccess { result ->
                    val count = catalogFacade.mediaItemCount()
                    val msg = result.message ?: (
                        "Censimento terminato: trovati ${result.mediaFound}, " +
                            "nuovi ${result.mediaAdded}, già noti ${result.mediaSkippedExisting}"
                        )
                    _state.update {
                        it.copy(
                            censusBusyLocationIds = it.censusBusyLocationIds - locationId,
                            catalogCount = count,
                            message = msg,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            censusBusyLocationIds = it.censusBusyLocationIds - locationId,
                            message = error.message ?: "Censimento non riuscito",
                        )
                    }
                }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CensusViewModel(catalogFacade) as T
                }
            }
    }
}
