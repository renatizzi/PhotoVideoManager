package com.renatizzi.photovideomanager.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val kindFilter: SearchKindFilter = SearchKindFilter.ALL,
    val entries: List<CatalogSearchEntry> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val message: String? = null,
) {
    /** Elenco UI: solo selezionati; se nessuno → vuoto (coerente con Importa). */
    val listedEntries: List<CatalogSearchEntry>
        get() = entries.filter { it.mediaItem.id in selectedIds }
}

class SearchViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        runSearch(cycleSelection = false)
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            runSearch(cycleSelection = false)
        }
    }

    /**
     * Chip Tutti / Foto / Video: filtra l’elenco e cicla i flag di selezione
     * degli elementi risultanti (tutti on → tutti off → tutti on …).
     */
    fun onKindFilter(filter: SearchKindFilter) {
        _state.update { it.copy(kindFilter = filter) }
        runSearch(cycleSelection = true)
    }

    fun toggleSelection(mediaItemId: String) {
        _state.update { current ->
            val next = current.selectedIds.toMutableSet()
            if (!next.add(mediaItemId)) next.remove(mediaItemId)
            current.copy(selectedIds = next)
        }
    }

    fun refresh() {
        runSearch(cycleSelection = false)
    }

    private fun runSearch(cycleSelection: Boolean) {
        val query = _state.value.query
        val filter = _state.value.kindFilter
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                catalogFacade.searchCatalog(query = query, kindFilter = filter)
            }.onSuccess { entries ->
                _state.update { current ->
                    val ids = entries.map { it.mediaItem.id }
                    val idSet = ids.toSet()
                    val selectedIds = if (cycleSelection) {
                        val allSelected = ids.isNotEmpty() &&
                            ids.all { it in current.selectedIds }
                        if (allSelected) emptySet() else idSet
                    } else if (current.selectedIds.isEmpty()) {
                        // Primo caricamento / refresh a selezione vuota: mostra tutto.
                        idSet
                    } else {
                        current.selectedIds.intersect(idSet)
                    }
                    current.copy(
                        entries = entries,
                        selectedIds = selectedIds,
                        loading = false,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        message = error.message ?: "Ricerca non riuscita",
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
                    return SearchViewModel(catalogFacade) as T
                }
            }
    }
}
