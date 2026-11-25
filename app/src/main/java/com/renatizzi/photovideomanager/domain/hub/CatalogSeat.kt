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
    /** Etichetta percorso / share (es. Pictures/Catalogo o C:\MyCat\MediaManager). */
    val displayLabel: String = "",
    /** Nome convenzionale del Catalogo (default MyMedia). */
    val catalogName: String = DEFAULT_CATALOG_NAME,
    /** Modello / nome dispositivo (telefono o etichetta locale); non usato in riga sede PC. */
    val deviceModel: String = "",
    /** Memoria locale: Principale o Secondaria (microSD); vuoto se remoto. */
    val memoryLabel: String = "",
    /**
     * Path Windows della cartella Catalogo (es. `C:\MyCat`), senza sottocartella servizio.
     * Solo sede PC; vuoto altrimenti.
     */
    val windowsFolderPath: String = "",
    val configuredAtEpochMs: Long = 0L,
) {
    val isConfigured: Boolean
        get() = kind != CatalogSeatKind.NONE &&
            (kind == CatalogSeatKind.LOCAL_DEVICE || !treeUri.isNullOrBlank())

    /** Nome Catalogo non vuoto per UI. */
    val effectiveCatalogName: String
        get() = catalogName.ifBlank { DEFAULT_CATALOG_NAME }

    /** Modello dispositivo non vuoto per UI. */
    val effectiveDeviceModel: String
        get() = deviceModel.ifBlank { DEFAULT_DEVICE_MODEL }

    /**
     * Percorso sede da mostrare in riga Catalogo.
     * PC: `C:\Nome\MediaManager` (fascicolo hub); altrimenti [displayLabel].
     */
    val seatPathForDisplay: String
        get() {
            if (kind != CatalogSeatKind.PC_NOTEBOOK) {
                return displayLabel.ifBlank { effectiveCatalogName }
            }
            val base = windowsFolderPath.trim().trimEnd('\\', '/')
                .ifBlank {
                    val fromLabel = displayLabel.trim()
                    when {
                        fromLabel.matches(Regex("""^[A-Za-z]:\\.+""", RegexOption.IGNORE_CASE)) ->
                            fromLabel.substringBeforeLast("\\${HubPackage.RELATIVE_DIR}", fromLabel)
                                .trimEnd('\\')
                        else -> "C:\\$effectiveCatalogName"
                    }
                }
            val hub = HubPackage.RELATIVE_DIR
            return if (base.endsWith("\\$hub", ignoreCase = true)) base else "$base\\$hub"
        }

    companion object {
        const val DEFAULT_CATALOG_NAME = "MyMedia"
        const val DEFAULT_DEVICE_MODEL = "MyDevice"
        const val MEMORY_PRIMARY = "Principale"
        const val MEMORY_SECONDARY = "Secondaria"
    }
}
