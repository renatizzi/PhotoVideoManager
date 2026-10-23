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
 * Riquadro a tre stati (liste Acquisisci / Aggiorna).
 * - [SELECTED]: ✓
 * - [NOT_SELECTED]: vuoto
 * - [EXCLUDED]: X (rimozione / Elimina → Cestino; niente icona cestino separata)
 *
 * Default: Acquisisci = [SELECTED]; Aggiorna = [NOT_SELECTED] (filtri utilizzabili).
 */
enum class SourceCensusSelection {
    /** on / selezionato (✓) */
    SELECTED,
    /** off / non selezionato (vuoto) */
    NOT_SELECTED,
    /** escluso / rimosso (X) */
    EXCLUDED,
}

/**
 * Ciclo riquadro Acquisisci: solo vuoto ↔ X.
 * Il ✓ si ottiene di nuovo solo con «Tutti» (non dal riquadro).
 * Se si parte da ✓ (default / Tutti), il primo tocco va a vuoto.
 */
fun SourceCensusSelection.cycleNext(): SourceCensusSelection = when (this) {
    SourceCensusSelection.SELECTED -> SourceCensusSelection.NOT_SELECTED
    SourceCensusSelection.NOT_SELECTED -> SourceCensusSelection.EXCLUDED
    SourceCensusSelection.EXCLUDED -> SourceCensusSelection.NOT_SELECTED
}

/** Ciclo Aggiorna (default vuoto): vuoto → ✓ → X → vuoto */
fun SourceCensusSelection.cycleNextCatalog(): SourceCensusSelection = when (this) {
    SourceCensusSelection.NOT_SELECTED -> SourceCensusSelection.SELECTED
    SourceCensusSelection.SELECTED -> SourceCensusSelection.EXCLUDED
    SourceCensusSelection.EXCLUDED -> SourceCensusSelection.NOT_SELECTED
}

enum class SourceSortMode {
    NAME,
    DEVICE,
    AVAILABILITY,
}

/** Gruppo UI: un dispositivo con le sue fonti (Acquisisci Grado 1). */
data class DeviceSourceGroup(
    val deviceLabel: String,
    val sources: List<SourceSummary>,
) {
    val availableCount: Int get() = sources.count { it.availability == Availability.AVAILABLE }
}

enum class MediaBrowseSortMode {
    NAME,
    KIND,
    NEWEST,
}
