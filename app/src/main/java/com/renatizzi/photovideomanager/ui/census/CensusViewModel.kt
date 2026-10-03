package com.renatizzi.photovideomanager.ui.census

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSortMode
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CensusUiState(
    val sources: List<SourceSummary> = emptyList(),
    val selection: Map<String, SourceCensusSelection> = emptyMap(),
    val sortMode: SourceSortMode = SourceSortMode.NAME,
    val loading: Boolean = true,
    val censusBusyLocationIds: Set<String> = emptySet(),
    val catalogCount: Long = 0,
    val message: String? = null,
) {
    val visibleSources: List<SourceSummary>
        get() {
            val censable = sources.filterNot { it.isBuiltInPersonal }
            return when (sortMode) {
                SourceSortMode.NAME -> censable.sortedBy { it.displayName.lowercase() }
                SourceSortMode.DEVICE -> censable.sortedBy { it.deviceLabel.lowercase() }
                SourceSortMode.AVAILABILITY -> censable.sortedBy { it.availability.name }
            }
        }

    fun selectionOf(locationId: String): SourceCensusSelection =
        selection[locationId] ?: SourceCensusSelection.TO_SELECT
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
                val count = catalogFacade.mediaItemCount()
                sources to count
            }.onSuccess { (sources, count) ->
                _state.update { current ->
                    val kept = current.selection.filterKeys { id ->
                        sources.any { it.locationId == id && !it.isBuiltInPersonal }
                    }
                    current.copy(
                        sources = sources,
                        selection = kept,
                        catalogCount = count,
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

    fun setSortMode(mode: SourceSortMode) {
        _state.update { it.copy(sortMode = mode) }
    }

    fun toggleSelection(locationId: String) {
        _state.update { current ->
            val next = when (current.selectionOf(locationId)) {
                SourceCensusSelection.TO_SELECT -> SourceCensusSelection.SELECTED
                SourceCensusSelection.SELECTED -> SourceCensusSelection.REMOVED
                SourceCensusSelection.REMOVED -> SourceCensusSelection.SELECTED
            }
            current.copy(selection = current.selection + (locationId to next))
        }
    }

    fun addSafSource(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.registerSafFolder(uri, displayName) }
                .onSuccess { added ->
                    val sources = catalogFacade.listSources(refresh = true)
                    _state.update { current ->
                        current.copy(
                            sources = sources,
                            selection = current.selection +
                                (added.locationId to SourceCensusSelection.SELECTED),
                            loading = false,
                            message = "Sorgente aggiunta: ${added.displayName}",
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
            _state.update { it.copy(message = "Seleziona almeno una sorgente (✅) da censire") }
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
