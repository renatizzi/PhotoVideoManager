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
        CatalogSeatKind.LOCAL_DEVICE -> "Questo dispositivo"
        CatalogSeatKind.PC_NOTEBOOK -> "PC Windows"
        CatalogSeatKind.CLOUD_DRIVE -> "Cloud"
        CatalogSeatKind.NETWORK_NAS -> "Disco di Rete"
        CatalogSeatKind.NONE -> ""
    }

    fun shortDevice(kind: CatalogSeatKind): String = when (kind) {
        CatalogSeatKind.LOCAL_DEVICE -> "Questo dispositivo"
        CatalogSeatKind.PC_NOTEBOOK -> "PC Windows"
        CatalogSeatKind.CLOUD_DRIVE -> "Cloud"
        CatalogSeatKind.NETWORK_NAS -> "Disco di Rete"
        CatalogSeatKind.NONE -> ""
    }
}

object CatalogSeatRowFormatter {
    fun reachabilityLabel(reach: CatalogSeatReachability): String = when (reach) {
        CatalogSeatReachability.REACHABLE -> "🛜 Online"
        CatalogSeatReachability.UNREACHABLE -> "⚠ Offline"
        CatalogSeatReachability.UNKNOWN,
        CatalogSeatReachability.NOT_CONFIGURED,
        -> "? Non verificabile"
    }

    /**
     * Convenzione display percorso (D4): sempre con `\` iniziale,
     * `/` → `\`. UNC Windows (`\\host\share`) lasciato invariato.
     */
    fun displayPath(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed == "—") return "—"
        if (trimmed.startsWith("\\\\")) return trimmed
        val withBackslash = trimmed.replace('/', '\\').trimStart('\\')
        if (withBackslash.isEmpty()) return "—"
        return "\\$withBackslash"
    }

    /**
     * Seconda riga stile TO-BE (C1):
     * locale Principale: «device \path - 🛜 Online»
     * locale Secondaria: «device SD \path - 🛜 Online»
     * remoto: «device \path - 🛜 Online»
     */
    fun detailLine(status: CatalogSeatStatus): String {
        val seat = status.seat
        val device = seat.deviceModel.ifBlank {
            CatalogSeatKindLabels.shortDevice(seat.kind).ifBlank { CatalogSeat.DEFAULT_DEVICE_MODEL }
        }
        val path = displayPath(seat.displayLabel.ifBlank { "—" })
        val state = reachabilityLabel(status.reachability)
        val parts = mutableListOf(device)
        if (seat.kind == CatalogSeatKind.LOCAL_DEVICE &&
            seat.memoryLabel == CatalogSeat.MEMORY_SECONDARY
        ) {
            parts += "SD"
        }
        if (path != "—") {
            parts += path
        }
        return parts.joinToString(" ") + " - $state"
    }
}
