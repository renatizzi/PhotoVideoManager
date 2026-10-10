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

/**
 * Stato selezione sorgente in Acquisisci (BL-03): riquadro a tre stati.
 * - [SELECTED]: ✓ nel riquadro → usata in Conferma/Importa
 * - [NOT_SELECTED]: riquadro vuoto → resta in elenco, non usata
 * - [EXCLUDED]: X nel riquadro → tolta dall’elenco (dopo conferma)
 */
enum class SourceCensusSelection {
    /** on / selezionato (✓) */
    SELECTED,
    /** off / non selezionato (vuoto) */
    NOT_SELECTED,
    /** escluso / rimosso (X) — niente icona cestino separata */
    EXCLUDED,
}

/** Ciclo BL-03: ✓ → vuoto → X → ✓ */
fun SourceCensusSelection.cycleNext(): SourceCensusSelection = when (this) {
    SourceCensusSelection.SELECTED -> SourceCensusSelection.NOT_SELECTED
    SourceCensusSelection.NOT_SELECTED -> SourceCensusSelection.EXCLUDED
    SourceCensusSelection.EXCLUDED -> SourceCensusSelection.SELECTED
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
