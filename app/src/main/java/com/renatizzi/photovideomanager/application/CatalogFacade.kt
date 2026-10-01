package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.ArchiveKind
import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import com.renatizzi.photovideomanager.domain.port.StorageAdapter

/**
 * Application service sottile: orchestra Catalogo + Storage senza contaminare il Domain.
 */
class CatalogFacade(
    private val catalogStore: CatalogStore,
    private val localStorage: StorageAdapter,
    private val permissionGate: PermissionGate,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        require(permissionGate.canMutateCatalog(
            com.renatizzi.photovideomanager.domain.model.DomainScope.PERSONAL,
        ))
        catalogStore.upsertArchive(
            ArchiveRef(
                id = PERSONAL_ARCHIVE_ID,
                displayName = "Archivio personale locale",
                kind = ArchiveKind.PERSONAL_LOCAL,
                isSharedArchive = false,
            ),
        )
        catalogStore.upsertStorageLocation(
            StorageLocation(
                id = PERSONAL_LOCATION_ID,
                archiveId = PERSONAL_ARCHIVE_ID,
                opaqueLocator = "",
                availability = localStorage.availability(),
            ),
        )
    }

    suspend fun mediaItemCount(): Long = catalogStore.countMediaItems()

    suspend fun localStorageAvailability(): Availability = localStorage.availability()

    companion object {
        const val PERSONAL_ARCHIVE_ID = "archive.personal.local"
        const val PERSONAL_LOCATION_ID = "location.personal.local.root"
    }
}
