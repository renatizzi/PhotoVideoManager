package com.renatizzi.photovideomanager.domain.model

data class TrashEntry(
    val mediaItem: MediaItem,
    val mediaCopy: MediaCopy,
    val locationName: String,
    val isPersonalArchive: Boolean,
)

data class TrashActionResult(
    val affected: Int,
    val message: String? = null,
)

data class ArchiveEntry(
    val mediaItem: MediaItem,
    val mediaCopy: MediaCopy,
)
