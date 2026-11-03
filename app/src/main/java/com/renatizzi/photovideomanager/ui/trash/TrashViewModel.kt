package com.renatizzi.photovideomanager.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrashUiState(
    val entries: List<TrashEntry> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val pendingPurgeAll: Boolean = false,
    val message: String? = null,
) {
    val selectedCount: Int get() = selectedIds.size
}

class TrashViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(TrashUiState())
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

    /** Dopo ripristino riuscito → aprire Aggiorna per vedere subito i file. */
    private val _goToAggiorna = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val goToAggiorna: SharedFlow<Unit> = _goToAggiorna.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.listTrash() }
                .onSuccess { entries ->
                    _state.update { current ->
                        val still = current.selectedIds.filter { id ->
                            entries.any { it.mediaItem.id == id }
                        }.toSet()
                        current.copy(
                            entries = entries,
                            selectedIds = still.ifEmpty {
                                entries.map { it.mediaItem.id }.toSet()
                            },
                            loading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            message = error.message ?: "Impossibile caricare il Cestino",
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

    fun restoreSelected() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            var ok = 0
            var fail = 0
            for (id in ids) {
                runCatching { catalogFacade.restoreFromTrash(id) }
                    .onSuccess { ok++ }
                    .onFailure { fail++ }
            }
            val entries = catalogFacade.listTrash()
            _state.update {
                it.copy(
                    busy = false,
                    entries = entries,
                    selectedIds = emptySet(),
                    message = when {
                        ok > 0 && fail == 0 -> "Ripristinati nel Catalogo: $ok"
                        ok > 0 -> "Ripristinati: $ok · non riusciti: $fail"
                        else -> "Ripristino non riuscito"
                    },
                )
            }
            if (ok > 0) _goToAggiorna.tryEmit(Unit)
        }
    }

    fun restore(mediaItemId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            runCatching { catalogFacade.restoreFromTrash(mediaItemId) }
                .onSuccess { result ->
                    val entries = catalogFacade.listTrash()
                    _state.update {
                        it.copy(
                            busy = false,
                            entries = entries,
                            selectedIds = it.selectedIds - mediaItemId,
                            message = result.message,
                        )
                    }
                    if (result.affected > 0) _goToAggiorna.tryEmit(Unit)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(busy = false, message = error.message ?: "Ripristino non riuscito")
                    }
                }
        }
    }

    fun purge(mediaItemId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            runCatching { catalogFacade.purgeFromTrash(mediaItemId) }
                .onSuccess { result ->
                    val entries = catalogFacade.listTrash()
                    _state.update {
                        it.copy(
                            busy = false,
                            entries = entries,
                            selectedIds = it.selectedIds - mediaItemId,
                            message = result.message,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(busy = false, message = error.message ?: "Eliminazione non riuscita")
                    }
                }
        }
    }

    fun requestEmptyTrash() {
        _state.update { it.copy(pendingPurgeAll = true) }
    }

    fun dismissEmptyTrash() {
        _state.update { it.copy(pendingPurgeAll = false) }
    }

    fun confirmEmptyTrash() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, pendingPurgeAll = false, message = null) }
            runCatching { catalogFacade.emptyTrash() }
                .onSuccess { result ->
                    val entries = catalogFacade.listTrash()
                    _state.update {
                        it.copy(
                            busy = false,
                            entries = entries,
                            selectedIds = emptySet(),
                            message = result.message,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(busy = false, message = error.message ?: "Svuotamento non riuscito")
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
                    return TrashViewModel(catalogFacade) as T
                }
            }
    }
}
