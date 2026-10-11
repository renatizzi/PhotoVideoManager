package com.renatizzi.photovideomanager.domain.policy

/**
 * BL-01 v1 — candidati «Possibili scarti» (solo proposta; conferma utente → Cestino).
 * Criteri conservativi: niente cancellazione automatica.
 */
object ScrapHeuristics {
    /** Soglia file microscopico (byte). */
    const val TINY_BYTES = 2_048L

    private val NAME_MARKERS = listOf(
        "thumbs.db",
        "albumart",
        "folder.jpg",
        "folder.png",
        "cover.jpg",
        "cover.png",
        ".nomedia",
        "_thumb",
        "-thumb",
        ".tmp",
        ".trashed",
        "image_placeholder",
    )

    /**
     * @param displayTitle titolo Catalogo o nome file
     * @param byteSize dimensione nota (null = sconosciuta)
     */
    fun isPossibleScrap(displayTitle: String?, byteSize: Long?): Boolean {
        val name = displayTitle?.trim().orEmpty()
        if (name.isBlank()) return false
        val lower = name.lowercase()
        if (NAME_MARKERS.any { lower.contains(it) }) return true
        if (byteSize != null && byteSize >= 0 && byteSize < TINY_BYTES) return true
        return false
    }

    fun reasonLabel(displayTitle: String?, byteSize: Long?): String {
        val name = displayTitle?.trim().orEmpty()
        val lower = name.lowercase()
        val byName = NAME_MARKERS.any { lower.contains(it) }
        val bySize = byteSize != null && byteSize >= 0 && byteSize < TINY_BYTES
        return when {
            byName && bySize -> "Nome tipico di scarto e file molto piccolo"
            byName -> "Nome tipico di scarto / anteprima di sistema"
            bySize -> "File molto piccolo (sotto i 2 KB)"
            else -> "Possibile scarto"
        }
    }
}
