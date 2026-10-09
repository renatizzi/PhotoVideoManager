package com.renatizzi.photovideomanager.application

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.renatizzi.photovideomanager.data.SourceLabelStore
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.ArchiveKind
import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaKind
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
    private val personalRoot: java.io.File,
    private val labelStore: SourceLabelStore,
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
            val pathLabel = pathLabelFor(location)
            val displayName = resolveDisplayName(location, pathLabel)
            // Auto-ripara displayName in Catalogo se ancora un token SAF illegibile.
            if (displayName != location.displayName && looksIllegible(location.displayName)) {
                catalogStore.upsertStorageLocation(location.copy(displayName = displayName))
            }
            SourceSummary(
                locationId = location.id,
                archiveId = location.archiveId,
                displayName = displayName,
                deviceLabel = deviceLabelFor(location),
                pathLabel = pathLabel,
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
        val pathLabel = SafPathLabels.humanPath(treeUri)
        val name = when {
            displayName.isNotBlank() && !looksIllegible(displayName) -> displayName
            pathLabel != null -> SafPathLabels.folderTitle(treeUri, pathLabel)
            else -> "Cartella dispositivo"
        }

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
            deviceLabel = deviceLabelFor(saved),
            pathLabel = pathLabel ?: saved.displayName,
            adapterKind = saved.adapterKind,
            availability = saved.availability,
            isSharedArchive = false,
            isBuiltInPersonal = false,
        )
    }

    suspend fun renameSource(locationId: String, newName: String) {
        require(locationId != PERSONAL_LOCATION_ID)
        val clean = newName.trim()
        require(clean.isNotEmpty()) { "Nome non valido" }
        val location = catalogStore.getStorageLocation(locationId) ?: return
        labelStore.setSourceAlias(locationId, clean)
        catalogStore.upsertStorageLocation(location.copy(displayName = clean))
        catalogStore.listArchives().firstOrNull { it.id == location.archiveId }?.let { archive ->
            catalogStore.upsertArchive(archive.copy(displayName = clean))
        }
    }

    fun setDeviceAlias(alias: String?) {
        labelStore.setDeviceAlias(alias)
    }

    fun deviceAliasOrDefault(): String =
        labelStore.deviceAlias() ?: SafPathLabels.deviceLabel(appContext)

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
        labelStore.setSourceAlias(locationId, null)
        catalogStore.deleteStorageLocation(locationId)
        if (location.archiveId != PERSONAL_ARCHIVE_ID) {
            catalogStore.deleteArchive(location.archiveId)
        }
    }

    suspend fun mediaItemCount(): Long = catalogStore.countMediaItems()

    suspend fun countByKind(kind: MediaKind): Long = catalogStore.countMediaItemsByKind(kind)

    suspend fun latestMediaUpdatedAtEpochMs(): Long? = catalogStore.latestMediaUpdatedAtEpochMs()

    suspend fun personalArchiveUsedBytes(): Long = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (!personalRoot.exists()) return@withContext 0L
        personalRoot.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun deviceLabelFor(location: StorageLocation): String = when (location.adapterKind) {
        StorageAdapterKind.LOCAL_FS -> "Spazio app MediaManager"
        StorageAdapterKind.SAF_TREE -> deviceAliasOrDefault()
        StorageAdapterKind.MEDIA_STORE -> "Galleria di sistema"
        StorageAdapterKind.SMB -> "Rete / NAS"
    }

    private fun pathLabelFor(location: StorageLocation): String {
        if (location.adapterKind == StorageAdapterKind.LOCAL_FS) {
            return personalRoot.absolutePath
        }
        if (location.opaqueLocator.isBlank()) return location.displayName
        return runCatching {
            val uri = Uri.parse(location.opaqueLocator)
            SafPathLabels.humanPath(uri)
                ?: location.displayName.takeUnless { looksIllegible(it) }
                ?: "Cartella dispositivo"
        }.getOrDefault(location.displayName)
    }

    private fun resolveDisplayName(location: StorageLocation, pathLabel: String): String {
        labelStore.sourceAlias(location.id)?.let { return it }
        if (location.adapterKind == StorageAdapterKind.SAF_TREE && location.opaqueLocator.isNotBlank()) {
            val derived = SafPathLabels.folderTitle(Uri.parse(location.opaqueLocator), pathLabel)
            val stored = location.displayName
            // Usa sempre il titolo derivato se lo stored è illegibile, uguale al solo volume,
            // o è un vecchio «Memoria principale» senza cartella.
            val volumeOnly = !pathLabel.contains('/')
            if (looksIllegible(stored) ||
                stored == pathLabel ||
                (volumeOnly && stored.equals(pathLabel, ignoreCase = true)) ||
                stored.equals(SafPathLabels.volumeLabel("primary"), ignoreCase = true)
            ) {
                return derived
            }
            return stored
        }
        if (!looksIllegible(location.displayName)) return location.displayName
        return pathLabel.substringAfterLast('/').ifBlank { pathLabel }
    }

    private fun looksIllegible(name: String): Boolean {
        if (name.isBlank()) return true
        val lower = name.lowercase()
        return lower.contains("encoded=") ||
            lower.startsWith("acc=") ||
            lower.contains("doc=") ||
            (name.length > 40 && !name.contains('/') && name.count { it == '-' || it == '_' } > 4)
    }

    companion object {
        const val PERSONAL_ARCHIVE_ID = CatalogFacade.PERSONAL_ARCHIVE_ID
        const val PERSONAL_LOCATION_ID = CatalogFacade.PERSONAL_LOCATION_ID
    }
}
