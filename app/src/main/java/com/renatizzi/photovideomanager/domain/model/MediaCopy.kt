package com.renatizzi.photovideomanager.domain.model

/**
 * Copia fisica di un MediaItem su una StorageLocation.
 * Non costituisce proprietà familiare autonoma.
 */
data class MediaCopy(
    val id: String,
    val mediaItemId: String,
    val storageLocationId: String,
    val byteSize: Long? = null,
    val mimeType: String? = null,
    val state: MediaCopyState = MediaCopyState.ACTIVE,
    val createdAtEpochMs: Long,
)

enum class MediaCopyState {
    ACTIVE,
    STAGED,
    INVALID,
    TRASHED,
}
