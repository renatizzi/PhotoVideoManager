package com.renatizzi.photovideomanager.domain.model

data class TrashEntry(
    val mediaItem: MediaItem,
    /** Copia usata per anteprima/etichetta (preferenza: file del dispositivo). */
    val mediaCopy: MediaCopy,
    val locationName: String,
    /** True se tra le copie in Cestino c’è anche quella gestita dall’app. */
    val isPersonalArchive: Boolean,
    /** Tutte le copie in Cestino dello stesso elemento di Catalogo. */
    val trashedCopyIds: List<String> = emptyList(),
)

data class TrashActionResult(
    val affected: Int,
    val message: String? = null,
)

data class ArchiveEntry(
    val mediaItem: MediaItem,
    val mediaCopy: MediaCopy,
)
