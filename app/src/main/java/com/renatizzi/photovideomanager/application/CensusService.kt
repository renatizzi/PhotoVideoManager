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
                    // Usa MIME del listing SAF subito: evita di scartare file senza estensione
                    // “classica” (o con suffissi anomali) che il provider tipizza correttamente.
                    val kind = com.renatizzi.photovideomanager.domain.policy.MediaKindDetector.detect(
                        fileName = child.displayName,
                        mimeType = child.mimeType,
                        locatorHint = child.opaqueLocator,
                    ) ?: continue
                    mediaFound++

                    val existing = catalogStore.findMediaCopyByLocator(locationId, child.opaqueLocator)
                    if (existing != null) {
                        mediaSkipped++
                        continue
                    }
                    // BL-15: esclusi dopo pulizia Catalogo → non reimportare.
                    if (catalogStore.isExcludedLocator(locationId, child.opaqueLocator)) {
                        mediaSkipped++
                        continue
                    }

                    val metadata = runCatching { adapter.readMetadata(child.opaqueLocator) }.getOrNull()
                    val resolvedKind =
                        com.renatizzi.photovideomanager.domain.policy.MediaKindDetector.detect(
                            fileName = child.displayName,
                            mimeType = metadata?.mimeType ?: child.mimeType,
                            locatorHint = child.opaqueLocator,
                        ) ?: kind
                    val resolvedSize = metadata?.byteSize ?: child.byteSize
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
                            byteSize = resolvedSize,
                            mimeType = metadata?.mimeType ?: child.mimeType,
                            state = MediaCopyState.ACTIVE,
                            createdAtEpochMs = createdAt,
                        ),
                    )
                    // Fingerprint L0 tecnico: size+name (non determina azioni automatiche).
                    val l0 = listOfNotNull(
                        resolvedSize?.toString(),
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

    companion object {
        const val DEFAULT_MAX_FILES = 10_000
        /** Più alto per NAS/SMB con molte sottocartelle (es. «famiglia»). */
        const val DEFAULT_MAX_DIRS = 5_000
    }
}
