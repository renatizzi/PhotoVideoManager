package com.renatizzi.photovideomanager.domain.model

/** Sintesi UI/application di una sorgente/archivio registrato. */
data class SourceSummary(
    val locationId: String,
    val archiveId: String,
    val displayName: String,
    /** Dispositivo di riferimento (es. Questo telefono, Spazio app). */
    val deviceLabel: String,
    /** Percorso/URI leggibile della sorgente. */
    val pathLabel: String,
    val adapterKind: StorageAdapterKind,
    val availability: Availability,
    val isSharedArchive: Boolean,
    val isBuiltInPersonal: Boolean,
)

/** Stato selezione sorgente per il censimento (icone UI). */
enum class SourceCensusSelection {
    /** 🟩 da selezionare */
    TO_SELECT,
    /** ✅ selezionato per il censimento */
    SELECTED,
    /** ❌ selezione rimossa */
    REMOVED,
}

enum class SourceSortMode {
    NAME,
    DEVICE,
    AVAILABILITY,
}

enum class MediaBrowseSortMode {
    NAME,
    KIND,
    NEWEST,
}
