package com.renatizzi.photovideomanager.ui.census

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.ArchiveEntry
import com.renatizzi.photovideomanager.domain.model.MediaBrowseSortMode
import com.renatizzi.photovideomanager.domain.model.MediaKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SourceBrowseUiState(
    val locationId: String = "",
    val title: String = "",
    val entries: List<ArchiveEntry> = emptyList(),
    val sortMode: MediaBrowseSortMode = MediaBrowseSortMode.NEWEST,
    val loading: Boolean = true,
    val message: String? = null,
) {
    val visibleEntries: List<ArchiveEntry>
        get() = when (sortMode) {
            MediaBrowseSortMode.NAME -> entries.sortedBy {
                it.mediaItem.displayTitle?.lowercase() ?: it.mediaItem.id
            }
            MediaBrowseSortMode.KIND -> entries.sortedWith(
                compareBy<ArchiveEntry> { it.mediaItem.kind.name }
                    .thenBy { it.mediaItem.displayTitle.orEmpty() },
            )
            MediaBrowseSortMode.NEWEST -> entries.sortedByDescending {
                it.mediaItem.updatedAtEpochMs
            }
        }
}

class SourceBrowseViewModel(
    private val catalogFacade: CatalogFacade,
    private val locationId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(SourceBrowseUiState(locationId = locationId))
    val state: StateFlow<SourceBrowseUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                val sources = catalogFacade.listSources(refresh = false)
                val source = sources.firstOrNull { it.locationId == locationId }
                val entries = catalogFacade.listMediaForLocation(locationId)
                Triple(source?.displayName ?: locationId, source?.pathLabel.orEmpty(), entries)
            }.onSuccess { (title, path, entries) ->
                _state.update {
                    it.copy(
                        title = if (path.isBlank()) title else "$title · $path",
                        entries = entries,
                        loading = false,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        message = error.message ?: "Impossibile caricare i media",
                    )
                }
            }
        }
    }

    fun setSortMode(mode: MediaBrowseSortMode) {
        _state.update { it.copy(sortMode = mode) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade, locationId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SourceBrowseViewModel(catalogFacade, locationId) as T
                }
            }
    }
}
