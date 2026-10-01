package com.renatizzi.photovideomanager.domain.port

import com.renatizzi.photovideomanager.domain.model.ArchiveRef
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.StorageLocation

/** Persistenza del Catalogo logico, indipendente dalla tecnologia fisica. */
interface CatalogStore {
    suspend fun countMediaItems(): Long
    suspend fun upsertArchive(archive: ArchiveRef)
    suspend fun upsertStorageLocation(location: StorageLocation)
    suspend fun upsertMediaItem(item: MediaItem)
    suspend fun upsertMediaCopy(copy: MediaCopy)
    suspend fun upsertFingerprint(fingerprint: MediaFingerprint)
    suspend fun listMediaItems(limit: Int = 100): List<MediaItem>
}
