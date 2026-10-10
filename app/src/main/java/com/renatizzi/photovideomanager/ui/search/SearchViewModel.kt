package com.renatizzi.photovideomanager.ui.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.cycleNextCatalog
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
    /**
     * Selezione riga Aggiorna (3 stati). Default assente = [SourceCensusSelection.NOT_SELECTED]
     * così i filtri lavorano sull’elenco completo.
     */
    val selection: Map<String, SourceCensusSelection> = emptyMap(),
    val loading: Boolean = true,
    val message: String? = null,
) {
    /** Elenco UI Aggiorna: sempre tutte le voci del filtro corrente (non nasconde se deselezionate). */
    val listedEntries: List<CatalogSearchEntry>
        get() = entries

    fun selectionOf(mediaItemId: String): SourceCensusSelection =
        selection[mediaItemId] ?: SourceCensusSelection.NOT_SELECTED
}

class SearchViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        runSearch()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            runSearch()
        }
    }

    /** Chip Tutti / Foto / Video: filtra l’elenco; non cambia le selezioni di riga. */
    fun onKindFilter(filter: SearchKindFilter) {
        _state.update { it.copy(kindFilter = filter) }
        runSearch()
    }

    /** Ciclo Aggiorna: vuoto → ✓ → X → vuoto. */
    fun toggleSelection(mediaItemId: String) {
        _state.update { current ->
            val next = current.selectionOf(mediaItemId).cycleNextCatalog()
            current.copy(selection = current.selection + (mediaItemId to next))
        }
    }

    fun setSelection(mediaItemId: String, value: SourceCensusSelection) {
        _state.update { current ->
            current.copy(selection = current.selection + (mediaItemId to value))
        }
    }

    fun refresh() {
        runSearch()
    }

    fun renameMedia(mediaItemId: String, newTitle: String) {
        viewModelScope.launch {
            runCatching { catalogFacade.renameMediaTitle(mediaItemId, newTitle) }
                .onSuccess {
                    _state.update { it.copy(message = "Rinominato") }
                    runSearch()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Rinomina non riuscita")
                    }
                }
        }
    }

    fun trashMedia(mediaItemId: String) {
        viewModelScope.launch {
            runCatching { catalogFacade.trashMediaItem(mediaItemId) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            message = result.message
                                ?: "Spostato nel Cestino. Recupero: Utility → Ripristina.",
                            selection = it.selection - mediaItemId,
                        )
                    }
                    runSearch()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Eliminazione non riuscita")
                    }
                }
        }
    }

    fun exportMedia(mediaItemId: String, destUri: Uri) {
        viewModelScope.launch {
            catalogFacade.exportMediaItemToUri(mediaItemId, destUri)
                .onSuccess { name ->
                    _state.update { it.copy(message = "Copiato su dispositivo: $name") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Copia su dispositivo non riuscita")
                    }
                }
        }
    }

    fun suggestedExportFileName(mediaItemId: String): String {
        val entry = _state.value.entries.firstOrNull { it.mediaItem.id == mediaItemId }
        val title = entry?.mediaItem?.displayTitle?.ifBlank { null }
        if (title != null) return title
        val kind = entry?.mediaItem?.kind
        return when (kind) {
            MediaKind.VIDEO -> "video_${mediaItemId.takeLast(8)}.mp4"
            else -> "foto_${mediaItemId.takeLast(8)}.jpg"
        }
    }

    fun exportMimeType(mediaItemId: String): String {
        val entry = _state.value.entries.firstOrNull { it.mediaItem.id == mediaItemId }
        entry?.previewCopy?.mimeType?.takeIf { it.isNotBlank() }?.let { return it }
        return when (entry?.mediaItem?.kind) {
            MediaKind.VIDEO -> "video/*"
            else -> "image/*"
        }
    }

    private fun runSearch() {
        val query = _state.value.query
        val filter = _state.value.kindFilter
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                catalogFacade.searchCatalog(query = query, kindFilter = filter)
            }.onSuccess { entries ->
                _state.update { current ->
                    // Default Aggiorna: deselezionato; conserva solo stati già scelti dall’utente.
                    val selection = buildMap {
                        for (entry in entries) {
                            val id = entry.mediaItem.id
                            put(
                                id,
                                current.selection[id] ?: SourceCensusSelection.NOT_SELECTED,
                            )
                        }
                    }
                    current.copy(
                        entries = entries,
                        selection = selection,
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
