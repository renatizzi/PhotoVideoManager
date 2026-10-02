package com.renatizzi.photovideomanager.domain.model

/**
 * Snapshot KPI per la dashboard Home.
 * I duplicati restano null finché non arriva M07 (Pulisci).
 */
data class DashboardSnapshot(
    val photoCount: Long,
    val videoCount: Long,
    val duplicatePhotoCount: Long? = null,
    val duplicateVideoCount: Long? = null,
    val personalUsedBytes: Long,
    val lastUpdatedEpochMs: Long?,
    val localAvailability: Availability,
)
