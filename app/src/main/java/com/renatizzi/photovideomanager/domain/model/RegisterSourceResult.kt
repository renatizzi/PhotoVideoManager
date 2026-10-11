package com.renatizzi.photovideomanager.domain.model

/** Esito aggiunta fonte SAF (BL-10): nuova oppure già in elenco. */
data class RegisterSourceResult(
    val source: SourceSummary,
    val alreadyPresent: Boolean,
)
