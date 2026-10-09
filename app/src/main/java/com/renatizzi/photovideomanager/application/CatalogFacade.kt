package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.AcquireResult
import com.renatizzi.photovideomanager.domain.model.ArchiveEntry
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.CatalogSearchEntry
import com.renatizzi.photovideomanager.domain.model.CensusResult
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.domain.model.DedupAnalysisResult
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.TrashActionResult
import com.renatizzi.photovideomanager.domain.model.TrashEntry

/**
 * Facade sottile verso UI.
 */
class CatalogFacade(
    private val sourceRegistry: SourceRegistry,
    private val censusService: CensusService,
    private val acquisitionService: AcquisitionService,
    private val dedupService: DedupService,
    private val trashService: TrashService,
    private val archiveService: ArchiveService,
    private val searchService: SearchService,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        sourceRegistry.bootstrapPersonalArchiveIfNeeded()
    }

    suspend fun mediaItemCount(): Long = sourceRegistry.mediaItemCount()

    suspend fun dashboardSnapshot(): DashboardSnapshot {
        bootstrapPersonalArchiveIfNeeded()
        val availability = localStorageAvailability()
        val (dupPhotos, dupVideos) = runCatching { dedupService.currentExactDuplicateExtras() }
            .getOrDefault(0L to 0L)
        val personal = runCatching { archiveService.listPersonalArchive() }.getOrDefault(emptyList())
        val acquiredPhotos = personal.count { it.mediaItem.kind == MediaKind.PHOTO }.toLong()
        val acquiredVideos = personal.count { it.mediaItem.kind == MediaKind.VIDEO }.toLong()
        val photoBytes = personal
            .filter { it.mediaItem.kind == MediaKind.PHOTO }
            .sumOf { it.mediaCopy.byteSize ?: 0L }
        val videoBytes = personal
            .filter { it.mediaItem.kind == MediaKind.VIDEO }
            .sumOf { it.mediaCopy.byteSize ?: 0L }
        return DashboardSnapshot(
            photoCount = sourceRegistry.countByKind(MediaKind.PHOTO),
            videoCount = sourceRegistry.countByKind(MediaKind.VIDEO),
            acquiredPhotoCount = acquiredPhotos,
            acquiredVideoCount = acquiredVideos,
            photoUsedBytes = photoBytes,
            videoUsedBytes = videoBytes,
            duplicatePhotoCount = dupPhotos,
            duplicateVideoCount = dupVideos,
            trashCount = trashService.trashCount(),
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

    suspend fun analyzeExactDuplicates(): DedupAnalysisResult =
        dedupService.analyzeExactDuplicates()

    suspend fun trashDuplicateExtras(
        fingerprintValue: String,
        keepMediaItemId: String,
        memberCopyIds: Collection<String>,
    ): TrashActionResult = trashService.trashDuplicateExtras(
        fingerprintValue = fingerprintValue,
        keepMediaItemId = keepMediaItemId,
        memberCopyIds = memberCopyIds,
    )

    suspend fun listTrash(): List<TrashEntry> = trashService.listTrash()

    suspend fun restoreFromTrash(copyId: String): TrashActionResult =
        trashService.restoreCopy(copyId)

    suspend fun purgeFromTrash(copyId: String): TrashActionResult =
        trashService.purgeCopy(copyId)

    suspend fun emptyTrash(): TrashActionResult = trashService.purgeAll()

    suspend fun listPersonalArchive(): List<ArchiveEntry> =
        archiveService.listPersonalArchive()

    suspend fun listMediaForLocation(locationId: String): List<ArchiveEntry> =
        archiveService.listMediaForLocation(locationId)

    suspend fun searchCatalog(
        query: String,
        kindFilter: SearchKindFilter = SearchKindFilter.ALL,
    ): List<CatalogSearchEntry> = searchService.search(query = query, kindFilter = kindFilter)

    companion object {
        const val PERSONAL_ARCHIVE_ID = "archive.personal.local"
        const val PERSONAL_LOCATION_ID = "location.personal.local.root"
    }
}
