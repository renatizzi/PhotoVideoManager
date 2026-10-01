package com.renatizzi.photovideomanager.domain.model

/** Sintesi UI/application di una sorgente/archivio registrato. */
data class SourceSummary(
    val locationId: String,
    val archiveId: String,
    val displayName: String,
    val adapterKind: StorageAdapterKind,
    val availability: Availability,
    val isSharedArchive: Boolean,
    val isBuiltInPersonal: Boolean,
)
