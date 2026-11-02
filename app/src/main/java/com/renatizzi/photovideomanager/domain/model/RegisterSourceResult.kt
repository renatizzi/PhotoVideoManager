package com.renatizzi.photovideomanager.domain.model

/**
 * Esito aggiunta fonte SAF (BL-10): nuova oppure già in elenco.
 *
 * NOVITÀ Nota 0.16.57: se la cartella scelta è una root con sottocartelle che
 * contengono media, [additionalSources] elenca le altre righe registrate
 * (corrispondenza biunivoca cartella/media). [source] è sempre la prima.
 */
data class RegisterSourceResult(
    val source: SourceSummary,
    val alreadyPresent: Boolean,
    val additionalSources: List<SourceSummary> = emptyList(),
) {
    /** Tutte le fonti coinvolte (prima + aggiuntive), nell’ordine di registrazione. */
    val allSources: List<SourceSummary>
        get() = listOf(source) + additionalSources
}
