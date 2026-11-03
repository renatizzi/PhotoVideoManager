package com.renatizzi.photovideomanager.domain.hub

import com.renatizzi.photovideomanager.domain.model.Availability

/**
 * Formato riga elenco Catalogo / Acquisisci / Importa / Aggiorna / Pulisci (Nota bis 2 nov).
 */
object CatalogListRowFormat {
    /**
     * Seconda riga meta: &lt;Dispositivo&gt;[, SD] &lt;\path&gt; - &lt;Stato&gt;
     * SD solo se [secondary] (memoria Secondaria su telefoni/tablet).
     */
    fun metaLine(
        deviceLabel: String,
        pathLabel: String,
        statusLabel: String,
        secondary: Boolean = false,
    ): String {
        val parts = mutableListOf<String>()
        val device = deviceLabel.trim()
        if (device.isNotEmpty()) parts += device
        if (secondary) parts += "SD"
        val path = CatalogSeatRowFormatter.displayPath(pathLabel)
        if (path != "—" && path.isNotBlank()) parts += path
        val status = statusLabel.trim()
        return if (status.isEmpty()) {
            parts.joinToString(" ")
        } else {
            parts.joinToString(" ") + " - $status"
        }
    }

    fun availabilityLabel(availability: Availability): String = when (availability) {
        Availability.AVAILABLE -> "🛜 Online"
        Availability.UNAVAILABLE -> "⚠ Offline"
        Availability.UNKNOWN -> "? Non verificabile"
    }
}
