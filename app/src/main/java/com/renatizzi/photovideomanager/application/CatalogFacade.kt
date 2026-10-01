package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceSummary

/**
 * Facade sottile verso UI: delega a SourceRegistry per bootstrap e sorgenti.
 */
class CatalogFacade(
    private val sourceRegistry: SourceRegistry,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        sourceRegistry.bootstrapPersonalArchiveIfNeeded()
    }

    suspend fun mediaItemCount(): Long = sourceRegistry.mediaItemCount()

    suspend fun localStorageAvailability(): Availability {
        val personal = sourceRegistry.listSources(refreshAvailability = true)
            .firstOrNull { it.isBuiltInPersonal }
        return personal?.availability ?: Availability.UNKNOWN
    }

    suspend fun listSources(refresh: Boolean = true): List<SourceSummary> =
        sourceRegistry.listSources(refreshAvailability = refresh)

    suspend fun registerSafFolder(treeUri: android.net.Uri, displayName: String): SourceSummary =
        sourceRegistry.registerSafFolder(treeUri, displayName)

    suspend fun removeSource(locationId: String) = sourceRegistry.removeSource(locationId)

    companion object {
        const val PERSONAL_ARCHIVE_ID = "archive.personal.local"
        const val PERSONAL_LOCATION_ID = "location.personal.local.root"
    }
}
