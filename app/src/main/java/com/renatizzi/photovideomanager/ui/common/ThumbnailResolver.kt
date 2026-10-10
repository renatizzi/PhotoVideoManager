package com.renatizzi.photovideomanager.ui.common

import android.content.Context
import android.net.Uri
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import java.io.File

/**
 * Risolve un modello caricabile da Coil (Uri/File) a partire da una MediaCopy.
 */
class ThumbnailResolver(
    appContext: Context,
    private val catalogStore: CatalogStore,
    private val personalRoot: File,
) {
    // appContext reserved for future content-resolver / FileProvider needs
    @Suppress("unused")
    private val appContext = appContext
    @Volatile
    private var locationCache: Map<String, StorageLocation> = emptyMap()

    suspend fun refreshLocations() {
        locationCache = catalogStore.listStorageLocations().associateBy { it.id }
    }

    suspend fun modelFor(copy: MediaCopy): Any? {
        if (locationCache.isEmpty()) refreshLocations()
        val location = locationCache[copy.storageLocationId]
            ?: catalogStore.getStorageLocation(copy.storageLocationId)?.also {
                locationCache = locationCache + (it.id to it)
            }
            ?: return null
        return modelFor(location, copy.opaqueLocator)
    }

    fun modelFor(location: StorageLocation, opaqueLocator: String): Any? {
        if (opaqueLocator.isBlank()) return null
        return when (location.adapterKind) {
            StorageAdapterKind.SAF_TREE -> {
                runCatching { Uri.parse(opaqueLocator) }.getOrNull()
            }
            StorageAdapterKind.LOCAL_FS -> {
                val relative = opaqueLocator.trim().removePrefix("/")
                val file = if (relative.isEmpty()) {
                    personalRoot
                } else {
                    File(personalRoot, relative)
                }
                file.takeIf { it.isFile && it.exists() }
            }
            StorageAdapterKind.MEDIA_STORE,
            StorageAdapterKind.SMB,
            -> null
        }
    }
}
