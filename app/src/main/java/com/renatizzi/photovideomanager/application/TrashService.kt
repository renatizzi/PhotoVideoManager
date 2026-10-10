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
 * Cestino Catalogo.
 * - File del dispositivo: solo rimozione dal Catalogo (file fisico intatto).
 * - Copia gestita dall’app: in Cestino; allo svuota si cancella anche il file gestito dall’app.
 * Elenco e ripristino sono **per elemento di Catalogo** (una riga), non per ogni copia fisica.
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
        val trashed = catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED)
        return trashed
            .groupBy { it.mediaItemId }
            .mapNotNull { (mediaItemId, copies) ->
                val item = items[mediaItemId] ?: return@mapNotNull null
                // Preferisci mostrare la posizione «file del dispositivo» se c’è.
                val deviceCopy = copies.firstOrNull {
                    it.storageLocationId != CatalogFacade.PERSONAL_LOCATION_ID
                }
                val personalCopy = copies.firstOrNull {
                    it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
                }
                val displayCopy = deviceCopy ?: personalCopy ?: copies.first()
                val location = locations[displayCopy.storageLocationId]
                val locationName = when {
                    displayCopy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID ->
                        "Catalogo"
                    else -> location?.displayName ?: "File del dispositivo"
                }
                TrashEntry(
                    mediaItem = item,
                    mediaCopy = displayCopy,
                    locationName = locationName,
                    isPersonalArchive = personalCopy != null,
                    trashedCopyIds = copies.map { it.id },
                )
            }
            .sortedByDescending { it.mediaCopy.createdAtEpochMs }
    }

    /** Mette nel Cestino tutte le copie attive di un elemento di Catalogo. */
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
                "Niente da eliminare"
            } else {
                "Spostato nel Cestino. Il file del dispositivo non è stato cancellato."
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

    /** Ripristina tutte le copie in Cestino dello stesso elemento di Catalogo. */
    suspend fun restoreMediaItem(mediaItemId: String): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val trashed = catalogStore.listMediaCopiesForItem(mediaItemId)
            .filter { it.state == MediaCopyState.TRASHED }
        if (trashed.isEmpty()) {
            return TrashActionResult(0, "Elemento non trovato nel Cestino")
        }
        for (copy in trashed) {
            catalogStore.upsertMediaCopy(copy.copy(state = MediaCopyState.ACTIVE))
        }
        return TrashActionResult(trashed.size, "Ripristinato nel Catalogo")
    }

    @Deprecated("Usare restoreMediaItem", ReplaceWith("restoreMediaItem(mediaItemId)"))
    suspend fun restoreCopy(copyId: String): TrashActionResult {
        val copy = catalogStore.getMediaCopy(copyId)
            ?: return TrashActionResult(0, "Elemento non trovato")
        return restoreMediaItem(copy.mediaItemId)
    }

    /** Elimina definitivamente tutte le copie in Cestino dello stesso elemento. */
    suspend fun purgeMediaItem(mediaItemId: String): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val trashed = catalogStore.listMediaCopiesForItem(mediaItemId)
            .filter { it.state == MediaCopyState.TRASHED }
        if (trashed.isEmpty()) {
            return TrashActionResult(0, "Elemento non trovato")
        }
        var n = 0
        var removedManagedFile = false
        for (copy in trashed) {
            n += purgeOneCopy(copy.id).affected
            if (copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID) {
                removedManagedFile = true
            }
        }
        return TrashActionResult(
            affected = n,
            message = if (removedManagedFile) {
                "Eliminato definitivamente dal Catalogo"
            } else {
                "Rimosso dal Catalogo (file del dispositivo non toccato)"
            },
        )
    }

    @Deprecated("Usare purgeMediaItem", ReplaceWith("purgeMediaItem(mediaItemId)"))
    suspend fun purgeCopy(copyId: String): TrashActionResult {
        val copy = catalogStore.getMediaCopy(copyId)
            ?: return TrashActionResult(0, "Elemento non trovato")
        // Se ci sono altre copie in Cestino dello stesso item, purga tutto l’item
        // per evitare residui (es. riga «Eraser» rimasta dopo un ripristino parziale).
        val siblings = catalogStore.listMediaCopiesForItem(copy.mediaItemId)
            .filter { it.state == MediaCopyState.TRASHED }
        return if (siblings.size > 1) {
            purgeMediaItem(copy.mediaItemId)
        } else {
            purgeOneCopy(copyId)
        }
    }

    private suspend fun purgeOneCopy(copyId: String): TrashActionResult {
        val copy = catalogStore.getMediaCopy(copyId)
            ?: return TrashActionResult(0, "Elemento non trovato")
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
                "Eliminato definitivamente dal Catalogo"
            } else {
                "Rimosso dal Catalogo (file del dispositivo non toccato)"
            },
        )
    }

    suspend fun purgeAll(): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val itemIds = catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED)
            .map { it.mediaItemId }
            .toSet()
        var n = 0
        for (id in itemIds) {
            n += purgeMediaItem(id).affected
        }
        return TrashActionResult(n, "Cestino svuotato")
    }

    /** Conta elementi di Catalogo in Cestino (non le singole copie fisiche). */
    suspend fun trashCount(): Long =
        catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED)
            .map { it.mediaItemId }
            .toSet()
            .size
            .toLong()
}
