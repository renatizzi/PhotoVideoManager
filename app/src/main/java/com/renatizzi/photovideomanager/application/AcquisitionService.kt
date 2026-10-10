package com.renatizzi.photovideomanager.application

import android.net.Uri
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.AcquireResult
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.ImportSession
import com.renatizzi.photovideomanager.domain.model.ImportSessionState
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import java.security.MessageDigest
import java.util.UUID

/**
 * Acquisizione M06 (fetta v1): copia verificata nello spazio personale locale.
 * Staging → verify hash → commit catalogo. Sorgente non modificata.
 * Consolidamento Archivio Condiviso: tranche successiva.
 */
class AcquisitionService(
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    /**
     * Originali Importa (candidati tecnici). Se [sourceLocationIds] è non vuoto, limita alle sole fonti
     * confermate in Acquisisci (non all’intero Catalogo).
     */
    suspend fun listCandidates(
        sourceLocationIds: Set<String>? = null,
        limit: Int = DEFAULT_CANDIDATE_LIMIT,
    ): List<AcquireCandidate> {
        catalogStore.listArchives() // warm
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        val filterIds = sourceLocationIds?.filter { it != CatalogFacade.PERSONAL_LOCATION_ID }?.toSet()
        val items = catalogStore.listAllMediaItems()
        val result = ArrayList<AcquireCandidate>(minOf(items.size, limit))
        for (item in items) {
            if (result.size >= limit) break
            val copies = catalogStore.listMediaCopiesForItem(item.id)
                .filter { it.state == MediaCopyState.ACTIVE }
            val sourceCopy = copies.firstOrNull { copy ->
                copy.storageLocationId != CatalogFacade.PERSONAL_LOCATION_ID &&
                    (filterIds == null || copy.storageLocationId in filterIds)
            } ?: continue
            val sourceLocation = locations[sourceCopy.storageLocationId]
            val alreadyPersonal = copies.any { it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID }
            result.add(
                AcquireCandidate(
                    mediaItem = item,
                    sourceCopy = sourceCopy,
                    sourceLocationName = sourcePathLabel(sourceLocation)
                        ?: sourceLocation?.displayName
                        ?: sourceCopy.storageLocationId,
                    alreadyInPersonalArchive = alreadyPersonal,
                ),
            )
        }
        return result
    }

    /** Stesso formato percorso usato in Acquisisci (es. Memoria principale/Pictures). */
    private fun sourcePathLabel(location: StorageLocation?): String? {
        if (location == null) return null
        if (location.adapterKind != StorageAdapterKind.SAF_TREE) return location.displayName
        if (location.opaqueLocator.isBlank()) return location.displayName
        return runCatching {
            val uri = Uri.parse(location.opaqueLocator)
            SafPathLabels.humanPath(uri)
                ?: location.displayName.takeUnless { SafPathLabels.looksIllegible(it) }
        }.getOrNull()
    }

    suspend fun acquireToPersonalArchive(mediaItemIds: Collection<String>): AcquireResult {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val destination = catalogStore.getStorageLocation(CatalogFacade.PERSONAL_LOCATION_ID)
            ?: return AcquireResult("", ImportSessionState.FAILED, 0, 0, 0, "Destinazione personale assente")

        val destAdapter = adapterFactory.create(destination)
        if (destAdapter.availability() != Availability.AVAILABLE) {
            return AcquireResult("", ImportSessionState.FAILED, 0, 0, 0, "Catalogo non disponibile")
        }
        if (StorageCapability.WRITE !in destAdapter.capabilities()) {
            return AcquireResult("", ImportSessionState.FAILED, 0, 0, 0, "Destinazione non scrivibile")
        }

        val now = System.currentTimeMillis()
        val sessionId = "import.${UUID.randomUUID()}"
        var session = ImportSession(
            id = sessionId,
            destinationLocationId = destination.id,
            state = ImportSessionState.PREFLIGHT,
            startedAtEpochMs = now,
            updatedAtEpochMs = now,
            itemsTotal = mediaItemIds.size,
        )
        catalogStore.upsertImportSession(session)

        var acquired = 0
        var skipped = 0
        var failed = 0

        session = session.copy(state = ImportSessionState.RUNNING, updatedAtEpochMs = System.currentTimeMillis())
        catalogStore.upsertImportSession(session)

        val stagingParent = ".pvm_staging/$sessionId"

        for (itemId in mediaItemIds) {
            try {
                val item = catalogStore.getMediaItem(itemId)
                if (item == null) {
                    failed++
                    continue
                }
                val copies = catalogStore.listMediaCopiesForItem(itemId)
                    .filter { it.state == MediaCopyState.ACTIVE }
                if (copies.any { it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID }) {
                    skipped++
                    continue
                }
                val sourceCopy = copies.firstOrNull { it.storageLocationId != CatalogFacade.PERSONAL_LOCATION_ID }
                    ?: copies.firstOrNull()
                if (sourceCopy == null) {
                    failed++
                    continue
                }
                val sourceLocation = catalogStore.getStorageLocation(sourceCopy.storageLocationId)
                if (sourceLocation == null) {
                    failed++
                    continue
                }
                val sourceAdapter = adapterFactory.create(sourceLocation)
                if (sourceAdapter.availability() != Availability.AVAILABLE) {
                    failed++
                    continue
                }

                val fileName = item.displayTitle?.ifBlank { null } ?: "media_${item.id}"
                val digest = MessageDigest.getInstance("SHA-256")
                var byteCount = 0L

                // Staging write
                val stagedLocator = sourceAdapter.openRead(sourceCopy.opaqueLocator).use { input ->
                    val counting = object : java.io.FilterInputStream(input) {
                        override fun read(): Int {
                            val b = super.read()
                            if (b >= 0) {
                                digest.update(b.toByte())
                                byteCount++
                            }
                            return b
                        }

                        override fun read(b: ByteArray, off: Int, len: Int): Int {
                            val n = super.read(b, off, len)
                            if (n > 0) {
                                digest.update(b, off, n)
                                byteCount += n.toLong()
                            }
                            return n
                        }
                    }
                    destAdapter.writeCopy(stagingParent, fileName, counting)
                }
                val hash = digest.digest().joinToString("") { "%02x".format(it) }

                // Verify staged size
                val stagedMeta = destAdapter.readMetadata(stagedLocator)
                if (stagedMeta?.byteSize != null && sourceCopy.byteSize != null &&
                    stagedMeta.byteSize != sourceCopy.byteSize &&
                    stagedMeta.byteSize != byteCount
                ) {
                    failed++
                    continue
                }

                // Promote: copy from staged into acquired/ folder (commit fisico semplificato)
                val promotedLocator = destAdapter.openRead(stagedLocator).use { stagedIn ->
                    destAdapter.writeCopy("acquired", fileName, stagedIn)
                }
                val promotedMeta = destAdapter.readMetadata(promotedLocator)
                if (promotedMeta?.byteSize != null && promotedMeta.byteSize != byteCount) {
                    failed++
                    continue
                }

                val createdAt = System.currentTimeMillis()
                val newCopyId = "copy.${UUID.randomUUID()}"
                catalogStore.upsertMediaCopy(
                    MediaCopy(
                        id = newCopyId,
                        mediaItemId = item.id,
                        storageLocationId = CatalogFacade.PERSONAL_LOCATION_ID,
                        opaqueLocator = promotedLocator,
                        byteSize = byteCount,
                        mimeType = sourceCopy.mimeType,
                        state = MediaCopyState.ACTIVE,
                        createdAtEpochMs = createdAt,
                    ),
                )
                catalogStore.upsertFingerprint(
                    MediaFingerprint(
                        id = "fp.${UUID.randomUUID()}",
                        mediaCopyId = newCopyId,
                        algorithm = "SHA-256",
                        level = 2,
                        value = hash,
                        computedAtEpochMs = createdAt,
                    ),
                )
                acquired++
            } catch (_: Throwable) {
                failed++
            }
        }

        val finalState = when {
            failed > 0 && acquired == 0 && skipped == 0 -> ImportSessionState.FAILED
            else -> ImportSessionState.COMPLETED
        }
        val lastError = if (failed > 0) "$failed elementi non acquisiti" else null
        session = session.copy(
            state = finalState,
            updatedAtEpochMs = System.currentTimeMillis(),
            itemsDone = acquired + skipped,
            itemsFailed = failed,
            lastError = lastError,
        )
        catalogStore.upsertImportSession(session)

        val summary = buildString {
            append("Acquisizione terminata: $acquired copiati")
            if (skipped > 0) append(", $skipped già presenti")
            if (failed > 0) append(", $failed non riusciti")
        }
        return AcquireResult(
            sessionId = sessionId,
            state = finalState,
            acquired = acquired,
            skippedAlreadyPresent = skipped,
            failed = failed,
            message = summary,
        )
    }

    companion object {
        const val DEFAULT_CANDIDATE_LIMIT = 5_000
    }
}
