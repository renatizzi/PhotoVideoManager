package com.renatizzi.photovideomanager.data.hub

/**
 * Gate per la sede Catalogo su PC via CIFS Documents Provider.
 *
 * Con **Folder CIFS vuoto** la radice SAF è l’elenco share del host (ADMIN$/IPC$/…)
 * e non è scrivibile. Con Folder = nome share (pin), la radice è il disco condiviso:
 * lì è **normale** vedere anche la cartella Catalogo omonima (`MyTEST` sotto `MyTEST`)
 * — non è più un segnale di elenco share.
 *
 * Heuristica: rifiuta solo l’elenco share tipico (ADMIN$/IPC$/C$ senza cartelle disco).
 */
object PcCatalogSeatGate {

    data class Assessment(
        val ok: Boolean,
        val reason: String? = null,
    )

    private val adminShareHints = listOf("admin$", "ipc$", "c$", "d$", "e$")

    /**
     * Valore da copiare in CIFS → Folder: nome share SMB (non path Windows, non vuoto).
     * Es. `MyMedia` → URI storage `smb://HOST/MyMedia/`.
     */
    fun cifsFolderPin(shareName: String = WindowsCatalogSetupScript.DEFAULT_SHARE): String =
        shareName.trim().ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }

    /**
     * Controlla i nomi dei figli della cartella SAF scelta.
     * Rifiuta l’elenco share del host; accetta radice disco (anche se contiene la cartella Catalogo).
     */
    fun assessChildNames(
        childNames: Collection<String>,
        expectedShare: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    ): Assessment {
        val share = cifsFolderPin(expectedShare)
        val lower = childNames.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        if (lower.isEmpty()) return Assessment(true)

        val hasAdminHints = adminShareHints.any { it in lower }
        // ADMIN$/IPC$/C$ = elenco share del host (anche se compare un nome tipo Users).
        if (hasAdminHints) {
            return Assessment(
                ok = false,
                reason = "Hai selezionato l’elenco delle share del PC (compaiono ADMIN$/IPC$), " +
                    "non la cartella del Catalogo. In CIFS imposta Folder=$share (non vuoto), " +
                    "salva, poi in Attiva Catalogo scegli la connessione «$share» " +
                    "(o la cartella $share\\MediaManager) e premi «Usa questa cartella».",
            )
        }
        // Radice disco (Windows/Users/…) o cartella Catalogo: ok anche se compare
        // una cartella omonima della share (es. MyTEST sotto share MyTEST).
        return Assessment(true)
    }

    fun writableProbeFailureHint(
        expectedShare: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    ): String {
        val share = cifsFolderPin(expectedShare)
        return "Posizione del Catalogo non scrivibile. In CIFS: Folder=$share " +
            "(non vuoto), Read only disattivato, Safe data transfer attivo; " +
            "Wi‑Fi e PC non in sospensione. Poi ripeti Attiva Catalogo e scegli " +
            "la cartella «$share» (Catalogo) oppure «$share\\MediaManager»."
    }
}
