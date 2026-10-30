package com.renatizzi.photovideomanager.domain.hub

enum class CatalogSeatKind {
    NONE,
    LOCAL_DEVICE,
    PC_NOTEBOOK,
    CLOUD_DRIVE,
    NETWORK_NAS,
}

data class CatalogSeat(
    val kind: CatalogSeatKind,
    val treeUri: String? = null,
    /** Etichetta percorso / share (es. Pictures/Catalogo o \\host\share). */
    val displayLabel: String = "",
    /** Nome convenzionale del Catalogo (default MediaManagerCatalog). */
    val catalogName: String = DEFAULT_CATALOG_NAME,
    /** Modello / nome dispositivo mostrato in riga (es. S25 Ultra, PC-UFFICIO). */
    val deviceModel: String = "",
    /** Memoria locale: Principale o Secondaria (microSD); vuoto se remoto. */
    val memoryLabel: String = "",
    val configuredAtEpochMs: Long = 0L,
) {
    val isConfigured: Boolean
        get() = kind != CatalogSeatKind.NONE &&
            (kind == CatalogSeatKind.LOCAL_DEVICE || !treeUri.isNullOrBlank())

    /** Nome Catalogo non vuoto per UI. */
    val effectiveCatalogName: String
        get() = catalogName.ifBlank { DEFAULT_CATALOG_NAME }

    companion object {
        const val DEFAULT_CATALOG_NAME = "MediaManagerCatalog"
        const val MEMORY_PRIMARY = "Principale"
        const val MEMORY_SECONDARY = "Secondaria"
    }
}
