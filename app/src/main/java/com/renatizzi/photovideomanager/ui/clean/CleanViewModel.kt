package com.renatizzi.photovideomanager.ui.clean

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.ScrapCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CleanUiState(
    /** Id selezionati (✓) portati da Aggiorna — BL-12. */
    val selectedIds: Set<String> = emptySet(),
    val groups: List<DuplicateGroup> = emptyList(),
    /** BL-01: possibili scarti proposti. */
    val scraps: List<ScrapCandidate> = emptyList(),
    val scrapSelectedIds: Set<String> = emptySet(),
    val scrapsLoaded: Boolean = false,
    val analyzing: Boolean = false,
    val busy: Boolean = false,
    val copiesScanned: Int = 0,
    val hashesComputed: Int = 0,
    val hashesFailed: Int = 0,
    val hasAnalyzed: Boolean = false,
    val pendingTrashGroup: DuplicateGroup? = null,
    val pendingTrashSelected: Boolean = false,
    val pendingTrashScraps: Boolean = false,
    val message: String? = null,
) {
    val selectedCount: Int get() = selectedIds.size
    val scrapSelectedCount: Int get() = scrapSelectedIds.size
}

class CleanViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CleanUiState())
    val state: StateFlow<CleanUiState> = _state.asStateFlow()

    init {
        refreshScraps()
    }

    /** Chiamato all’ingresso in Pulisci con i ✓ di Aggiorna. */
    fun setSelectedFromAggiorna(ids: Set<String>) {
        _state.update { it.copy(selectedIds = ids, pendingTrashSelected = false) }
        refreshScraps()
    }

    fun refreshScraps() {
        viewModelScope.launch {
            runCatching { catalogFacade.listPossibleScraps() }
                .onSuccess { list ->
                    _state.update { current ->
                        val still = current.scrapSelectedIds.filter { id ->
                            list.any { it.mediaItem.id == id }
                        }.toSet()
                        current.copy(
                            scraps = list,
                            scrapsLoaded = true,
                            scrapSelectedIds = still,
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(scrapsLoaded = true, scraps = emptyList()) }
                }
        }
    }

    fun toggleScrap(mediaItemId: String) {
        _state.update { current ->
            val next = current.scrapSelectedIds.toMutableSet()
            if (!next.add(mediaItemId)) next.remove(mediaItemId)
            current.copy(scrapSelectedIds = next)
        }
    }

    fun selectAllScraps(selected: Boolean) {
        _state.update { current ->
            current.copy(
                scrapSelectedIds = if (selected) {
                    current.scraps.map { it.mediaItem.id }.toSet()
                } else {
                    emptySet()
                },
            )
        }
    }

    fun requestTrashScraps() {
        if (_state.value.scrapSelectedIds.isEmpty()) return
        _state.update {
            it.copy(
                pendingTrashScraps = true,
                pendingTrashSelected = false,
                pendingTrashGroup = null,
            )
        }
    }

    fun dismissTrashScraps() {
        _state.update { it.copy(pendingTrashScraps = false) }
    }

    fun confirmTrashScraps(deleteSourceFiles: Boolean = false) {
        val ids = _state.value.scrapSelectedIds.toList()
        if (ids.isEmpty()) {
            _state.update { it.copy(pendingTrashScraps = false) }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(busy = true, pendingTrashScraps = false, message = null)
            }
            var ok = 0
            var fail = 0
            for (id in ids) {
                runCatching {
                    catalogFacade.trashMediaItem(
                        mediaItemId = id,
                        deleteSourceFiles = deleteSourceFiles,
                    )
                }
                    .onSuccess { ok++ }
                    .onFailure { fail++ }
            }
            val scraps = runCatching { catalogFacade.listPossibleScraps() }.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    busy = false,
                    scraps = scraps,
                    scrapSelectedIds = emptySet(),
                    message = when {
                        ok > 0 && fail == 0 ->
                            "Scarti nel Cestino: $ok. Recupero: Utility → Ripristina."
                        ok > 0 ->
                            "Scarti nel Cestino: $ok · non riusciti: $fail."
                        else ->
                            "Eliminazione scarti non riuscita"
                    },
                )
            }
        }
    }

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(analyzing = true, message = null) }
            runCatching { catalogFacade.analyzeExactDuplicates() }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            analyzing = false,
                            hasAnalyzed = true,
                            groups = result.groups,
                            copiesScanned = result.copiesScanned,
                            hashesComputed = result.hashesComputed,
                            hashesFailed = result.hashesFailed,
                            message = null,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            analyzing = false,
                            message = error.message ?: "Analisi non riuscita",
                        )
                    }
                }
        }
    }

    fun requestTrashSelected() {
        if (_state.value.selectedIds.isEmpty()) return
        _state.update {
            it.copy(
                pendingTrashSelected = true,
                pendingTrashGroup = null,
                pendingTrashScraps = false,
            )
        }
    }

    fun dismissTrashSelected() {
        _state.update { it.copy(pendingTrashSelected = false) }
    }

    fun confirmTrashSelected(deleteSourceFiles: Boolean = false) {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) {
            _state.update { it.copy(pendingTrashSelected = false) }
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(busy = true, pendingTrashSelected = false, message = null)
            }
            var ok = 0
            var fail = 0
            for (id in ids) {
                runCatching {
                    catalogFacade.trashMediaItem(
                        mediaItemId = id,
                        deleteSourceFiles = deleteSourceFiles,
                    )
                }
                    .onSuccess { ok++ }
                    .onFailure { fail++ }
            }
            val scraps = runCatching { catalogFacade.listPossibleScraps() }.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    busy = false,
                    selectedIds = emptySet(),
                    scraps = scraps,
                    message = when {
                        ok > 0 && fail == 0 ->
                            "Spostati nel Cestino: $ok. Recupero: Utility → Ripristina."
                        ok > 0 ->
                            "Spostati nel Cestino: $ok · non riusciti: $fail."
                        else ->
                            "Eliminazione selezionati non riuscita"
                    },
                )
            }
        }
    }

    fun requestTrashExtras(group: DuplicateGroup) {
        _state.update {
            it.copy(
                pendingTrashGroup = group,
                pendingTrashSelected = false,
                pendingTrashScraps = false,
            )
        }
    }

    fun dismissTrashConfirm() {
        _state.update { it.copy(pendingTrashGroup = null) }
    }

    fun confirmTrashExtras(deleteSourceFiles: Boolean = false) {
        val group = _state.value.pendingTrashGroup ?: return
        val keepId = group.members.firstOrNull { it.isSuggestedKeep }?.mediaItem?.id
            ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, pendingTrashGroup = null, message = null) }
            runCatching {
                catalogFacade.trashDuplicateExtras(
                    fingerprintValue = group.fingerprintValue,
                    keepMediaItemId = keepId,
                    memberCopyIds = group.members.map { it.mediaCopy.id },
                    deleteSourceFiles = deleteSourceFiles,
                )
            }.onSuccess { result ->
                val refreshed = catalogFacade.analyzeExactDuplicates()
                val scraps = runCatching { catalogFacade.listPossibleScraps() }.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        busy = false,
                        groups = refreshed.groups,
                        copiesScanned = refreshed.copiesScanned,
                        hashesComputed = refreshed.hashesComputed,
                        hashesFailed = refreshed.hashesFailed,
                        hasAnalyzed = true,
                        scraps = scraps,
                        message = result.message
                            ?: "Duplicati nel Cestino. Recupero: Utility → Ripristina.",
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(busy = false, message = error.message ?: "Operazione non riuscita")
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
                    return CleanViewModel(catalogFacade) as T
                }
            }
    }
}
