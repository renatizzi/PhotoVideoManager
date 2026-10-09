package com.renatizzi.photovideomanager.ui.acquire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcquireUiState(
    val candidates: List<AcquireCandidate> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val kindFilter: SearchKindFilter = SearchKindFilter.ALL,
    val sourceLocationIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val acquiring: Boolean = false,
    val catalogCount: Long = 0,
    val message: String? = null,
) {
    /** Pool filtrato per tipo (Tutti/Foto/Video). */
    val kindFiltered: List<AcquireCandidate>
        get() = candidates.filter { candidate ->
            when (kindFilter) {
                SearchKindFilter.ALL -> true
                SearchKindFilter.PHOTO -> candidate.mediaItem.kind == MediaKind.PHOTO
                SearchKindFilter.VIDEO -> candidate.mediaItem.kind == MediaKind.VIDEO
            }
        }

    @Deprecated("Use kindFiltered", ReplaceWith("kindFiltered"))
    val visibleCandidates: List<AcquireCandidate>
        get() = kindFiltered

    /**
     * Elenco mostrato: solo elementi selezionati (o già in archivio).
     * Se tutto deselezionato → lista vuota (coerente col filtro a zero risultati).
     */
    val listedCandidates: List<AcquireCandidate>
        get() = kindFiltered.filter {
            it.mediaItem.id in selectedIds || it.alreadyInPersonalArchive
        }
}

class AcquireViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(AcquireUiState())
    val state: StateFlow<AcquireUiState> = _state.asStateFlow()

    init {
        refresh(null)
    }

    fun refresh(sourceLocationIds: Set<String>? = _state.value.sourceLocationIds.takeIf { it.isNotEmpty() }) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                val candidates = catalogFacade.listAcquireCandidates(
                    sourceLocationIds = sourceLocationIds,
                )
                val count = catalogFacade.mediaItemCount()
                Triple(candidates, count, sourceLocationIds.orEmpty())
            }.onSuccess { (candidates, count, sources) ->
                _state.update { current ->
                    val next = current.copy(
                        candidates = candidates,
                        catalogCount = count,
                        sourceLocationIds = sources,
                        loading = false,
                    )
                    next.copy(
                        selectedIds = next.kindFiltered
                            .filterNot { it.alreadyInPersonalArchive }
                            .map { it.mediaItem.id }
                            .toSet(),
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        message = error.message ?: "Errore nel caricamento dei candidati",
                    )
                }
            }
        }
    }

    fun toggleSelection(mediaItemId: String) {
        _state.update { current ->
            val next = current.selectedIds.toMutableSet()
            if (!next.add(mediaItemId)) next.remove(mediaItemId)
            current.copy(selectedIds = next)
        }
    }

    /**
     * Chip Tutti / Foto / Video: applica il filtro e cicla la selezione dei
     * candidati del pool (tutti flag → nessuno → tutti …).
     * Con selezione vuota l’elenco risulta vuoto.
     */
    fun onKindFilter(filter: SearchKindFilter) {
        _state.update { current ->
            val next = current.copy(kindFilter = filter)
            val selectableIds = next.kindFiltered
                .filterNot { it.alreadyInPersonalArchive }
                .map { it.mediaItem.id }
            val allSelected = selectableIds.isNotEmpty() &&
                selectableIds.all { it in current.selectedIds }
            next.copy(
                selectedIds = if (allSelected) emptySet() else selectableIds.toSet(),
            )
        }
    }

    fun selectPendingOnly() {
        _state.update { current ->
            current.copy(
                selectedIds = current.kindFiltered
                    .filterNot { it.alreadyInPersonalArchive }
                    .map { it.mediaItem.id }
                    .toSet(),
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun acquireSelected(onDone: (Boolean) -> Unit = {}) {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) {
            _state.update { it.copy(message = "Seleziona almeno un elemento") }
            onDone(false)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(acquiring = true, message = null) }
            runCatching { catalogFacade.acquireToPersonalArchive(ids) }
                .onSuccess { result ->
                    val sources = _state.value.sourceLocationIds.takeIf { it.isNotEmpty() }
                    val candidates = catalogFacade.listAcquireCandidates(sourceLocationIds = sources)
                    val count = catalogFacade.mediaItemCount()
                    val msg = result.message ?: buildString {
                        append("Acquisizione terminata: ")
                        append("${result.acquired} copiati")
                        if (result.skippedAlreadyPresent > 0) {
                            append(", ${result.skippedAlreadyPresent} già presenti")
                        }
                        if (result.failed > 0) {
                            append(", ${result.failed} non riusciti")
                        }
                    }
                    _state.update { current ->
                        val next = current.copy(
                            acquiring = false,
                            candidates = candidates,
                            catalogCount = count,
                            message = msg,
                        )
                        next.copy(
                            selectedIds = next.kindFiltered
                                .filterNot { c -> c.alreadyInPersonalArchive }
                                .map { c -> c.mediaItem.id }
                                .toSet(),
                        )
                    }
                    onDone(result.acquired > 0 || result.skippedAlreadyPresent > 0)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            acquiring = false,
                            message = error.message ?: "Acquisizione non riuscita",
                        )
                    }
                    onDone(false)
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
                    return AcquireViewModel(catalogFacade) as T
                }
            }
    }
}
