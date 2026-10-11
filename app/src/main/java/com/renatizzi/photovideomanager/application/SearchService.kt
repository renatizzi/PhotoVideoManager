package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate

/**
 * Ricerca v1: trova MediaItem con almeno una copia ACTIVE nel Catalogo.
 * Filtro testo su titolo (o id) e filtro tipo foto/video.
 *
 * Nessun tetto artificiale basso: Aggiorna/Home devono poter mostrare
 * l’intero Catalogo (censimenti tipici >1000 elementi).
 */
class SearchService(
    private val catalogStore: CatalogStore,
    private val permissionGate: PermissionGate,
) {
    suspend fun search(
        query: String,
        kindFilter: SearchKindFilter = SearchKindFilter.ALL,
        limit: Int = DEFAULT_LIMIT,
    ): List<CatalogSearchEntry> {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        val activeCopies = catalogStore.listMediaCopiesByState(MediaCopyState.ACTIVE)
        val copiesByItem = activeCopies.groupBy { it.mediaItemId }
        val needle = query.trim().lowercase()
        return catalogStore.listAllMediaItems()
            .asSequence()
            .filter { item -> copiesByItem.containsKey(item.id) }
            .filter { item ->
                when (kindFilter) {
                    SearchKindFilter.ALL -> true
                    SearchKindFilter.PHOTO -> item.kind == MediaKind.PHOTO
                    SearchKindFilter.VIDEO -> item.kind == MediaKind.VIDEO
                }
            }
            .filter { item ->
                if (needle.isEmpty()) return@filter true
                val title = item.displayTitle?.lowercase().orEmpty()
                title.contains(needle) || item.id.lowercase().contains(needle)
            }
            .map { item ->
                val copies = copiesByItem.getValue(item.id)
                val preferred = copies.firstOrNull {
                    it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
                } ?: copies.first()
                CatalogSearchEntry(
                    mediaItem = item,
                    previewCopy = preferred,
                    activeCopyCount = copies.size,
                    locationNames = copies.mapNotNull { copy ->
                        locations[copy.storageLocationId]?.displayName
                    }.distinct(),
                    inPersonalArchive = copies.any {
                        it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
                    },
                )
            }
            .sortedByDescending { it.mediaItem.updatedAtEpochMs }
            .take(limit)
            .toList()
    }

    companion object {
        /** Soft cap solo anti-OOM; non deve truncare cataloghi reali. */
        const val DEFAULT_LIMIT = 100_000
    }
}
