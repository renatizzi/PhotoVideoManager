package com.renatizzi.photovideomanager.data.catalog

import com.renatizzi.photovideomanager.domain.model.ArchiveKind
import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.ImportSession
import com.renatizzi.photovideomanager.domain.model.ImportSessionState
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.ScanSession
import com.renatizzi.photovideomanager.domain.model.ScanSessionState
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore

class RoomCatalogStore(
    private val db: CatalogDatabase,
) : CatalogStore {
    override suspend fun countMediaItems(): Long = db.mediaItemDao().count()

    override suspend fun countMediaItemsByKind(kind: MediaKind): Long =
        db.mediaItemDao().countByKind(kind.name)

    override suspend fun latestMediaUpdatedAtEpochMs(): Long? =
        db.mediaItemDao().latestUpdatedAt()

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

    override suspend fun listArchives(): List<ArchiveRef> =
        db.archiveDao().list().map { it.toDomain() }

    override suspend fun upsertStorageLocation(location: StorageLocation) {
        db.storageLocationDao().upsert(
            StorageLocationEntity(
                id = location.id,
                archiveId = location.archiveId,
                displayName = location.displayName,
                adapterKind = location.adapterKind.name,
                opaqueLocator = location.opaqueLocator,
                availability = location.availability.name,
            ),
        )
    }

    override suspend fun listStorageLocations(): List<StorageLocation> =
        db.storageLocationDao().list().map { it.toDomain() }

    override suspend fun getStorageLocation(id: String): StorageLocation? =
        db.storageLocationDao().get(id)?.toDomain()

    override suspend fun deleteStorageLocation(id: String) {
        db.storageLocationDao().delete(id)
    }

    override suspend fun deleteArchive(id: String) {
        db.archiveDao().delete(id)
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
                opaqueLocator = copy.opaqueLocator,
                byteSize = copy.byteSize,
                mimeType = copy.mimeType,
                state = copy.state.name,
                createdAtEpochMs = copy.createdAtEpochMs,
            ),
        )
    }

    override suspend fun findMediaCopyByLocator(
        storageLocationId: String,
        opaqueLocator: String,
    ): MediaCopy? = db.mediaCopyDao().findByLocator(storageLocationId, opaqueLocator)?.toDomain()

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

    override suspend fun upsertScanSession(session: ScanSession) {
        db.scanSessionDao().upsert(
            ScanSessionEntity(
                id = session.id,
                sourceLocationId = session.sourceLocationId,
                state = session.state.name,
                startedAtEpochMs = session.startedAtEpochMs,
                updatedAtEpochMs = session.updatedAtEpochMs,
                itemsSeen = session.itemsSeen,
                lastError = session.lastError,
            ),
        )
    }

    override suspend fun getScanSession(id: String): ScanSession? =
        db.scanSessionDao().get(id)?.let { entity ->
            ScanSession(
                id = entity.id,
                sourceLocationId = entity.sourceLocationId,
                state = ScanSessionState.valueOf(entity.state),
                startedAtEpochMs = entity.startedAtEpochMs,
                updatedAtEpochMs = entity.updatedAtEpochMs,
                itemsSeen = entity.itemsSeen,
                lastError = entity.lastError,
            )
        }


    override suspend fun getMediaItem(id: String): MediaItem? =
        db.mediaItemDao().get(id)?.let { entity ->
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

    override suspend fun getMediaCopy(id: String): MediaCopy? =
        db.mediaCopyDao().get(id)?.toDomain()

    override suspend fun listMediaCopiesForItem(mediaItemId: String): List<MediaCopy> =
        db.mediaCopyDao().listByMediaItem(mediaItemId).map { it.toDomain() }

    override suspend fun upsertImportSession(session: ImportSession) {
        db.importSessionDao().upsert(
            ImportSessionEntity(
                id = session.id,
                destinationLocationId = session.destinationLocationId,
                state = session.state.name,
                startedAtEpochMs = session.startedAtEpochMs,
                updatedAtEpochMs = session.updatedAtEpochMs,
                itemsTotal = session.itemsTotal,
                itemsDone = session.itemsDone,
                itemsFailed = session.itemsFailed,
                lastError = session.lastError,
            ),
        )
    }

    override suspend fun getImportSession(id: String): ImportSession? =
        db.importSessionDao().get(id)?.let { entity ->
            ImportSession(
                id = entity.id,
                destinationLocationId = entity.destinationLocationId,
                state = ImportSessionState.valueOf(entity.state),
                startedAtEpochMs = entity.startedAtEpochMs,
                updatedAtEpochMs = entity.updatedAtEpochMs,
                itemsTotal = entity.itemsTotal,
                itemsDone = entity.itemsDone,
                itemsFailed = entity.itemsFailed,
                lastError = entity.lastError,
            )
        }

    private fun ArchiveEntity.toDomain() = ArchiveRef(
        id = id,
        displayName = displayName,
        kind = ArchiveKind.valueOf(kind),
        isSharedArchive = isSharedArchive,
    )

    private fun StorageLocationEntity.toDomain() = StorageLocation(
        id = id,
        archiveId = archiveId,
        displayName = displayName,
        adapterKind = StorageAdapterKind.valueOf(adapterKind),
        opaqueLocator = opaqueLocator,
        availability = Availability.valueOf(availability),
    )

    private fun MediaCopyEntity.toDomain() = MediaCopy(
        id = id,
        mediaItemId = mediaItemId,
        storageLocationId = storageLocationId,
        opaqueLocator = opaqueLocator,
        byteSize = byteSize,
        mimeType = mimeType,
        state = MediaCopyState.valueOf(state),
        createdAtEpochMs = createdAtEpochMs,
    )
}
