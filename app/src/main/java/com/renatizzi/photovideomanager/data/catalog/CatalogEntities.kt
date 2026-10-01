package com.renatizzi.photovideomanager.data.catalog

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val kind: String,
    val isSharedArchive: Boolean,
)

@Entity(
    tableName = "storage_locations",
    foreignKeys = [
        ForeignKey(
            entity = ArchiveEntity::class,
            parentColumns = ["id"],
            childColumns = ["archiveId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("archiveId")],
)
data class StorageLocationEntity(
    @PrimaryKey val id: String,
    val archiveId: String,
    val opaqueLocator: String,
    val availability: String,
)

@Entity(
    tableName = "media_items",
    indices = [Index("domainScope"), Index("kind")],
)
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val domainScope: String,
    val capturedAtEpochMs: Long?,
    val displayTitle: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "media_copies",
    foreignKeys = [
        ForeignKey(
            entity = MediaItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StorageLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["storageLocationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mediaItemId"), Index("storageLocationId"), Index("state")],
)
data class MediaCopyEntity(
    @PrimaryKey val id: String,
    val mediaItemId: String,
    val storageLocationId: String,
    val byteSize: Long?,
    val mimeType: String?,
    val state: String,
    val createdAtEpochMs: Long,
)

@Entity(
    tableName = "media_fingerprints",
    foreignKeys = [
        ForeignKey(
            entity = MediaCopyEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaCopyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("mediaCopyId"),
        Index(value = ["algorithm", "value"]),
    ],
)
data class MediaFingerprintEntity(
    @PrimaryKey val id: String,
    val mediaCopyId: String,
    val algorithm: String,
    val level: Int,
    val value: String,
    val computedAtEpochMs: Long,
)
