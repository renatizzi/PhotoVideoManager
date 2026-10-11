package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DedupAnalysisResult
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.policy.ExactDedupGrouping
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import java.security.MessageDigest
import java.util.UUID

/**
 * M07 fetta v1: analisi duplicati esatti (SHA-256).
 * Calcola hash mancanti dove la sorgente è leggibile; nessuna eliminazione automatica.
 */
class DedupService(
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    suspend fun analyzeExactDuplicates(
        maxHashComputations: Int = DEFAULT_MAX_HASHES,
    ): DedupAnalysisResult {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val copies = catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE }
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        val locationNames = locations.mapValues { it.value.displayName }

        var existing = catalogStore.listFingerprintsByAlgorithm(
            ExactDedupGrouping.SHA256,
            ExactDedupGrouping.LEVEL,
        )
        val hashedCopyIds = existing.map { it.mediaCopyId }.toSet()

        var hashesComputed = 0
        var hashesFailed = 0
        for (copy in copies) {
            if (hashesComputed >= maxHashComputations) break
            if (copy.id in hashedCopyIds) continue
            val location = locations[copy.storageLocationId] ?: continue
            val adapter = runCatching { adapterFactory.create(location) }.getOrNull() ?: continue
            if (runCatching { adapter.availability() }.getOrNull() != Availability.AVAILABLE) {
                hashesFailed++
                continue
            }
            val hash = runCatching {
                adapter.openRead(copy.opaqueLocator).use { input ->
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(DEFAULT_BUFFER)
                    while (true) {
                        val n = input.read(buffer)
                        if (n <= 0) break
                        digest.update(buffer, 0, n)
                    }
                    digest.digest().joinToString("") { "%02x".format(it) }
                }
            }.getOrNull()
            if (hash == null) {
                hashesFailed++
                continue
            }
            val fp = MediaFingerprint(
                id = "fp.${UUID.randomUUID()}",
                mediaCopyId = copy.id,
                algorithm = ExactDedupGrouping.SHA256,
                level = ExactDedupGrouping.LEVEL,
                value = hash,
                computedAtEpochMs = System.currentTimeMillis(),
            )
            catalogStore.upsertFingerprint(fp)
            hashesComputed++
        }

        existing = catalogStore.listFingerprintsByAlgorithm(
            ExactDedupGrouping.SHA256,
            ExactDedupGrouping.LEVEL,
        )
        val groups = ExactDedupGrouping.buildGroups(
            itemsById = items,
            copies = copies,
            shaFingerprints = existing,
            locationNames = locationNames,
        )
        val (dupPhotos, dupVideos) = ExactDedupGrouping.extraCountsByKind(groups)
        val message = when {
            copies.isEmpty() ->
                "Catalogo vuoto: prima censisci e acquisisci dei media."
            groups.isEmpty() ->
                "Nessun duplicato esatto trovato tra elementi distinti."
            else -> null
        }
        return DedupAnalysisResult(
            groups = groups,
            copiesScanned = copies.size,
            hashesComputed = hashesComputed,
            hashesFailed = hashesFailed,
            duplicatePhotoExtras = dupPhotos,
            duplicateVideoExtras = dupVideos,
            message = message,
        )
    }

    suspend fun currentExactDuplicateExtras(): Pair<Long, Long> {
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val copies = catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE }
        val locationNames = catalogStore.listStorageLocations()
            .associate { it.id to it.displayName }
        val fps = catalogStore.listFingerprintsByAlgorithm(
            ExactDedupGrouping.SHA256,
            ExactDedupGrouping.LEVEL,
        )
        val groups = ExactDedupGrouping.buildGroups(items, copies, fps, locationNames)
        return ExactDedupGrouping.extraCountsByKind(groups)
    }

    companion object {
        const val DEFAULT_MAX_HASHES = 300
        private const val DEFAULT_BUFFER = 64 * 1024
    }
}
