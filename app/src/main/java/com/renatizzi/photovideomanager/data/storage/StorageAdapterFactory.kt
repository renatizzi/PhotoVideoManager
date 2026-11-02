package com.renatizzi.photovideomanager.data.storage

import android.content.Context
import android.net.Uri
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import com.renatizzi.photovideomanager.domain.model.StorageLocation
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import java.io.File

class StorageAdapterFactory(
    private val context: Context,
    private val personalRoot: File,
) {
    fun create(location: StorageLocation): StorageAdapter =
        when (location.adapterKind) {
            StorageAdapterKind.LOCAL_FS -> LocalFilesystemStorageAdapter(personalRoot)
            StorageAdapterKind.SAF_TREE -> SafTreeStorageAdapter(
                context = context,
                // Sottocartella espansa: opaque = tree/parent/document/child → adapter sul parent
                // (permesso persistente del pick). Foglia pick diretto: tree URI della cartella.
                treeUri = SafTreeIdentity.adapterTreeUri(Uri.parse(location.opaqueLocator)),
            )
            StorageAdapterKind.MEDIA_STORE,
            StorageAdapterKind.SMB,
            -> error("Adapter ${location.adapterKind} non ancora implementato")
        }
}
