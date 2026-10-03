package com.renatizzi.photovideomanager.domain.model

/**
 * Sessione di censimento (fondamenta M05).
 * Ripresa solo su azione utente; nessun retry automatico.
 */
data class ScanSession(
    val id: String,
    val sourceLocationId: String,
    val state: ScanSessionState,
    val startedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val itemsSeen: Int = 0,
    val lastError: String? = null,
)

enum class ScanSessionState {
    CREATED,
    RUNNING,
    PAUSED_BY_USER,
    COMPLETED,
    FAILED,
    CANCELLED,
}
