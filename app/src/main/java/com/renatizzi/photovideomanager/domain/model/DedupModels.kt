package com.renatizzi.photovideomanager.domain.model

/**
 * Gruppo di duplicati esatti (stesso fingerprint SHA-256) su MediaItem distinti.
 * Nessuna azione automatica: solo evidenza per revisione utente (M07).
 */
data class DuplicateMember(
    val mediaItem: MediaItem,
    val mediaCopy: MediaCopy,
    val locationName: String,
    val isSuggestedKeep: Boolean,
)

data class DuplicateGroup(
    val fingerprintValue: String,
    val algorithm: String,
    val members: List<DuplicateMember>,
) {
    val distinctItemCount: Int get() = members.map { it.mediaItem.id }.toSet().size
    val extraItemCount: Int get() = (distinctItemCount - 1).coerceAtLeast(0)
}

data class DedupAnalysisResult(
    val groups: List<DuplicateGroup>,
    val copiesScanned: Int,
    val hashesComputed: Int,
    val hashesFailed: Int,
    val duplicatePhotoExtras: Long,
    val duplicateVideoExtras: Long,
    val message: String? = null,
)
