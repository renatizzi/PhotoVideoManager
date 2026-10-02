package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.CensusResult
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.ScanSession
import com.renatizzi.photovideomanager.domain.model.ScanSessionState
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import java.util.ArrayDeque
import java.util.UUID

/**
 * Censimento M05: individua foto/video senza modificare le sorgenti.
 * Idempotente sul locator; nessun retry automatico.
 */
class CensusService(
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    suspend fun censusSource(
        locationId: String,
        maxFiles: Int = DEFAULT_MAX_FILES,
    ): CensusResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val location = catalogStore.getStorageLocation(locationId)
            ?: return CensusResult(
                sessionId = "",
                sourceLocationId = locationId,
                filesSeen = 0,
                mediaFound = 0,
                mediaAdded = 0,
                mediaSkippedExisting = 0,
                state = ScanSessionState.FAILED,
                message = "Sorgente non trovata",
            )

        val now = System.currentTimeMillis()
        val sessionId = "scan.${UUID.randomUUID()}"
        var session = ScanSession(
            id = sessionId,
            sourceLocationId = locationId,
            state = ScanSessionState.RUNNING,
            startedAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        catalogStore.upsertScanSession(session)

        val adapter = adapterFactory.create(location)
        if (adapter.availability() != Availability.AVAILABLE) {
            session = session.copy(
                state = ScanSessionState.FAILED,
                updatedAtEpochMs = System.currentTimeMillis(),
                lastError = "Sorgente non disponibile",
            )
            catalogStore.upsertScanSession(session)
            return CensusResult(
                sessionId = sessionId,
                sourceLocationId = locationId,
                filesSeen = 0,
                mediaFound = 0,
                mediaAdded = 0,
                mediaSkippedExisting = 0,
                state = ScanSessionState.FAILED,
                message = "Sorgente non disponibile",
            )
        }

        var filesSeen = 0
        var mediaFound = 0
        var mediaAdded = 0
        var mediaSkipped = 0

        try {
            val queue = ArrayDeque<String>()
            queue.add(location.opaqueLocator)
            var dirsVisited = 0

            while (queue.isNotEmpty() && filesSeen < maxFiles && dirsVisited < DEFAULT_MAX_DIRS) {
                val current = queue.removeFirst()
                dirsVisited++
                val children: List<StorageEntry> = runCatching { adapter.listChildren(current) }
                    .getOrDefault(emptyList())

                for (child in children) {
                    if (child.isDirectory) {
                        queue.add(child.opaqueLocator)
                        continue
                    }
                    filesSeen++
                    val kind = detectMediaKind(child.displayName, null) ?: continue
                    mediaFound++

                    val existing = catalogStore.findMediaCopyByLocator(locationId, child.opaqueLocator)
                    if (existing != null) {
                        mediaSkipped++
                        continue
                    }

                    val metadata = runCatching { adapter.readMetadata(child.opaqueLocator) }.getOrNull()
                    val resolvedKind = detectMediaKind(child.displayName, metadata?.mimeType) ?: kind
                    val createdAt = System.currentTimeMillis()
                    val itemId = "media.${UUID.randomUUID()}"
                    val copyId = "copy.${UUID.randomUUID()}"

                    catalogStore.upsertMediaItem(
                        MediaItem(
                            id = itemId,
                            kind = resolvedKind,
                            domainScope = DomainScope.PERSONAL,
                            capturedAtEpochMs = metadata?.lastModifiedEpochMs,
                            displayTitle = child.displayName,
                            createdAtEpochMs = createdAt,
                            updatedAtEpochMs = createdAt,
                        ),
                    )
                    catalogStore.upsertMediaCopy(
                        MediaCopy(
                            id = copyId,
                            mediaItemId = itemId,
                            storageLocationId = locationId,
                            opaqueLocator = child.opaqueLocator,
                            byteSize = metadata?.byteSize,
                            mimeType = metadata?.mimeType,
                            state = MediaCopyState.ACTIVE,
                            createdAtEpochMs = createdAt,
                        ),
                    )
                    // Fingerprint L0 tecnico: size+name (non determina azioni automatiche).
                    val l0 = listOfNotNull(
                        metadata?.byteSize?.toString(),
                        child.displayName.lowercase(),
                    ).joinToString("|")
                    if (l0.isNotBlank()) {
                        catalogStore.upsertFingerprint(
                            MediaFingerprint(
                                id = "fp.${UUID.randomUUID()}",
                                mediaCopyId = copyId,
                                algorithm = "L0_SIZE_NAME",
                                level = 0,
                                value = l0,
                                computedAtEpochMs = createdAt,
                            ),
                        )
                    }
                    mediaAdded++
                    if (filesSeen >= maxFiles) break
                }
            }

            session = session.copy(
                state = ScanSessionState.COMPLETED,
                updatedAtEpochMs = System.currentTimeMillis(),
                itemsSeen = mediaFound,
            )
            catalogStore.upsertScanSession(session)

            val emptyHint = if (mediaFound == 0) {
                "Nessuna foto/video trovata in questa cartella e nelle sottocartelle. " +
                    "Controlla di aver scelto la cartella giusta (es. quella che contiene Camera)."
            } else {
                null
            }

            return CensusResult(
                sessionId = sessionId,
                sourceLocationId = locationId,
                filesSeen = filesSeen,
                mediaFound = mediaFound,
                mediaAdded = mediaAdded,
                mediaSkippedExisting = mediaSkipped,
                state = ScanSessionState.COMPLETED,
                message = emptyHint,
            )
        } catch (t: Throwable) {
            session = session.copy(
                state = ScanSessionState.FAILED,
                updatedAtEpochMs = System.currentTimeMillis(),
                itemsSeen = mediaFound,
                lastError = t.message,
            )
            catalogStore.upsertScanSession(session)
            return CensusResult(
                sessionId = sessionId,
                sourceLocationId = locationId,
                filesSeen = filesSeen,
                mediaFound = mediaFound,
                mediaAdded = mediaAdded,
                mediaSkippedExisting = mediaSkipped,
                state = ScanSessionState.FAILED,
                message = t.message ?: "Errore durante il censimento",
            )
        }
    }

    private fun detectMediaKind(fileName: String, mimeType: String?): MediaKind? {
        val mime = mimeType?.lowercase().orEmpty()
        if (mime.startsWith("image/")) return MediaKind.PHOTO
        if (mime.startsWith("video/")) return MediaKind.VIDEO
        val ext = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return when (ext) {
            in PHOTO_EXT -> MediaKind.PHOTO
            in VIDEO_EXT -> MediaKind.VIDEO
            else -> null
        }
    }

    companion object {
        const val DEFAULT_MAX_FILES = 2_000
        const val DEFAULT_MAX_DIRS = 500
        private val PHOTO_EXT = setOf(
            "jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp", "dng",
        )
        private val VIDEO_EXT = setOf(
            "mp4", "mov", "mkv", "webm", "avi", "3gp", "m4v",
        )
    }
}
