package com.renatizzi.photovideomanager.domain.model

/**
 * Snapshot KPI per la dashboard Home.
 */
data class DashboardSnapshot(
    val photoCount: Long,
    val videoCount: Long,
    /** Elementi con almeno una copia ACTIVE nello spazio app personale (dopo Importa). */
    val acquiredPhotoCount: Long = 0L,
    val acquiredVideoCount: Long = 0L,
    /**
     * Byte degli originali in Catalogo (copia preferita: spazio app se presente, altrimenti sorgente).
     * Valorizzato già dopo Acquisisci/CONFERMA.
     */
    val photoUsedBytes: Long = 0L,
    val videoUsedBytes: Long = 0L,
    /** Byte delle sole copie nello spazio app (sottoinsieme di photo/videoUsedBytes). */
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
