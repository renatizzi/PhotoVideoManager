package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.AcquireResult
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.CensusResult
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SourceSummary

/**
 * Facade sottile verso UI: sorgenti, censimento, acquisizione, dashboard.
 */
class CatalogFacade(
    private val sourceRegistry: SourceRegistry,
    private val censusService: CensusService,
    private val acquisitionService: AcquisitionService,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        sourceRegistry.bootstrapPersonalArchiveIfNeeded()
    }

    suspend fun mediaItemCount(): Long = sourceRegistry.mediaItemCount()

    suspend fun dashboardSnapshot(): DashboardSnapshot {
        bootstrapPersonalArchiveIfNeeded()
        val availability = localStorageAvailability()
        return DashboardSnapshot(
            photoCount = sourceRegistry.countByKind(MediaKind.PHOTO),
            videoCount = sourceRegistry.countByKind(MediaKind.VIDEO),
            duplicatePhotoCount = null,
            duplicateVideoCount = null,
            personalUsedBytes = sourceRegistry.personalArchiveUsedBytes(),
            lastUpdatedEpochMs = sourceRegistry.latestMediaUpdatedAtEpochMs(),
            localAvailability = availability,
        )
    }

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

    suspend fun censusSource(locationId: String): CensusResult =
        censusService.censusSource(locationId)

    suspend fun listAcquireCandidates(limit: Int = 200): List<AcquireCandidate> =
        acquisitionService.listCandidates(limit)

    suspend fun acquireToPersonalArchive(mediaItemIds: Collection<String>): AcquireResult =
        acquisitionService.acquireToPersonalArchive(mediaItemIds)

    companion object {
        const val PERSONAL_ARCHIVE_ID = "archive.personal.local"
        const val PERSONAL_LOCATION_ID = "location.personal.local.root"
    }
}
