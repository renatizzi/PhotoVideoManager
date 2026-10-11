package com.renatizzi.photovideomanager.domain.model

/**
 * Snapshot KPI per la dashboard Home (modello A — BL-14).
 *
 * - [photoCount] / [videoCount] = **originali sui dispositivi/fonti**
 *   (copie non-personali conosciute, ACTIVE o TRASHED: il file fisico resta).
 * - [catalogPhotoCount] / [catalogVideoCount] = **di cui in Catalogo**
 *   (MediaItem con almeno una copia ACTIVE = elenco Aggiorna).
 * - Entra in Catalogo con **CONFERMA** (censimento), non con Importa.
 * - [acquiredPhotoCount] = copie nello spazio app dopo **Importa** (riepilogo Acquisisci).
 */
data class DashboardSnapshot(
    val photoCount: Long,
    val videoCount: Long,
    /** Elementi nel Catalogo (ACTIVE), mostrati come «di cui in Catalogo». */
    val catalogPhotoCount: Long = 0L,
    val catalogVideoCount: Long = 0L,
    /** Copie ACTIVE nello spazio app personale (dopo Importa). */
    val acquiredPhotoCount: Long = 0L,
    val acquiredVideoCount: Long = 0L,
    /**
     * Byte degli originali in Catalogo (copia preferita: spazio app se presente, altrimenti sorgente).
     */
    val photoUsedBytes: Long = 0L,
    val videoUsedBytes: Long = 0L,
    /** Byte delle sole copie nello spazio app. */
    val acquiredPhotoBytes: Long = 0L,
    val acquiredVideoBytes: Long = 0L,
    /** Extra esatti (SHA-256) oltre al consigliato, per kind. */
    val duplicatePhotoCount: Long? = null,
    val duplicateVideoCount: Long? = null,
    val trashCount: Long = 0L,
    val personalUsedBytes: Long,
    val lastUpdatedEpochMs: Long?,
    val localAvailability: Availability,
)
