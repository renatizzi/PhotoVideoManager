package com.renatizzi.photovideomanager.ui.acquire

import com.renatizzi.photovideomanager.domain.model.AcquireCandidate

/**
 * Regole Importa (testabili senza Android):
 * - si acquisiscono solo i ✓ su elementi non già in Catalogo;
 * - i deselezionati (non già in Catalogo) vanno scartati dal Catalogo.
 */
object AcquireSelectionLogic {
    fun idsToAcquire(
        candidates: List<AcquireCandidate>,
        selectedIds: Set<String>,
    ): Set<String> = selectedIds.filter { id ->
        candidates.any { c -> c.mediaItem.id == id && !c.alreadyInPersonalArchive }
    }.toSet()

    fun idsToDiscard(
        candidates: List<AcquireCandidate>,
        selectedIds: Set<String>,
    ): List<String> {
        val acquire = idsToAcquire(candidates, selectedIds)
        return candidates
            .filter { !it.alreadyInPersonalArchive && it.mediaItem.id !in acquire }
            .map { it.mediaItem.id }
    }
}
