package com.renatizzi.photovideomanager.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrashUiState(
    val entries: List<TrashEntry> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val pendingPurgeAll: Boolean = false,
    val message: String? = null,
)

class TrashViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(TrashUiState())
    val state: StateFlow<TrashUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.listTrash() }
                .onSuccess { entries ->
                    _state.update { it.copy(entries = entries, loading = false) }
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

    fun restore(copyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            runCatching { catalogFacade.restoreFromTrash(copyId) }
                .onSuccess { result ->
                    val entries = catalogFacade.listTrash()
                    _state.update {
                        it.copy(busy = false, entries = entries, message = result.message)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(busy = false, message = error.message ?: "Ripristino non riuscito")
                    }
                }
        }
    }

    fun purge(copyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, message = null) }
            runCatching { catalogFacade.purgeFromTrash(copyId) }
                .onSuccess { result ->
                    val entries = catalogFacade.listTrash()
                    _state.update {
                        it.copy(busy = false, entries = entries, message = result.message)
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
                        it.copy(busy = false, entries = entries, message = result.message)
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
