package com.renatizzi.photovideomanager.application

import android.content.Context
import android.net.Uri
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.data.storage.SafTreeIdentity
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DedupAnalysisResult
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
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
    private val appContext: Context,
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
    private val localDeviceLabel: () -> String,
) {
    suspend fun analyzeExactDuplicates(
        maxHashComputations: Int = DEFAULT_MAX_HASHES,
    ): DedupAnalysisResult {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val allActive = catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE }
        // Pulisci / Catalogo: esamina le copie nello spazio app (elementi Catalogo),
        // non anche le copie sorgente (altrimenti ~2× rispetto agli elementi).
        val personalCopies = allActive.filter {
            it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
        }
        val copies = personalCopies.ifEmpty { allActive }
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        val locationNames = locations.mapValues { it.value.displayName }
        val itemsExamined = copies.map { it.mediaItemId }.distinct().size

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
        val rawGroups = ExactDedupGrouping.buildGroups(
            itemsById = items,
            copies = copies,
            shaFingerprints = existing,
            locationNames = locationNames,
        )
        // LOCK S25 #2: meta da sorgente esterna (non solo «Catalogo» / path vuoto).
        val groups = enrichMemberSourceLabels(rawGroups, allActive, locations)
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
            copiesScanned = itemsExamined,
            hashesComputed = hashesComputed,
            hashesFailed = hashesFailed,
            duplicatePhotoExtras = dupPhotos,
            duplicateVideoExtras = dupVideos,
            message = message,
        )
    }

    suspend fun currentExactDuplicateExtras(): Pair<Long, Long> {
        val items = catalogStore.listAllMediaItems().associateBy { it.id }
        val allActive = catalogStore.listAllMediaCopies()
            .filter { it.state == MediaCopyState.ACTIVE }
        val personal = allActive.filter {
            it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
        }
        val copies = personal.ifEmpty { allActive }
        val locationNames = catalogStore.listStorageLocations()
            .associate { it.id to it.displayName }
        val fps = catalogStore.listFingerprintsByAlgorithm(
            ExactDedupGrouping.SHA256,
            ExactDedupGrouping.LEVEL,
        )
        val locations = catalogStore.listStorageLocations().associateBy { it.id }
        val groups = enrichMemberSourceLabels(
            ExactDedupGrouping.buildGroups(items, copies, fps, locationNames),
            allActive,
            locations,
        )
        return ExactDedupGrouping.extraCountsByKind(groups)
    }

    private fun enrichMemberSourceLabels(
        groups: List<DuplicateGroup>,
        allActive: List<MediaCopy>,
        locations: Map<String, StorageLocation>,
    ): List<DuplicateGroup> {
        val byItem = allActive.groupBy { it.mediaItemId }
        return groups.map { group ->
            group.copy(
                members = group.members.map { member ->
                    val external = byItem[member.mediaItem.id]?.firstOrNull { copy ->
                        copy.storageLocationId != CatalogFacade.PERSONAL_LOCATION_ID
                    }
                    val loc = locations[external?.storageLocationId]
                        ?: locations[member.mediaCopy.storageLocationId]
                    val device = deviceLabelFor(loc)
                    val path = pathLabelFor(loc, device)
                    member.copy(
                        locationName = loc?.displayName?.ifBlank { null } ?: member.locationName,
                        deviceLabel = device,
                        pathLabel = path,
                        isSecondaryVolume = loc?.opaqueLocator
                            ?.let { SafTreeIdentity.isMobileSecondaryVolume(it) } == true,
                    )
                },
            )
        }
    }

    private fun deviceLabelFor(location: StorageLocation?): String {
        if (location == null) return ""
        if (location.id == CatalogFacade.PERSONAL_LOCATION_ID) return ""
        return when (location.adapterKind) {
            StorageAdapterKind.LOCAL_FS -> ""
            StorageAdapterKind.MEDIA_STORE -> "Galleria di sistema"
            StorageAdapterKind.SMB -> SafPathLabels.networkDeviceLabelFromHints(
                displayHint = location.displayName,
                path = "",
                uriString = location.opaqueLocator,
            )
            StorageAdapterKind.SAF_TREE -> {
                if (location.opaqueLocator.isBlank()) return location.displayName
                runCatching {
                    val uri = Uri.parse(location.opaqueLocator)
                    when (SafPathLabels.providerOf(uri)) {
                        SafPathLabels.SafProvider.NETWORK_NAS ->
                            SafPathLabels.networkDeviceLabel(uri, location.displayName)
                        else ->
                            SafPathLabels.deviceLabelForTree(
                                appContext,
                                uri,
                                localDeviceLabel().ifBlank { SafPathLabels.deviceLabel(appContext) },
                            )
                    }
                }.getOrDefault(location.displayName)
            }
        }
    }

    private fun pathLabelFor(location: StorageLocation?, deviceLabel: String): String {
        if (location == null) return ""
        if (location.id == CatalogFacade.PERSONAL_LOCATION_ID) return ""
        if (location.adapterKind != StorageAdapterKind.SAF_TREE || location.opaqueLocator.isBlank()) {
            return location.displayName.trim()
        }
        return runCatching {
            val uri = Uri.parse(location.opaqueLocator)
            val raw = SafPathLabels.storagePathLabel(appContext, uri)
            if (SafPathLabels.looksIllegible(raw)) {
                return@runCatching location.displayName.trim()
            }
            val under = SafPathLabels.pathUnderName(raw, location.displayName, deviceLabel)
            under.ifBlank { location.displayName.trim() }
        }.getOrDefault(location.displayName.trim())
    }

    companion object {
        const val DEFAULT_MAX_HASHES = 300
        private const val DEFAULT_BUFFER = 64 * 1024
    }
}
