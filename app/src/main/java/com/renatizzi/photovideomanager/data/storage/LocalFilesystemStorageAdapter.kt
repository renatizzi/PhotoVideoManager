package com.renatizzi.photovideomanager.data.storage

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.port.MissingCapabilityException
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import com.renatizzi.photovideomanager.domain.port.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

/**
 * Adapter filesystem locale (app-private / path opaque).
 * SAF/MediaStore/SMB arriveranno come adapter distinti M04.
 */
class LocalFilesystemStorageAdapter(
    private val rootDirectory: File,
) : StorageAdapter {
    override val adapterId: String = "local-fs"

    private val caps = setOf(
        StorageCapability.LIST,
        StorageCapability.READ,
        StorageCapability.WRITE,
        StorageCapability.CREATE_DIRECTORY,
        StorageCapability.RENAME,
        StorageCapability.MOVE,
        StorageCapability.DELETE,
        StorageCapability.RANDOM_READ,
        StorageCapability.SEQUENTIAL_READ,
        StorageCapability.METADATA,
        StorageCapability.FINGERPRINT_STREAM,
        StorageCapability.AVAILABILITY,
    )

    override fun capabilities(): Set<StorageCapability> = caps

    override suspend fun availability(): Availability = withContext(Dispatchers.IO) {
        if (rootDirectory.exists() || rootDirectory.mkdirs()) {
            Availability.AVAILABLE
        } else {
            Availability.UNAVAILABLE
        }
    }

    override suspend fun listChildren(opaqueLocator: String): List<StorageEntry> =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.LIST)
            val dir = resolve(opaqueLocator)
            if (!dir.isDirectory) return@withContext emptyList()
            dir.listFiles()?.map { file ->
                StorageEntry(
                    opaqueLocator = relativize(file),
                    displayName = file.name,
                    isDirectory = file.isDirectory,
                )
            }.orEmpty()
        }

    override suspend fun openRead(opaqueLocator: String): InputStream =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.READ)
            resolve(opaqueLocator).inputStream()
        }

    override suspend fun readMetadata(opaqueLocator: String): StorageMetadata? =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.METADATA)
            val file = resolve(opaqueLocator)
            if (!file.exists()) return@withContext null
            StorageMetadata(
                byteSize = file.length(),
                mimeType = null,
                lastModifiedEpochMs = file.lastModified(),
            )
        }

    private fun requireCapability(capability: StorageCapability) {
        if (capability !in caps) {
            throw MissingCapabilityException(capability, adapterId)
        }
    }

    private fun resolve(opaqueLocator: String): File {
        val normalized = opaqueLocator.trim().removePrefix("/")
        val target = if (normalized.isEmpty()) rootDirectory else File(rootDirectory, normalized)
        val canonicalRoot = rootDirectory.canonicalFile
        val canonicalTarget = target.canonicalFile
        require(canonicalTarget.path.startsWith(canonicalRoot.path)) {
            "Locator fuori dal root dell'adapter"
        }
        return canonicalTarget
    }

    private fun relativize(file: File): String {
        val root = rootDirectory.canonicalFile.path
        val path = file.canonicalFile.path
        return path.removePrefix(root).removePrefix("/")
    }
}
