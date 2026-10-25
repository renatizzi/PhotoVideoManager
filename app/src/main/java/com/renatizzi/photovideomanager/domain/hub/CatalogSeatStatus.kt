package com.renatizzi.photovideomanager.domain.hub

/**
 * Stato UI della Posizione del Catalogo (contratto comune Dashboard / Organizza /
 * Acquisisci / Aggiorna). Non è una scansione rete: solo probe della sede già scelta.
 */
enum class CatalogSeatReachability {
    /** Nessuna Posizione impostata. */
    NOT_CONFIGURED,
    /** Configurata e probe riuscito in questa sessione. */
    REACHABLE,
    /** Configurata ma probe fallito (PC spento, Wi‑Fi, permessi, …). */
    UNREACHABLE,
    /** Configurata ma non ancora / non verificabile ora. */
    UNKNOWN,
}

data class CatalogSeatStatus(
    val seat: CatalogSeat,
    val reachability: CatalogSeatReachability,
) {
    val configured: Boolean get() = seat.isConfigured
    val label: String get() = seat.displayLabel
    val kind: CatalogSeatKind get() = seat.kind
}

object CatalogSeatKindLabels {
    fun label(kind: CatalogSeatKind): String = when (kind) {
        CatalogSeatKind.LOCAL_DEVICE -> "Su questo dispositivo"
        CatalogSeatKind.PC_NOTEBOOK -> "Su PC / notebook"
        CatalogSeatKind.CLOUD_DRIVE -> "Su Google Drive / cloud"
        CatalogSeatKind.NETWORK_NAS -> "Su disco di rete / NAS"
        CatalogSeatKind.NONE -> ""
    }
}
