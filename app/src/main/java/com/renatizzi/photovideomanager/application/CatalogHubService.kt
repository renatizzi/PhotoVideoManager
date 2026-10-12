package com.renatizzi.photovideomanager.application

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.renatizzi.photovideomanager.data.hub.CatalogSeatStore
import com.renatizzi.photovideomanager.data.hub.HubPackageIo
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubCard
import com.renatizzi.photovideomanager.domain.hub.HubDiffEngine
import com.renatizzi.photovideomanager.domain.hub.HubDiffPreview
import com.renatizzi.photovideomanager.domain.hub.HubPackage
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate

class CatalogHubService(
    private val appContext: Context,
    private val catalogStore: CatalogStore,
    private val seatStore: CatalogSeatStore,
    private val packageIo: HubPackageIo,
    private val permissionGate: PermissionGate,
) {
    fun currentSeat(): CatalogSeat = seatStore.seat()

    fun deviceId(): String = seatStore.deviceId()

    suspend fun configureSeat(seat: CatalogSeat): Result<CatalogSeat> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val toSave = seat.copy(
            configuredAtEpochMs = System.currentTimeMillis(),
        )
        if (!toSave.treeUri.isNullOrBlank()) {
            val uri = Uri.parse(toSave.treeUri)
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching {
                appContext.contentResolver.takePersistableUriPermission(uri, flags)
            }
        }
        packageIo.probeWritable(toSave).getOrThrow()
        val existing = packageIo.read(toSave)
        if (existing == null) {
            packageIo.write(toSave, buildPackageFromLocal(toSave)).getOrThrow()
        }
        seatStore.saveSeat(toSave)
        toSave
    }

    suspend fun seatReachable(): Boolean {
        val seat = seatStore.seat()
        if (!seat.isConfigured) return false
        return packageIo.probeWritable(seat).isSuccess || packageIo.read(seat) != null
    }

    suspend fun buildPreview(): Result<HubDiffPreview> = runCatching {
        require(permissionGate.canView(DomainScope.PERSONAL))
        val seat = seatStore.seat()
        require(seat.isConfigured) { "Sede non configurata" }
        val local = buildLocalCards()
        val remote = packageIo.read(seat)?.cards.orEmpty()
        HubDiffEngine.preview(local, remote, seatStore.lastSynced())
    }

    suspend fun applyUpdate(
        preview: HubDiffPreview,
        resolutions: Map<String, ConflictResolution>,
    ): Result<HubDiffPreview> = runCatching {
        require(permissionGate.canMutateCatalog(DomainScope.PERSONAL))
        val seat = seatStore.seat()
        require(seat.isConfigured) { "Sede non configurata" }
        val resolved = HubDiffEngine.applyResolutions(preview, resolutions)
        if (resolved.conflicts.isNotEmpty()) {
            return@runCatching resolved
        }
        val remotePkg = packageIo.read(seat) ?: HubPackage(
            seatId = seat.treeUri ?: "local",
            updatedAtEpochMs = 0L,
            updatedByDeviceId = deviceId(),
            cards = emptyList(),
        )
        val remoteById = remotePkg.cards.associateBy { it.id }.toMutableMap()
        val localById = buildLocalCards().associateBy { it.id }.toMutableMap()
        val stamps = seatStore.lastSynced().toMutableMap()

        for (change in resolved.toReceive) {
            val remote = change.remote ?: continue
            upsertFromRemote(remote)
            localById[remote.id] = remote
            stamps[remote.id] = HubDiffEngine.SyncStamp(remote.updatedAtEpochMs, remote.updatedByDeviceId)
        }
        for (change in resolved.toPublish) {
            val local = change.local ?: continue
            remoteById[local.id] = local
            stamps[local.id] = HubDiffEngine.SyncStamp(local.updatedAtEpochMs, local.updatedByDeviceId)
        }

        val out = HubPackage(
            seatId = seat.treeUri ?: "local",
            updatedAtEpochMs = System.currentTimeMillis(),
            updatedByDeviceId = deviceId(),
            cards = remoteById.values.sortedBy { it.id },
        )
        packageIo.write(seat, out).getOrThrow()
        seatStore.putLastSynced(stamps)
        resolved
    }

    private suspend fun buildPackageFromLocal(seat: CatalogSeat): HubPackage {
        val cards = buildLocalCards()
        return HubPackage(
            seatId = seat.treeUri ?: "local",
            updatedAtEpochMs = System.currentTimeMillis(),
            updatedByDeviceId = deviceId(),
            cards = cards,
        )
    }

    private suspend fun buildLocalCards(): List<HubCard> {
        val items = catalogStore.listAllMediaItems()
            .filter { it.domainScope == DomainScope.PERSONAL }
        val copies = catalogStore.listAllMediaCopies().groupBy { it.mediaItemId }
        val device = deviceId()
        val synced = seatStore.lastSynced()
        return items.map { item ->
            val itemCopies = copies[item.id].orEmpty()
            val trashed = itemCopies.isNotEmpty() && itemCopies.all { it.state == MediaCopyState.TRASHED }
            val sha = itemCopies.firstOrNull()?.let { copy ->
                catalogStore.listFingerprintsForCopy(copy.id)
                    .firstOrNull { it.algorithm.equals("SHA-256", ignoreCase = true) }
                    ?.value
            }
            val stamp = synced[item.id]
            val by = if (stamp != null && stamp.updatedAtEpochMs == item.updatedAtEpochMs) {
                stamp.updatedByDeviceId
            } else {
                device
            }
            HubCard(
                id = item.id,
                kind = item.kind.name,
                displayTitle = item.displayTitle,
                updatedAtEpochMs = item.updatedAtEpochMs,
                updatedByDeviceId = by,
                trashed = trashed,
                sha256 = sha,
            )
        }
    }

    private suspend fun upsertFromRemote(remote: HubCard) {
        val existing = catalogStore.getMediaItem(remote.id)
        val kind = runCatching { MediaKind.valueOf(remote.kind) }.getOrDefault(MediaKind.PHOTO)
        val now = System.currentTimeMillis()
        val item = MediaItem(
            id = remote.id,
            kind = kind,
            domainScope = DomainScope.PERSONAL,
            capturedAtEpochMs = existing?.capturedAtEpochMs,
            displayTitle = remote.displayTitle ?: existing?.displayTitle,
            createdAtEpochMs = existing?.createdAtEpochMs ?: now,
            updatedAtEpochMs = remote.updatedAtEpochMs,
        )
        catalogStore.upsertMediaItem(item)
        if (remote.trashed) {
            catalogStore.listMediaCopiesForItem(remote.id)
                .filter { it.state != MediaCopyState.TRASHED }
                .forEach { copy ->
                    catalogStore.upsertMediaCopy(copy.copy(state = MediaCopyState.TRASHED))
                }
        }
    }
}
