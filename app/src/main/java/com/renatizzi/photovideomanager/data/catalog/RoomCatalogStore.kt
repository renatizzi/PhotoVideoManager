package com.renatizzi.photovideomanager.data.catalog

import com.renatizzi.photovideomanager.domain.model.ArchiveKind
import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore

class RoomCatalogStore(
    private val db: CatalogDatabase,
) : CatalogStore {
    override suspend fun countMediaItems(): Long = db.mediaItemDao().count()

    override suspend fun upsertArchive(archive: ArchiveRef) {
        db.archiveDao().upsert(
            ArchiveEntity(
                id = archive.id,
                displayName = archive.displayName,
                kind = archive.kind.name,
                isSharedArchive = archive.isSharedArchive,
            ),
        )
    }

    override suspend fun upsertStorageLocation(location: StorageLocation) {
        db.storageLocationDao().upsert(
            StorageLocationEntity(
                id = location.id,
                archiveId = location.archiveId,
                opaqueLocator = location.opaqueLocator,
                availability = location.availability.name,
            ),
        )
    }

    override suspend fun upsertMediaItem(item: MediaItem) {
        db.mediaItemDao().upsert(
            MediaItemEntity(
                id = item.id,
                kind = item.kind.name,
                domainScope = item.domainScope.name,
                capturedAtEpochMs = item.capturedAtEpochMs,
                displayTitle = item.displayTitle,
                createdAtEpochMs = item.createdAtEpochMs,
                updatedAtEpochMs = item.updatedAtEpochMs,
            ),
        )
    }

    override suspend fun upsertMediaCopy(copy: MediaCopy) {
        db.mediaCopyDao().upsert(
            MediaCopyEntity(
                id = copy.id,
                mediaItemId = copy.mediaItemId,
                storageLocationId = copy.storageLocationId,
                byteSize = copy.byteSize,
                mimeType = copy.mimeType,
                state = copy.state.name,
                createdAtEpochMs = copy.createdAtEpochMs,
            ),
        )
    }

    override suspend fun upsertFingerprint(fingerprint: MediaFingerprint) {
        db.mediaFingerprintDao().upsert(
            MediaFingerprintEntity(
                id = fingerprint.id,
                mediaCopyId = fingerprint.mediaCopyId,
                algorithm = fingerprint.algorithm,
                level = fingerprint.level,
                value = fingerprint.value,
                computedAtEpochMs = fingerprint.computedAtEpochMs,
            ),
        )
    }

    override suspend fun listMediaItems(limit: Int): List<MediaItem> =
        db.mediaItemDao().list(limit).map { entity ->
            MediaItem(
                id = entity.id,
                kind = MediaKind.valueOf(entity.kind),
                domainScope = DomainScope.valueOf(entity.domainScope),
                capturedAtEpochMs = entity.capturedAtEpochMs,
                displayTitle = entity.displayTitle,
                createdAtEpochMs = entity.createdAtEpochMs,
                updatedAtEpochMs = entity.updatedAtEpochMs,
            )
        }
}

/** Helpers reserved for future mapping of ArchiveKind/Availability enums. */
internal fun ArchiveKind.asStorageLabel(): String = name
internal fun Availability.asStorageLabel(): String = name
internal fun MediaCopyState.asStorageLabel(): String = name
