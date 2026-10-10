package com.renatizzi.photovideomanager.domain.model

/**
 * Evidenza tecnica di riconoscimento. Non determina azioni automatiche.
 * Livello L0–L2 lasciato flessibile per M05/M07.
 */
data class MediaFingerprint(
    val id: String,
    val mediaCopyId: String,
    val algorithm: String,
    val level: Int,
    val value: String,
    val computedAtEpochMs: Long,
)
