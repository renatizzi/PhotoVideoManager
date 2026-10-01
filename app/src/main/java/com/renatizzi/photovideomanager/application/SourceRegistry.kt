package com.renatizzi.photovideomanager.application

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.ArchiveKind
import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import java.util.UUID

/**
 * Registro sorgenti/archivi: bootstrap personale, aggiunta cartelle SAF, refresh disponibilità.
 */
class SourceRegistry(
    private val appContext: Context,
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        catalogStore.upsertArchive(
            ArchiveRef(
                id = PERSONAL_ARCHIVE_ID,
                displayName = "Archivio personale locale",
                kind = ArchiveKind.PERSONAL_LOCAL,
                isSharedArchive = false,
            ),
        )
        val localAdapter = adapterFactory.create(
            StorageLocation(
                id = PERSONAL_LOCATION_ID,
                archiveId = PERSONAL_ARCHIVE_ID,
                displayName = "Spazio interno app",
                adapterKind = StorageAdapterKind.LOCAL_FS,
                opaqueLocator = "",
            ),
        )
        catalogStore.upsertStorageLocation(
            StorageLocation(
                id = PERSONAL_LOCATION_ID,
                archiveId = PERSONAL_ARCHIVE_ID,
                displayName = "Spazio interno app",
                adapterKind = StorageAdapterKind.LOCAL_FS,
                opaqueLocator = "",
                availability = localAdapter.availability(),
            ),
        )
    }

    suspend fun listSources(refreshAvailability: Boolean = true): List<SourceSummary> {
        bootstrapPersonalArchiveIfNeeded()
        val archives = catalogStore.listArchives().associateBy { it.id }
        return catalogStore.listStorageLocations().map { location ->
            val availability = if (refreshAvailability) {
                runCatching { adapterFactory.create(location).availability() }
                    .getOrDefault(Availability.UNKNOWN)
                    .also { fresh ->
                        if (fresh != location.availability) {
                            catalogStore.upsertStorageLocation(location.copy(availability = fresh))
                        }
                    }
            } else {
                location.availability
            }
            val archive = archives[location.archiveId]
            SourceSummary(
                locationId = location.id,
                archiveId = location.archiveId,
                displayName = location.displayName,
                adapterKind = location.adapterKind,
                availability = availability,
                isSharedArchive = archive?.isSharedArchive == true,
                isBuiltInPersonal = location.id == PERSONAL_LOCATION_ID,
            )
        }
    }

    suspend fun registerSafFolder(treeUri: Uri, displayName: String): SourceSummary {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            appContext.contentResolver.takePersistableUriPermission(treeUri, flags)
        }.recoverCatching {
            appContext.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }.getOrThrow()

        val archiveId = "archive.external.${UUID.randomUUID()}"
        val locationId = "location.saf.${UUID.randomUUID()}"
        val name = displayName.ifBlank { treeUri.lastPathSegment ?: "Cartella esterna" }

        catalogStore.upsertArchive(
            ArchiveRef(
                id = archiveId,
                displayName = name,
                kind = ArchiveKind.EXTERNAL_SOURCE,
                isSharedArchive = false,
            ),
        )
        val location = StorageLocation(
            id = locationId,
            archiveId = archiveId,
            displayName = name,
            adapterKind = StorageAdapterKind.SAF_TREE,
            opaqueLocator = treeUri.toString(),
            availability = Availability.UNKNOWN,
        )
        val availability = runCatching { adapterFactory.create(location).availability() }
            .getOrDefault(Availability.UNKNOWN)
        val saved = location.copy(availability = availability)
        catalogStore.upsertStorageLocation(saved)

        return SourceSummary(
            locationId = saved.id,
            archiveId = saved.archiveId,
            displayName = saved.displayName,
            adapterKind = saved.adapterKind,
            availability = saved.availability,
            isSharedArchive = false,
            isBuiltInPersonal = false,
        )
    }

    suspend fun removeSource(locationId: String) {
        require(locationId != PERSONAL_LOCATION_ID) { "La sorgente personale integrata non è rimovibile" }
        val location = catalogStore.getStorageLocation(locationId) ?: return
        if (location.adapterKind == StorageAdapterKind.SAF_TREE) {
            runCatching {
                val uri = Uri.parse(location.opaqueLocator)
                appContext.contentResolver.releasePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
        }
        catalogStore.deleteStorageLocation(locationId)
        if (location.archiveId != PERSONAL_ARCHIVE_ID) {
            catalogStore.deleteArchive(location.archiveId)
        }
    }

    suspend fun mediaItemCount(): Long = catalogStore.countMediaItems()

    companion object {
        const val PERSONAL_ARCHIVE_ID = CatalogFacade.PERSONAL_ARCHIVE_ID
        const val PERSONAL_LOCATION_ID = CatalogFacade.PERSONAL_LOCATION_ID
    }
}
