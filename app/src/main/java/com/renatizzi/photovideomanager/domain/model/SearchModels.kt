package com.renatizzi.photovideomanager.domain.model

data class CatalogSearchEntry(
    val mediaItem: MediaItem,
    val previewCopy: MediaCopy?,
    val activeCopyCount: Int,
    val locationNames: List<String>,
    val inPersonalArchive: Boolean,
    /** Meta riga Catalogo (Nota bis): dispositivo / path / SD. */
    val deviceLabel: String = "",
    val pathLabel: String = "",
    val isSecondaryVolume: Boolean = false,
    val availability: Availability = Availability.AVAILABLE,
)

enum class SearchKindFilter {
    ALL,
    PHOTO,
    VIDEO,
}
