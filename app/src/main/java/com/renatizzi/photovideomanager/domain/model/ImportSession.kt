package com.renatizzi.photovideomanager.domain.model

/**
 * Sessione di acquisizione (M06).
 * Ripresa solo su azione utente; nessun retry/queue automatici.
 */
data class ImportSession(
    val id: String,
    val destinationLocationId: String,
    val state: ImportSessionState,
    val startedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val itemsTotal: Int = 0,
    val itemsDone: Int = 0,
    val itemsFailed: Int = 0,
    val lastError: String? = null,
)

enum class ImportSessionState {
    CREATED,
    PREFLIGHT,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

data class AcquireCandidate(
    val mediaItem: MediaItem,
    val sourceCopy: MediaCopy,
    /** Riga unica legacy / fallback (dispositivo · percorso). */
    val sourceLocationName: String,
    /** Come in Acquisisci: dispositivo sotto il nome file. */
    val sourceDeviceLabel: String = "",
    /** Percorso completo senza ripetere il nome file. */
    val sourcePathLabel: String = "",
    val alreadyInPersonalArchive: Boolean,
)

data class AcquireResult(
    val sessionId: String,
    val state: ImportSessionState,
    val acquired: Int,
    val skippedAlreadyPresent: Int,
    val failed: Int,
    val message: String? = null,
)
