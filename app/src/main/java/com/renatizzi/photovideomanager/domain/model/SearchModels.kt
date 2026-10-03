package com.renatizzi.photovideomanager.domain.model

data class CatalogSearchEntry(
    val mediaItem: MediaItem,
    val activeCopyCount: Int,
    val locationNames: List<String>,
    val inPersonalArchive: Boolean,
)

enum class SearchKindFilter {
    ALL,
    PHOTO,
    VIDEO,
}
