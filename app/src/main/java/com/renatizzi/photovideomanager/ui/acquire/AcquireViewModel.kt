package com.renatizzi.photovideomanager.ui.acquire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcquireUiState(
    val candidates: List<AcquireCandidate> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val acquiring: Boolean = false,
    val catalogCount: Long = 0,
    val message: String? = null,
)

class AcquireViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(AcquireUiState())
    val state: StateFlow<AcquireUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                val candidates = catalogFacade.listAcquireCandidates()
                val count = catalogFacade.mediaItemCount()
                candidates to count
            }.onSuccess { (candidates, count) ->
                val pendingIds = candidates
                    .filterNot { it.alreadyInPersonalArchive }
                    .map { it.mediaItem.id }
                    .toSet()
                _state.update {
                    it.copy(
                        candidates = candidates,
                        selectedIds = pendingIds,
                        catalogCount = count,
                        loading = false,
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

    fun selectPendingOnly() {
        _state.update { current ->
            current.copy(
                selectedIds = current.candidates
                    .filterNot { it.alreadyInPersonalArchive }
                    .map { it.mediaItem.id }
                    .toSet(),
            )
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedIds = emptySet()) }
    }

    fun acquireSelected() {
        val ids = _state.value.selectedIds
        if (ids.isEmpty()) {
            _state.update { it.copy(message = "Seleziona almeno un elemento") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(acquiring = true, message = null) }
            runCatching { catalogFacade.acquireToPersonalArchive(ids) }
                .onSuccess { result ->
                    val candidates = catalogFacade.listAcquireCandidates()
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
                    _state.update {
                        it.copy(
                            acquiring = false,
                            candidates = candidates,
                            selectedIds = candidates
                                .filterNot { c -> c.alreadyInPersonalArchive }
                                .map { c -> c.mediaItem.id }
                                .toSet(),
                            catalogCount = count,
                            message = msg,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            acquiring = false,
                            message = error.message ?: "Acquisizione non riuscita",
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
                    return AcquireViewModel(catalogFacade) as T
                }
            }
    }
}
