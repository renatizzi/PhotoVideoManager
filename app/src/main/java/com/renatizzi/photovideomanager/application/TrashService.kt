package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.model.TrashActionResult
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate

/**
 * Cestino Catalogo.
 * - Default: solo rimozione dal Catalogo (file del dispositivo intatto) + esclusione anti re-import (BL-15).
 * - Opzione esplicita: cancellare anche il file sulla sorgente (capability DELETE).
 * - Copia gestita dall’app: allo svuota si cancella anche il file gestito dall’app.
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

    /**
     * @param deleteSourceFiles se true (scelta utente), tenta DELETE sulle copie sorgente.
     */
    suspend fun trashMediaItem(
        mediaItemId: String,
        deleteSourceFiles: Boolean = false,
    ): TrashActionResult {
        val ids = catalogStore.listMediaCopiesForItem(mediaItemId)
            .filter { it.state == MediaCopyState.ACTIVE }
            .map { it.id }
        return trashCopies(ids, deleteSourceFiles = deleteSourceFiles)
    }

    suspend fun trashCopies(
        copyIds: Collection<String>,
        deleteSourceFiles: Boolean = false,
    ): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        var n = 0
        var deletedSources = 0
        for (id in copyIds) {
            val copy = catalogStore.getMediaCopy(id) ?: continue
            if (copy.state == MediaCopyState.TRASHED) continue
            rememberExclusion(copy)
            if (deleteSourceFiles && copy.storageLocationId != CatalogFacade.PERSONAL_LOCATION_ID) {
                if (tryDeletePhysical(copy)) deletedSources++
            }
            catalogStore.upsertMediaCopy(copy.copy(state = MediaCopyState.TRASHED))
            n++
        }
        return TrashActionResult(
            affected = n,
            message = when {
                n == 0 -> "Niente da eliminare"
                deletedSources > 0 ->
                    "Spostato nel Cestino. File eliminati sulla sorgente: $deletedSources."
                else ->
                    "Spostato nel Cestino. Il file del dispositivo non è stato cancellato."
            },
        )
    }

    suspend fun trashDuplicateExtras(
        fingerprintValue: String,
        keepMediaItemId: String,
        memberCopyIds: Collection<String>,
        deleteSourceFiles: Boolean = false,
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
        val result = trashCopies(toTrash, deleteSourceFiles = deleteSourceFiles)
        return result.copy(
            message = result.message?.let {
                "Gruppo ${fingerprintValue.take(8)}… — $it"
            },
        )
    }

    suspend fun restoreMediaItem(mediaItemId: String): TrashActionResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val trashed = catalogStore.listMediaCopiesForItem(mediaItemId)
            .filter { it.state == MediaCopyState.TRASHED }
        if (trashed.isEmpty()) {
            return TrashActionResult(0, "Elemento non trovato nel Cestino")
        }
        for (copy in trashed) {
            catalogStore.removeExclusion(copy.storageLocationId, copy.opaqueLocator)
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
            rememberExclusion(copy)
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
        val siblings = catalogStore.listMediaCopiesForItem(copy.mediaItemId)
            .filter { it.state == MediaCopyState.TRASHED }
        return if (siblings.size > 1) {
            purgeMediaItem(copy.mediaItemId)
        } else {
            rememberExclusion(copy)
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

    suspend fun trashCount(): Long =
        catalogStore.listMediaCopiesByState(MediaCopyState.TRASHED)
            .map { it.mediaItemId }
            .toSet()
            .size
            .toLong()

    private suspend fun rememberExclusion(copy: MediaCopy) {
        if (copy.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID) return
        val sha = catalogStore.listFingerprintsForCopy(copy.id)
            .firstOrNull { it.algorithm.contains("SHA", ignoreCase = true) }
            ?.value
        catalogStore.addExclusion(
            storageLocationId = copy.storageLocationId,
            opaqueLocator = copy.opaqueLocator,
            fingerprintSha256 = sha,
        )
    }

    private suspend fun tryDeletePhysical(copy: MediaCopy): Boolean {
        val location = catalogStore.getStorageLocation(copy.storageLocationId) ?: return false
        return runCatching {
            val adapter = adapterFactory.create(location)
            if (StorageCapability.DELETE !in adapter.capabilities()) return@runCatching false
            adapter.delete(copy.opaqueLocator)
            true
        }.getOrDefault(false)
    }
}
