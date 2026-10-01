package com.renatizzi.photovideomanager.data.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.port.MissingCapabilityException
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import com.renatizzi.photovideomanager.domain.port.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Adapter SAF Document Tree.
 * Capability parziali: move/rename non atomici; non assumere WRITE uniforme.
 */
class SafTreeStorageAdapter(
    private val context: Context,
    private val treeUri: Uri,
) : StorageAdapter {
    override val adapterId: String = "saf-tree"

    private val caps = setOf(
        StorageCapability.LIST,
        StorageCapability.READ,
        StorageCapability.WRITE,
        StorageCapability.CREATE_DIRECTORY,
        StorageCapability.DELETE,
        StorageCapability.SEQUENTIAL_READ,
        StorageCapability.METADATA,
        StorageCapability.FINGERPRINT_STREAM,
        StorageCapability.AVAILABILITY,
        // RENAME/MOVE/RANDOM_READ: parziali su SAF — non esposte come garantite.
    )

    override fun capabilities(): Set<StorageCapability> = caps

    override suspend fun availability(): Availability = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri)
        when {
            root == null -> Availability.UNKNOWN
            root.exists() && root.canRead() -> Availability.AVAILABLE
            else -> Availability.UNAVAILABLE
        }
    }

    override suspend fun listChildren(opaqueLocator: String): List<StorageEntry> =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.LIST)
            val dir = resolve(opaqueLocator) ?: return@withContext emptyList()
            if (!dir.isDirectory) return@withContext emptyList()
            dir.listFiles().mapNotNull { file ->
                val uri = file.uri.toString()
                StorageEntry(
                    opaqueLocator = uri,
                    displayName = file.name ?: uri.substringAfterLast('/'),
                    isDirectory = file.isDirectory,
                )
            }
        }

    override suspend fun openRead(opaqueLocator: String): InputStream =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.READ)
            val uri = Uri.parse(opaqueLocator.ifBlank { treeUri.toString() })
            context.contentResolver.openInputStream(uri)
                ?: error("Impossibile aprire in lettura: $opaqueLocator")
        }

    override suspend fun readMetadata(opaqueLocator: String): StorageMetadata? =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.METADATA)
            val file = resolve(opaqueLocator) ?: return@withContext null
            if (!file.exists()) return@withContext null
            StorageMetadata(
                byteSize = file.length().takeIf { it >= 0 },
                mimeType = file.type,
                lastModifiedEpochMs = file.lastModified().takeIf { it > 0 },
            )
        }

    private fun requireCapability(capability: StorageCapability) {
        if (capability !in caps) {
            throw MissingCapabilityException(capability, adapterId)
        }
    }

    private fun resolve(opaqueLocator: String): DocumentFile? {
        if (opaqueLocator.isBlank() || opaqueLocator == treeUri.toString()) {
            return DocumentFile.fromTreeUri(context, treeUri)
        }
        return DocumentFile.fromSingleUri(context, Uri.parse(opaqueLocator))
    }
}
