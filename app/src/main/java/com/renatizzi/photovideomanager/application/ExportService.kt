package com.renatizzi.photovideomanager.application

import android.content.Context
import android.net.Uri
import com.renatizzi.photovideomanager.data.storage.StorageAdapterFactory
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.port.CatalogStore
import com.renatizzi.photovideomanager.domain.port.PermissionGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Export v1: copia una MediaCopy ACTIVE (preferenza spazio app) verso un Uri SAF/CreateDocument.
 */
class ExportService(
    private val appContext: Context,
    private val catalogStore: CatalogStore,
    private val adapterFactory: StorageAdapterFactory,
    private val permissionGate: PermissionGate,
) {
    suspend fun exportMediaItemToUri(mediaItemId: String, destUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(permissionGate.canView(DomainScope.PERSONAL))
                val item = catalogStore.getMediaItem(mediaItemId)
                    ?: error("Elemento non trovato")
                val copies = catalogStore.listMediaCopiesForItem(mediaItemId)
                    .filter { it.state == MediaCopyState.ACTIVE }
                val copy = copies.firstOrNull {
                    it.storageLocationId == CatalogFacade.PERSONAL_LOCATION_ID
                } ?: copies.firstOrNull()
                    ?: error("Nessuna copia disponibile da esportare")
                val location = catalogStore.getStorageLocation(copy.storageLocationId)
                    ?: error("Ubicazione non disponibile")
                val adapter = adapterFactory.create(location)
                adapter.openRead(copy.opaqueLocator).use { input ->
                    val out = appContext.contentResolver.openOutputStream(destUri)
                        ?: error("Impossibile scrivere sulla destinazione")
                    out.use { output -> input.copyTo(output) }
                }
                item.displayTitle?.ifBlank { null } ?: "Esportazione completata"
            }
        }
}
