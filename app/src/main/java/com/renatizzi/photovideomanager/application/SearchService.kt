package com.renatizzi.photovideomanager.application

import android.content.Context
import android.net.Uri
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
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
    private val appContext: Context,
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
                    locationNames = copies
                        .sortedBy { copy ->
                            if (copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID) 1 else 0
                        }
                        .mapNotNull { copy ->
                            formatLocationLabel(locations[copy.storageLocationId])
                        }
                        .distinct(),
                    inPersonalArchive = copies.any {
                        it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
                    },
                )
            }
            .sortedByDescending { it.mediaItem.updatedAtEpochMs }
            .take(limit)
            .toList()
    }

    /** Etichetta leggibile sorgente (anche per video nuovi). */
    private fun formatLocationLabel(location: StorageLocation?): String? {
        if (location == null) return null
        if (location.id == CatalogFacade.PERSONAL_LOCATION_ID) return "Spazio app"
        if (location.adapterKind != StorageAdapterKind.SAF_TREE || location.opaqueLocator.isBlank()) {
            return location.displayName.takeIf { it.isNotBlank() }
        }
        return runCatching {
            val uri = Uri.parse(location.opaqueLocator)
            val path = SafPathLabels.storagePathLabel(appContext, uri)
                .takeUnless { it.isBlank() || SafPathLabels.looksIllegible(it) }
            val name = location.displayName.takeUnless { SafPathLabels.looksIllegible(it) || it.isBlank() }
            when {
                !path.isNullOrBlank() && !name.isNullOrBlank() &&
                    !path.equals(name, ignoreCase = true) &&
                    !name.equals(path.substringAfterLast('/'), ignoreCase = true) ->
                    "$name · $path"
                !path.isNullOrBlank() -> path
                !name.isNullOrBlank() -> name
                else -> null
            }
        }.getOrNull() ?: location.displayName.takeIf { it.isNotBlank() }
    }

    companion object {
        /** Soft cap solo anti-OOM; non deve truncare cataloghi reali. */
        const val DEFAULT_LIMIT = 100_000
    }
}
