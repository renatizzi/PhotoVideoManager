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
import com.renatizzi.photovideomanager.domain.model.RegisterSourceResult
import com.renatizzi.photovideomanager.domain.model.ScrapCandidate
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
    private val exportService: ExportService,
) {
    suspend fun bootstrapPersonalArchiveIfNeeded() {
        sourceRegistry.bootstrapPersonalArchiveIfNeeded()
        // Orfani: MediaItem rimasti dopo CASCADE su sorgente rimossa (senza copie).
        runCatching { sourceRegistry.purgeOrphanMediaItems() }
    }

    suspend fun mediaItemCount(): Long = sourceRegistry.mediaItemCount()

    /**
     * KPI Dashboard — modello A (BL-14, ritocco 0.16.9):
     * - `photoCount` / `videoCount` = originali conosciuti (≥ Catalogo): fonti dispositivo ACTIVE
     *   **oppure** elementi già in Catalogo (copre il caso «solo copia spazio app»).
     *   Il Cestino **app** (TRASHED) non conta — non è la cartella Cestino del telefono.
     * - `catalogPhotoCount` / `catalogVideoCount` = elementi **in Catalogo** (ACTIVE = Aggiorna).
     *   Ingresso in Catalogo = **CONFERMA**, non Importa.
     * - `acquired*` = copie nello spazio app (dopo IMPORTA).
     */
    suspend fun dashboardSnapshot(): DashboardSnapshot {
        bootstrapPersonalArchiveIfNeeded()
        val availability = localStorageAvailability()
        val (dupPhotos, dupVideos) = runCatching { dedupService.currentExactDuplicateExtras() }
            .getOrDefault(0L to 0L)
        val personal = runCatching { archiveService.listPersonalArchive() }.getOrDefault(emptyList())
        val acquiredPhotos = personal.count { it.mediaItem.kind == MediaKind.PHOTO }.toLong()
        val acquiredVideos = personal.count { it.mediaItem.kind == MediaKind.VIDEO }.toLong()
        val personalDiskBytes = sourceRegistry.personalArchiveUsedBytes()
        var acquiredPhotoBytes = personal
            .filter { it.mediaItem.kind == MediaKind.PHOTO }
            .sumOf { it.mediaCopy.byteSize ?: 0L }
        var acquiredVideoBytes = personal
            .filter { it.mediaItem.kind == MediaKind.VIDEO }
            .sumOf { it.mediaCopy.byteSize ?: 0L }
        val catalogedPersonalBytes = acquiredPhotoBytes + acquiredVideoBytes
        if (catalogedPersonalBytes == 0L && personalDiskBytes > 0L && personal.isNotEmpty()) {
            val total = acquiredPhotos + acquiredVideos
            if (total > 0L) {
                acquiredPhotoBytes = personalDiskBytes * acquiredPhotos / total
                acquiredVideoBytes = personalDiskBytes - acquiredPhotoBytes
            }
        }
        // Spazio elementi in Catalogo (preferisci copia personale se presente).
        val catalogEntries = runCatching {
            searchService.search(query = "", kindFilter = SearchKindFilter.ALL)
        }.getOrDefault(emptyList())
        val photoCatalogBytes = catalogEntries
            .filter { it.mediaItem.kind == MediaKind.PHOTO }
            .sumOf { it.previewCopy?.byteSize ?: 0L }
        val videoCatalogBytes = catalogEntries
            .filter { it.mediaItem.kind == MediaKind.VIDEO }
            .sumOf { it.previewCopy?.byteSize ?: 0L }
        val devicePhotos = sourceRegistry.countDeviceOriginalsByKind(MediaKind.PHOTO)
        val deviceVideos = sourceRegistry.countDeviceOriginalsByKind(MediaKind.VIDEO)
        val catalogPhotos = sourceRegistry.countByKind(MediaKind.PHOTO)
        val catalogVideos = sourceRegistry.countByKind(MediaKind.VIDEO)
        // «di cui» ≤ originali: se un elemento è solo nello spazio app, resta negli originali.
        return DashboardSnapshot(
            photoCount = maxOf(devicePhotos, catalogPhotos),
            videoCount = maxOf(deviceVideos, catalogVideos),
            catalogPhotoCount = catalogPhotos,
            catalogVideoCount = catalogVideos,
            acquiredPhotoCount = acquiredPhotos,
            acquiredVideoCount = acquiredVideos,
            photoUsedBytes = photoCatalogBytes,
            videoUsedBytes = videoCatalogBytes,
            acquiredPhotoBytes = acquiredPhotoBytes,
            acquiredVideoBytes = acquiredVideoBytes,
            duplicatePhotoCount = dupPhotos,
            duplicateVideoCount = dupVideos,
            trashCount = trashService.trashCount(),
            personalUsedBytes = personalDiskBytes,
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

    suspend fun registerSafFolder(treeUri: android.net.Uri, displayName: String): RegisterSourceResult =
        sourceRegistry.registerSafFolder(treeUri, displayName)

    suspend fun removeSource(locationId: String) = sourceRegistry.removeSource(locationId)

    suspend fun renameSource(locationId: String, newName: String) =
        sourceRegistry.renameSource(locationId, newName)

    fun setDeviceAlias(alias: String?) = sourceRegistry.setDeviceAlias(alias)

    fun deviceAliasOrDefault(): String = sourceRegistry.deviceAliasOrDefault()

    suspend fun censusSource(locationId: String): CensusResult =
        censusService.censusSource(locationId)

    suspend fun listAcquireCandidates(
        sourceLocationIds: Set<String>? = null,
        limit: Int = AcquisitionService.DEFAULT_CANDIDATE_LIMIT,
    ): List<AcquireCandidate> =
        acquisitionService.listCandidates(sourceLocationIds = sourceLocationIds, limit = limit)

    suspend fun acquireToPersonalArchive(mediaItemIds: Collection<String>): AcquireResult =
        acquisitionService.acquireToPersonalArchive(mediaItemIds)

    /** Scarta dal Catalogo i file deselezionati in Importa (non ancora acquisiti). */
    suspend fun discardUnselectedCensusItems(mediaItemIds: Collection<String>): Int =
        acquisitionService.discardUnselectedCensusItems(mediaItemIds)

    suspend fun analyzeExactDuplicates(): DedupAnalysisResult =
        dedupService.analyzeExactDuplicates()

    /** BL-01: possibili scarti (proposta; conferma → Cestino). */
    suspend fun listPossibleScraps(): List<ScrapCandidate> =
        archiveService.listPossibleScraps()

    suspend fun trashDuplicateExtras(
        fingerprintValue: String,
        keepMediaItemId: String,
        memberCopyIds: Collection<String>,
        deleteSourceFiles: Boolean = false,
    ): TrashActionResult = trashService.trashDuplicateExtras(
        fingerprintValue = fingerprintValue,
        keepMediaItemId = keepMediaItemId,
        memberCopyIds = memberCopyIds,
        deleteSourceFiles = deleteSourceFiles,
    )

    suspend fun listTrash(): List<TrashEntry> = trashService.listTrash()

    suspend fun restoreFromTrash(mediaItemId: String): TrashActionResult =
        trashService.restoreMediaItem(mediaItemId)

    suspend fun purgeFromTrash(mediaItemId: String): TrashActionResult =
        trashService.purgeMediaItem(mediaItemId)

    suspend fun emptyTrash(): TrashActionResult = trashService.purgeAll()

    suspend fun listPersonalArchive(): List<ArchiveEntry> =
        archiveService.listPersonalArchive()

    suspend fun listMediaForLocation(locationId: String): List<ArchiveEntry> =
        archiveService.listMediaForLocation(locationId)

    /** Sottocartelle immediate dell’albero SAF autorizzato (verifica es. «famiglia»). */
    suspend fun listImmediateChildFolders(locationId: String): List<String> =
        sourceRegistry.listImmediateChildFolders(locationId)

    suspend fun searchCatalog(
        query: String,
        kindFilter: SearchKindFilter = SearchKindFilter.ALL,
    ): List<CatalogSearchEntry> = searchService.search(query = query, kindFilter = kindFilter)

    suspend fun renameMediaTitle(mediaItemId: String, newTitle: String) =
        archiveService.renameMediaTitle(mediaItemId, newTitle)

    suspend fun trashMediaItem(
        mediaItemId: String,
        deleteSourceFiles: Boolean = false,
    ): TrashActionResult = trashService.trashMediaItem(
        mediaItemId = mediaItemId,
        deleteSourceFiles = deleteSourceFiles,
    )

    suspend fun exportMediaItemToUri(
        mediaItemId: String,
        destUri: android.net.Uri,
    ): Result<String> = exportService.exportMediaItemToUri(mediaItemId, destUri)

    companion object {
        const val PERSONAL_ARCHIVE_ID = "archive.personal.local"
        const val PERSONAL_LOCATION_ID = "location.personal.local.root"
    }
}
