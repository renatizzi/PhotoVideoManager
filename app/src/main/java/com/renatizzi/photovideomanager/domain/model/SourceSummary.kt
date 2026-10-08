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

/** Stato selezione sorgente (Nota v5.2: solo on/off). */
enum class SourceCensusSelection {
    /** off / non selezionato */
    NOT_SELECTED,
    /** on / selezionato */
    SELECTED,
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
