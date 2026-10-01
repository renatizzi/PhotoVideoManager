package com.renatizzi.photovideomanager.domain.model

/**
 * Riferimento logico a un archivio gestito dall'app.
 * Distinto da sorgente operativa e da StorageLocation concreta.
 */
data class ArchiveRef(
    val id: String,
    val displayName: String,
    val kind: ArchiveKind,
    val isSharedArchive: Boolean = false,
)

enum class ArchiveKind {
    PERSONAL_LOCAL,
    SHARED,
    EXTERNAL_SOURCE,
}
