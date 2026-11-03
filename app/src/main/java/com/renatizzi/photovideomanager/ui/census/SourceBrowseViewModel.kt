package com.renatizzi.photovideomanager.ui.census

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.application.LiveMediaPreview
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
    /** Stesso contenuto della riga in Acquisisci. */
    val displayName: String = "",
    val deviceLabel: String = "",
    val pathLabel: String = "",
    /** Memoria secondaria → «SD» in Dettaglio. */
    val isSecondaryVolume: Boolean = false,
    /** Sottocartelle immediate — non mostrate in Dettaglio (NOVITÀ: riga=media). */
    val childFolders: List<String> = emptyList(),
    val entries: List<ArchiveEntry> = emptyList(),
    /** Anteprima SAF se il Catalogo non ha ancora copie (prima di Conferma). */
    val livePreviews: List<LiveMediaPreview> = emptyList(),
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

    val visibleLivePreviews: List<LiveMediaPreview>
        get() = when (sortMode) {
            MediaBrowseSortMode.NAME -> livePreviews.sortedBy { it.displayName.lowercase() }
            MediaBrowseSortMode.KIND -> livePreviews.sortedWith(
                compareBy<LiveMediaPreview> { it.kind.name }.thenBy { it.displayName },
            )
            MediaBrowseSortMode.NEWEST -> livePreviews
        }

    val itemCount: Int
        get() = if (entries.isNotEmpty()) entries.size else livePreviews.size

    val showingLivePreview: Boolean
        get() = entries.isEmpty() && livePreviews.isNotEmpty()
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
                val folders = catalogFacade.listImmediateChildFolders(locationId)
                val live = if (entries.isEmpty()) {
                    catalogFacade.listLiveMediaPreview(locationId)
                } else {
                    emptyList()
                }
                BrowsePayload(source, entries, folders, live)
            }.onSuccess { payload ->
                _state.update {
                    it.copy(
                        displayName = payload.source?.displayName.orEmpty(),
                        deviceLabel = payload.source?.deviceLabel.orEmpty(),
                        pathLabel = payload.source?.pathLabel.orEmpty(),
                        isSecondaryVolume = payload.source?.isSecondaryVolume == true,
                        childFolders = payload.folders,
                        entries = payload.entries,
                        livePreviews = payload.live,
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

private data class BrowsePayload(
    val source: com.renatizzi.photovideomanager.domain.model.SourceSummary?,
    val entries: List<ArchiveEntry>,
    val folders: List<String>,
    val live: List<LiveMediaPreview>,
)
