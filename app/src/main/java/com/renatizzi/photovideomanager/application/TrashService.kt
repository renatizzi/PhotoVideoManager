package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.model.TrashActionResult
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate

/**
 * Cestino catalogo (soft-delete).
 * - SAF/esterni: solo stato TRASHED nel Catalogo (file originali intatti).
 * - Spazio personale: TRASHED; allo svuota si cancella anche il file app.
 */
class TrashService(
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    suspend fun listTrash(): List<TrashEntry> {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        return catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED).mapNotNull { copy ->
            val item = items[copy.mediaItemId] ?: return@mapNotNull null
            val location = locations[copy.storageLocationId]
            TrashEntry(
                mediaItem = item,
                mediaCopy = copy,
                locationName = location?.displayName ?: copy.storageLocationId,
                isPersonalArchive = copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID,
            )
        }
    }

    /** Mette nel Cestino tutte le copie ACTIVE di un MediaItem. */
    suspend fun trashMediaItem(mediaItemId: String): TrashActionResult {
        val ids = catalogStore.listMediaCopiesForItem(mediaItemId)
            .filter { it.state == MediaCopyState.ACTIVE }
            .map { it.id }
        return trashCopies(ids)
    }

    suspend fun trashCopies(copyIds: Collection<String>): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        var n = 0
        for (id in copyIds) {
            val copy = catalogStore.getMediaCopy(id) ?: continue
            if (copy.state == MediaCopyState.TRASHED) continue
            catalogStore.upsertMediaCopy(copy.copy(state = MediaCopyState.TRASHED))
            n++
        }
        return TrashActionResult(
            affected = n,
            message = if (n == 0) {
                "Nessuna copia da cestinare"
            } else {
                "Spostate nel Cestino: $n. Gli originali sulle cartelle del telefono restano intatti."
            },
        )
    }

    /**
     * Per un gruppo di duplicati: tiene [keepMediaItemId], cestinale le altre copie ACTIVE
     * degli altri MediaItem del gruppo (tutte le loro copie ACTIVE).
     */
    suspend fun trashDuplicateExtras(
        fingerprintValue: String,
        keepMediaItemId: String,
        memberCopyIds: Collection<String>,
    ): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val memberCopies = memberCopyIds.mapNotNull { catalogStore.getMediaCopy(it) }
        val extraItemIds = memberCopies
            .map { it.mediaItemId }
            .filter { it != keepMediaItemId }
            .toSet()
        if (extraItemIds.isEmpty()) {
            return TrashActionResult(0, "Niente da cestinare in questo gruppo")
        }
        val toTrash = catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE && it.mediaItemId in extraItemIds }
            .map { it.id }
        val result = trashCopies(toTrash)
        return result.copy(
            message = result.message?.let {
                "Gruppo ${fingerprintValue.take(8)}… — $it"
            },
        )
    }

    suspend fun restoreCopy(copyId: String): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val copy = catalogStore.getMediaCopy(copyId)
            ?: return TrashActionResult(0, "Elemento non trovato")
        if (copy.state != MediaCopyState.TRASHED) {
            return TrashActionResult(0, "Elemento non è nel Cestino")
        }
        catalogStore.upsertMediaCopy(copy.copy(state = MediaCopyState.ACTIVE))
        return TrashActionResult(1, "Ripristinato nel Catalogo")
    }

    suspend fun purgeCopy(copyId: String): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val copy = catalogStore.getMediaCopy(copyId)
            ?: return TrashActionResult(0, "Elemento non trovato")
        // Solo spazio personale: cancellazione fisica. Altri locator: solo catalogo.
        if (copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID) {
            val location = catalogStore.getStorageLocation(copy.storageLocationId)
            if (location != null) {
                runCatching {
                    val adapter = adapterFactory.create(location)
                    if (StorageCapability.DELETE in adapter.capabilities()) {
                        adapter.delete(copy.opaqueLocator)
                    }
                }
            }
        }
        catalogStore.deleteFingerprintsForCopy(copy.id)
        catalogStore.deleteMediaCopy(copy.id)
        val remaining = catalogStore.listMediaCopiesForItem(copy.mediaItemId)
        if (remaining.isEmpty()) {
            catalogStore.deleteMediaItem(copy.mediaItemId)
        }
        return TrashActionResult(
            affected = 1,
            message = if (copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID) {
                "Eliminato definitivamente (anche dal file nello spazio app)"
            } else {
                "Rimosso dal Catalogo (file originale sul telefono non toccato)"
            },
        )
    }

    suspend fun purgeAll(): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val trashed = catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED)
        var n = 0
        for (copy in trashed) {
            n += purgeCopy(copy.id).affected
        }
        return TrashActionResult(n, "Cestino svuotato: $n elementi")
    }

    suspend fun trashCount(): Long =
        catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED).size.toLong()
}
