package com.renatizzi.photovideomanager.data.catalog

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * DAO Catalogo.
 *
 * Importante: usare [Upsert] (INSERT ON CONFLICT DO UPDATE), **non**
 * `@Insert(onConflict = REPLACE)`. REPLACE in SQLite elimina la riga e la
 * reinserisce: con `ForeignKey(onDelete = CASCADE)` cancellerebbe tutte le
 * MediaCopy / fingerprint figlie. Sintomo: dopo IMPORTA, Home chiama
 * `bootstrapPersonalArchiveIfNeeded()` → upsert location personale →
 * acquiredPhotoCount torna a 0.
 */
@Dao
interface ArchiveDao {
    @Upsert
    suspend fun upsert(entity: ArchiveEntity)

    @Query("SELECT * FROM archives ORDER BY displayName")
    suspend fun list(): List<ArchiveEntity>

    @Query("DELETE FROM archives WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface StorageLocationDao {
    @Upsert
    suspend fun upsert(entity: StorageLocationEntity)

    @Query("SELECT * FROM storage_locations ORDER BY displayName")
    suspend fun list(): List<StorageLocationEntity>

    @Query("SELECT * FROM storage_locations WHERE id = :id LIMIT 1")
    suspend fun get(id: String): StorageLocationEntity?

    @Query("DELETE FROM storage_locations WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MediaItemDao {
    @Upsert
    suspend fun upsert(entity: MediaItemEntity)

    /** Elementi con almeno una copia ACTIVE (= Catalogo / Aggiorna). */
    @Query(
        """
        SELECT COUNT(DISTINCT mi.id) FROM media_items mi
        INNER JOIN media_copies mc ON mc.mediaItemId = mi.id
        WHERE mc.state = 'ACTIVE'
        """,
    )
    suspend fun count(): Long

    @Query(
        """
        SELECT COUNT(DISTINCT mi.id) FROM media_items mi
        INNER JOIN media_copies mc ON mc.mediaItemId = mi.id
        WHERE mi.kind = :kind AND mc.state = 'ACTIVE'
        """,
    )
    suspend fun countByKind(kind: String): Long

    /** MediaItem senza alcuna copia (es. dopo CASCADE su sorgente rimossa). */
    @Query(
        """
        SELECT mi.id FROM media_items mi
        LEFT JOIN media_copies mc ON mc.mediaItemId = mi.id
        WHERE mc.id IS NULL
        """,
    )
    suspend fun listOrphanIds(): List<String>

    @Query("SELECT MAX(updatedAtEpochMs) FROM media_items")
    suspend fun latestUpdatedAt(): Long?

    @Query("SELECT * FROM media_items ORDER BY updatedAtEpochMs DESC LIMIT :limit")
    suspend fun list(limit: Int): List<MediaItemEntity>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun get(id: String): MediaItemEntity?

    @Query("SELECT * FROM media_items ORDER BY createdAtEpochMs ASC")
    suspend fun listAll(): List<MediaItemEntity>

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MediaCopyDao {
    @Upsert
    suspend fun upsert(entity: MediaCopyEntity)

    @Query(
        "SELECT * FROM media_copies WHERE storageLocationId = :storageLocationId AND opaqueLocator = :opaqueLocator LIMIT 1",
    )
    suspend fun findByLocator(storageLocationId: String, opaqueLocator: String): MediaCopyEntity?

    @Query("SELECT * FROM media_copies WHERE mediaItemId = :mediaItemId")
    suspend fun listByMediaItem(mediaItemId: String): List<MediaCopyEntity>

    @Query("SELECT * FROM media_copies WHERE id = :id LIMIT 1")
    suspend fun get(id: String): MediaCopyEntity?

    @Query("SELECT * FROM media_copies ORDER BY createdAtEpochMs DESC")
    suspend fun listAll(): List<MediaCopyEntity>

    @Query("SELECT * FROM media_copies WHERE state = :state ORDER BY createdAtEpochMs DESC")
    suspend fun listByState(state: String): List<MediaCopyEntity>

    @Query(
        "SELECT COUNT(*) FROM media_copies WHERE storageLocationId = :storageLocationId AND state = :state",
    )
    suspend fun countByLocationAndState(storageLocationId: String, state: String): Long

    @Query("DELETE FROM media_copies WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MediaFingerprintDao {
    @Upsert
    suspend fun upsert(entity: MediaFingerprintEntity)

    @Query("SELECT * FROM media_fingerprints")
    suspend fun listAll(): List<MediaFingerprintEntity>

    @Query("SELECT * FROM media_fingerprints WHERE mediaCopyId = :mediaCopyId")
    suspend fun listByMediaCopy(mediaCopyId: String): List<MediaFingerprintEntity>

    @Query(
        "SELECT * FROM media_fingerprints WHERE algorithm = :algorithm AND level = :level",
    )
    suspend fun listByAlgorithm(algorithm: String, level: Int): List<MediaFingerprintEntity>

    @Query("DELETE FROM media_fingerprints WHERE mediaCopyId = :mediaCopyId")
    suspend fun deleteByMediaCopy(mediaCopyId: String)
}

@Dao
interface ScanSessionDao {
    @Upsert
    suspend fun upsert(entity: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ScanSessionEntity?
}


@Dao
interface ImportSessionDao {
    @Upsert
    suspend fun upsert(entity: ImportSessionEntity)

    @Query("SELECT * FROM import_sessions WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ImportSessionEntity?
}
