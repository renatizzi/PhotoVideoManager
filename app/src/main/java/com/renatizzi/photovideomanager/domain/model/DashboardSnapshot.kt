package com.renatizzi.photovideomanager.domain.model

/**
 * Snapshot KPI per la dashboard Home.
 */
data class DashboardSnapshot(
    val photoCount: Long,
    val videoCount: Long,
    val duplicatePhotoCount: Long? = null,
    val duplicateVideoCount: Long? = null,
    val trashCount: Long = 0L,
    val personalUsedBytes: Long,
    val lastUpdatedEpochMs: Long?,
    val localAvailability: Availability,
)
