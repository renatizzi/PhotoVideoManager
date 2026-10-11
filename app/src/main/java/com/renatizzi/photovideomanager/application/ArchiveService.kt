package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.ArchiveEntry
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.ScrapCandidate
import com.renatizzi.photovideomanager.domain.policy.ScrapHeuristics
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate

/**
 * Archivia v1: elenco contenuti attivi nello spazio personale dell'app.
 */
class ArchiveService(
    private val catalogStore: CatalogStore,
    private val permissionGate: PermissionGate,
) {
    suspend fun listPersonalArchive(): List<ArchiveEntry> {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        return catalogStore.listAllMediaCopies()
            .filter {
                it.state == MediaCopyState.ACTIVE &&
                    it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
            }
            .mapNotNull { copy ->
                val item = items[copy.mediaItemId] ?: return@mapNotNull null
                ArchiveEntry(mediaItem = item, mediaCopy = copy)
            }
            .sortedByDescending { it.mediaCopy.createdAtEpochMs }
    }

    suspend fun listMediaForLocation(locationId: String): List<ArchiveEntry> {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        return catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE && it.storageLocationId == locationId }
            .mapNotNull { copy ->
                val item = items[copy.mediaItemId] ?: return@mapNotNull null
                ArchiveEntry(mediaItem = item, mediaCopy = copy)
            }
            .sortedByDescending { it.mediaCopy.createdAtEpochMs }
    }

    suspend fun renameMediaTitle(mediaItemId: String, newTitle: String) {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val clean = newTitle.trim()
        require(clean.isNotEmpty()) { "Nome non valido" }
        val item = catalogStore.getMediaItem(mediaItemId)
            ?: error("Elemento non trovato")
        catalogStore.upsertMediaItem(
            item.copy(
                displayTitle = clean,
                updatedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    /**
     * BL-01: possibili scarti nel Catalogo (copie ACTIVE).
     * Preferisce la copia nello spazio app per titolo/dimensione.
     */
    suspend fun listPossibleScraps(): List<ScrapCandidate> {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val active = catalogStore.listMediaCopiesByState(MediaCopyState.ACTIVE)
            .groupBy { it.mediaItemId }
        val out = ArrayList<ScrapCandidate>()
        for ((itemId, copies) in active) {
            val item = items[itemId] ?: continue
            val preferred = copies.firstOrNull {
                it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
            } ?: copies.first()
            val title = item.displayTitle?.ifBlank { null }
                ?: preferred.opaqueLocator.substringAfterLast('/')
                    .substringAfterLast(':')
            if (!ScrapHeuristics.isPossibleScrap(title, preferred.byteSize)) continue
            out.add(
                ScrapCandidate(
                    mediaItem = item,
                    previewCopy = preferred,
                    reason = ScrapHeuristics.reasonLabel(title, preferred.byteSize),
                ),
            )
        }
        return out.sortedBy { it.mediaItem.displayTitle.orEmpty().lowercase() }
    }
}
