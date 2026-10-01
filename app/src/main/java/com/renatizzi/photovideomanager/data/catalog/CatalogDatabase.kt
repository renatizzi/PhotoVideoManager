package com.renatizzi.photovideomanager.data.catalog

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ArchiveEntity::class,
        StorageLocationEntity::class,
        MediaItemEntity::class,
        MediaCopyEntity::class,
        MediaFingerprintEntity::class,
        ScanSessionEntity::class,
        ImportSessionEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao
    abstract fun storageLocationDao(): StorageLocationDao
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun mediaCopyDao(): MediaCopyDao
    abstract fun mediaFingerprintDao(): MediaFingerprintDao
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun importSessionDao(): ImportSessionDao

    companion object {
        fun create(context: Context): CatalogDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                CatalogDatabase::class.java,
                "pvm_catalog.db",
            )
                // Early development: schema still evolving; no production data to preserve yet.
                .fallbackToDestructiveMigration()
                .build()
    }
}
