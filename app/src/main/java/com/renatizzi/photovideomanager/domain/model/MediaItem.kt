package com.renatizzi.photovideomanager.domain.model

/**
 * Identità logica del contenuto. Il domainScope appartiene al MediaItem.
 */
data class MediaItem(
    val id: String,
    val kind: MediaKind,
    val domainScope: DomainScope,
    val capturedAtEpochMs: Long? = null,
    val displayTitle: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
