package com.renatizzi.photovideomanager.domain.port

import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.ScanSession
import com.renatizzi.photovideomanager.domain.model.StorageLocation

/** Persistenza del Catalogo logico, indipendente dalla tecnologia fisica. */
interface CatalogStore {
    suspend fun countMediaItems(): Long
    suspend fun upsertArchive(archive: ArchiveRef)
    suspend fun listArchives(): List<ArchiveRef>
    suspend fun upsertStorageLocation(location: StorageLocation)
    suspend fun listStorageLocations(): List<StorageLocation>
    suspend fun getStorageLocation(id: String): StorageLocation?
    suspend fun deleteStorageLocation(id: String)
    suspend fun deleteArchive(id: String)
    suspend fun upsertMediaItem(item: MediaItem)
    suspend fun upsertMediaCopy(copy: MediaCopy)
    suspend fun findMediaCopyByLocator(storageLocationId: String, opaqueLocator: String): MediaCopy?
    suspend fun upsertFingerprint(fingerprint: MediaFingerprint)
    suspend fun listMediaItems(limit: Int = 100): List<MediaItem>
    suspend fun upsertScanSession(session: ScanSession)
    suspend fun getScanSession(id: String): ScanSession?
}
