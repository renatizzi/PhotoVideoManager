package com.renatizzi.photovideomanager.data.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ArchiveEntity)
}

@Dao
interface StorageLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StorageLocationEntity)
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
}

@Dao
interface MediaFingerprintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaFingerprintEntity)
}
