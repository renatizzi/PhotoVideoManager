package com.renatizzi.photovideomanager.data.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArchiveEntity)

    @Query("SELECT * FROM archives ORDER BY displayName")
    suspend fun list(): List<ArchiveEntity>

    @Query("DELETE FROM archives WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface StorageLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaItemEntity)

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun count(): Long

    @Query("SELECT * FROM media_items ORDER BY updatedAtEpochMs DESC LIMIT :limit")
    suspend fun list(limit: Int): List<MediaItemEntity>
}

@Dao
interface MediaCopyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaCopyEntity)

    @Query(
        "SELECT * FROM media_copies WHERE storageLocationId = :storageLocationId AND opaqueLocator = :opaqueLocator LIMIT 1",
    )
    suspend fun findByLocator(storageLocationId: String, opaqueLocator: String): MediaCopyEntity?
}

@Dao
interface MediaFingerprintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaFingerprintEntity)
}

@Dao
interface ScanSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ScanSessionEntity?
}
