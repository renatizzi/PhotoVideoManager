package com.renatizzi.photovideomanager.domain.model

/**
 * Collocazione concreta di una MediaCopy, espressa con riferimento opaco al Domain.
 * Non espone path/filesystem al Domain: opaqueLocator è interpretato solo dall'adapter.
 */
data class StorageLocation(
    val id: String,
    val archiveId: String,
    val displayName: String,
    val adapterKind: StorageAdapterKind,
    val opaqueLocator: String,
    val availability: Availability = Availability.UNKNOWN,
)
