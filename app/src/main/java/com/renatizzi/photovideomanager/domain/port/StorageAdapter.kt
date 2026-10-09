package com.renatizzi.photovideomanager.domain.port

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import java.io.InputStream

interface StorageAdapter {
    val adapterId: String
    fun capabilities(): Set<StorageCapability>
    suspend fun availability(): Availability
    suspend fun listChildren(opaqueLocator: String): List<StorageEntry>
    suspend fun openRead(opaqueLocator: String): InputStream
    suspend fun readMetadata(opaqueLocator: String): StorageMetadata?
    /**
     * Scrive una copia da stream in [parentOpaqueLocator] con nome [fileName].
     * Ritorna il nuovo opaqueLocator. Richiede capability WRITE.
     */
    suspend fun writeCopy(
        parentOpaqueLocator: String,
        fileName: String,
        source: InputStream,
    ): String

    /**
     * Elimina il file fisico indicato da [opaqueLocator].
     * Richiede capability DELETE. Usare con conferma utente.
     */
    suspend fun delete(opaqueLocator: String)
}

data class StorageEntry(
    val opaqueLocator: String,
    val displayName: String,
    val isDirectory: Boolean,
    /** MIME tipizzato dall'adapter quando disponibile (es. SAF listing). */
    val mimeType: String? = null,
    /** Dimensione dal listing (es. SAF COLUMN_SIZE), se disponibile. */
    val byteSize: Long? = null,
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
