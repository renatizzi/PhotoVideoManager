package com.renatizzi.photovideomanager.domain.hub

/**
 * Stato UI della Posizione del Catalogo (contratto comune Dashboard /
 * Acquisisci / Aggiorna / Componi). Non è una scansione rete: solo probe della sede già scelta.
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
    val catalogName: String get() = seat.effectiveCatalogName
    val deviceModel: String get() = seat.deviceModel
    val memoryLabel: String get() = seat.memoryLabel
}

object CatalogSeatKindLabels {
    fun label(kind: CatalogSeatKind): String = when (kind) {
        CatalogSeatKind.LOCAL_DEVICE -> "Questo dispositivo/Tablet"
        CatalogSeatKind.PC_NOTEBOOK -> "PC / notebook"
        CatalogSeatKind.CLOUD_DRIVE -> "Google Drive / Cloud"
        CatalogSeatKind.NETWORK_NAS -> "NAS / disco di rete"
        CatalogSeatKind.NONE -> ""
    }

    fun shortDevice(kind: CatalogSeatKind): String = when (kind) {
        CatalogSeatKind.LOCAL_DEVICE -> "Questo dispositivo/Tablet"
        CatalogSeatKind.PC_NOTEBOOK -> "PC / notebook"
        CatalogSeatKind.CLOUD_DRIVE -> "Google Drive"
        CatalogSeatKind.NETWORK_NAS -> "NAS / disco di rete"
        CatalogSeatKind.NONE -> ""
    }
}

object CatalogSeatRowFormatter {
    fun reachabilityLabel(reach: CatalogSeatReachability): String = when (reach) {
        CatalogSeatReachability.REACHABLE -> "Online"
        CatalogSeatReachability.UNREACHABLE -> "Offline"
        CatalogSeatReachability.UNKNOWN,
        CatalogSeatReachability.NOT_CONFIGURED,
        -> "Non verificabile"
    }

    /**
     * Seconda riga stile TO-BE:
     * locale: «device, Principale|Secondaria, folder - stato»
     * remoto: «device, path - stato»
     */
    fun detailLine(status: CatalogSeatStatus): String {
        val seat = status.seat
        val device = seat.deviceModel.ifBlank { CatalogSeatKindLabels.shortDevice(seat.kind) }
        val path = seat.displayLabel.ifBlank { "—" }
        val state = reachabilityLabel(status.reachability)
        return when (seat.kind) {
            CatalogSeatKind.LOCAL_DEVICE -> {
                val mem = seat.memoryLabel.ifBlank { CatalogSeat.MEMORY_PRIMARY }
                "$device, $mem, $path - $state"
            }
            else -> "$device, $path - $state"
        }
    }
}
