package com.renatizzi.photovideomanager.domain.port

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import java.io.InputStream

/**
 * Accesso fisico a archivi/sorgenti. Il Domain non vede path/tecnologia.
 * Capability non uniformi: l'assenza degrada in modo esplicito.
 */
interface StorageAdapter {
    val adapterId: String
    fun capabilities(): Set<StorageCapability>
    suspend fun availability(): Availability
    suspend fun listChildren(opaqueLocator: String): List<StorageEntry>
    suspend fun openRead(opaqueLocator: String): InputStream
    suspend fun readMetadata(opaqueLocator: String): StorageMetadata?
}

data class StorageEntry(
    val opaqueLocator: String,
    val displayName: String,
    val isDirectory: Boolean,
)

data class StorageMetadata(
    val byteSize: Long?,
    val mimeType: String?,
    val lastModifiedEpochMs: Long?,
)

class MissingCapabilityException(
    val capability: StorageCapability,
    adapterId: String,
) : IllegalStateException("Adapter $adapterId manca capability $capability")
