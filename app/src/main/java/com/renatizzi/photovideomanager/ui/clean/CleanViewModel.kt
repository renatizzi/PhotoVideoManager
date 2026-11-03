package com.renatizzi.photovideomanager.ui.clean

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.DuplicateMember
import com.renatizzi.photovideomanager.domain.model.ScrapCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DuplicateAgeFilter {
    /** Seleziona i più vecchi (tiene il più recente). */
    OLDER,
    /** Seleziona i più recenti (tiene il più vecchio). */
    NEWER,
}

data class CleanUiState(
    /** Id selezionati (✓) portati da Aggiorna — BL-12. */
    val selectedIds: Set<String> = emptySet(),
    val groups: List<DuplicateGroup> = emptyList(),
    /** BL-01: possibili scarti proposti. */
    val scraps: List<ScrapCandidate> = emptyList(),
    val scrapSelectedIds: Set<String> = emptySet(),
    /** Duplicati selezionati per il Cestino (override manuale sul filtro età). */
    val duplicateSelectedIds: Set<String> = emptySet(),
    val duplicateAgeFilter: DuplicateAgeFilter = DuplicateAgeFilter.OLDER,
    val scrapsLoaded: Boolean = false,
    val analyzing: Boolean = false,
    val busy: Boolean = false,
    val copiesScanned: Int = 0,
    val hashesComputed: Int = 0,
    val hashesFailed: Int = 0,
    val hasAnalyzed: Boolean = false,
    val pendingTrash: Boolean = false,
    val message: String? = null,
) {
    val selectedCount: Int get() = selectedIds.size
    val scrapSelectedCount: Int get() = scrapSelectedIds.size
    val duplicateSelectedCount: Int get() = duplicateSelectedIds.size
    val totalPendingTrashCount: Int
        get() = selectedCount + scrapSelectedCount + duplicateSelectedCount
    val duplicateMemberCount: Int
        get() = groups.sumOf { it.members.size }
}

class CleanViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CleanUiState())
    val state: StateFlow<CleanUiState> = _state.asStateFlow()

    init {
        // Nota bis: analisi al lancio di Pulisci (niente bottone «Analizza duplicati»).
        refreshScraps()
        analyze()
    }

    /** Chiamato all’ingresso in Pulisci con i ✓ di Aggiorna. */
    fun setSelectedFromAggiorna(ids: Set<String>) {
        _state.update { it.copy(selectedIds = ids, pendingTrash = false) }
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

    fun toggleDuplicate(mediaItemId: String) {
        _state.update { current ->
            val next = current.duplicateSelectedIds.toMutableSet()
            if (!next.add(mediaItemId)) next.remove(mediaItemId)
            current.copy(duplicateSelectedIds = next)
        }
    }

    fun setDuplicateAgeFilter(filter: DuplicateAgeFilter) {
        _state.update { current ->
            current.copy(
                duplicateAgeFilter = filter,
                duplicateSelectedIds = selectByAgeFilter(current.groups, filter),
            )
        }
    }

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(analyzing = true, message = null) }
            runCatching { catalogFacade.analyzeExactDuplicates() }
                .onSuccess { result ->
                    _state.update { current ->
                        val filter = current.duplicateAgeFilter
                        current.copy(
                            analyzing = false,
                            hasAnalyzed = true,
                            groups = result.groups,
                            copiesScanned = result.copiesScanned,
                            hashesComputed = result.hashesComputed,
                            hashesFailed = result.hashesFailed,
                            duplicateSelectedIds = selectByAgeFilter(result.groups, filter),
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

    fun requestTrashPending() {
        if (_state.value.totalPendingTrashCount <= 0) return
        _state.update { it.copy(pendingTrash = true) }
    }

    fun dismissTrashPending() {
        _state.update { it.copy(pendingTrash = false) }
    }

    fun confirmTrashPending(deleteSourceFiles: Boolean = false) {
        val snapshot = _state.value
        val ids = linkedSetOf<String>().apply {
            addAll(snapshot.selectedIds)
            addAll(snapshot.scrapSelectedIds)
            addAll(snapshot.duplicateSelectedIds)
        }
        if (ids.isEmpty()) {
            _state.update { it.copy(pendingTrash = false) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, pendingTrash = false, message = null) }
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
            val refreshed = runCatching { catalogFacade.analyzeExactDuplicates() }.getOrNull()
            _state.update { current ->
                val groups = refreshed?.groups ?: emptyList()
                current.copy(
                    busy = false,
                    selectedIds = emptySet(),
                    scraps = scraps,
                    scrapSelectedIds = emptySet(),
                    groups = groups,
                    copiesScanned = refreshed?.copiesScanned ?: current.copiesScanned,
                    hashesComputed = refreshed?.hashesComputed ?: current.hashesComputed,
                    hashesFailed = refreshed?.hashesFailed ?: current.hashesFailed,
                    hasAnalyzed = true,
                    duplicateSelectedIds = selectByAgeFilter(groups, current.duplicateAgeFilter),
                    message = when {
                        ok > 0 && fail == 0 ->
                            "Spostati nel Cestino: $ok. Recupero: Utility → Ripristina."
                        ok > 0 ->
                            "Spostati nel Cestino: $ok · non riusciti: $fail."
                        else ->
                            "Eliminazione non riuscita"
                    },
                )
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun selectByAgeFilter(
            groups: List<DuplicateGroup>,
            filter: DuplicateAgeFilter,
        ): Set<String> {
            val selected = linkedSetOf<String>()
            for (group in groups) {
                val byItem = group.members
                    .groupBy { it.mediaItem.id }
                    .mapValues { (_, members) -> members.first() }
                if (byItem.size < 2) continue
                val ordered = byItem.values.sortedBy { memberAgeEpochMs(it) }
                val keep = when (filter) {
                    // Più vecchi da eliminare → tiene il più recente
                    DuplicateAgeFilter.OLDER -> ordered.last()
                    // Più recenti da eliminare → tiene il più vecchio
                    DuplicateAgeFilter.NEWER -> ordered.first()
                }
                for (member in ordered) {
                    if (member.mediaItem.id != keep.mediaItem.id) {
                        selected += member.mediaItem.id
                    }
                }
            }
            return selected
        }

        /** Id da tenere (non cestinare) secondo il filtro età corrente. */
        fun keepIdsForFilter(
            groups: List<DuplicateGroup>,
            filter: DuplicateAgeFilter,
        ): Set<String> {
            val selected = selectByAgeFilter(groups, filter)
            return groups.flatMap { g -> g.members.map { it.mediaItem.id } }
                .toSet() - selected
        }

        private fun memberAgeEpochMs(member: DuplicateMember): Long =
            member.mediaItem.capturedAtEpochMs
                ?: member.mediaCopy.createdAtEpochMs
                ?: member.mediaItem.createdAtEpochMs

        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CleanViewModel(catalogFacade) as T
                }
            }
    }
}
