package com.renatizzi.photovideomanager.data.hub

/**
 * Gate per la sede Catalogo su PC via CIFS Documents Provider.
 *
 * Con **Folder CIFS vuoto** la radice SAF della connessione è l’elenco share del host:
 * lì non si può creare `MediaManager/catalog_hub.json` → [HubPackageIo.probeWritable]
 * fallisce e la UI mostra «non raggiungibile». L’etichetta SAF è il **Name** CIFS
 * (es. `DELL_SHARED`), non il PC dello script (`DELLRS`) né necessariamente `MyMedia`.
 *
 * Soluzione tecnica: **pin della share** nel campo Folder (`MyMedia`). Così la radice
 * della connessione è già `smb://HOST/MyMedia/` (scrivibile). In Attiva collegamento
 * basta scegliere la connessione e «Usa questa cartella» — senza entrare nell’elenco share.
 *
 * Heuristica di controllo: se tra i figli della tree scelta compare ancora la share
 * attesa (`MyMedia`), siamo restati sull’elenco share (Folder vuoto / livello sbagliato).
 */
object PcCatalogSeatGate {

    data class Assessment(
        val ok: Boolean,
        val reason: String? = null,
    )

    /**
     * Valore da copiare in CIFS → Folder: nome share SMB (non path Windows, non vuoto).
     * Es. `MyMedia` → URI storage `smb://HOST/MyMedia/`.
     */
    fun cifsFolderPin(shareName: String = WindowsCatalogSetupScript.DEFAULT_SHARE): String =
        shareName.trim().ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }

    /**
     * Controlla i nomi dei figli della cartella SAF scelta.
     * Se compare la share Catalogo come figlio diretto → siamo sull’elenco share (non scrivibile).
     */
    fun assessChildNames(
        childNames: Collection<String>,
        expectedShare: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    ): Assessment {
        val share = cifsFolderPin(expectedShare)
        val hit = childNames.any { it.equals(share, ignoreCase = true) }
        if (!hit) return Assessment(true)
        return Assessment(
            ok = false,
            reason = "Hai selezionato l’elenco delle share (compare ancora «$share»), non la cartella del Catalogo. " +
                "In CIFS imposta Folder=$share (non lasciare vuoto), lascia Read only disattivato, salva, " +
                "poi in Attiva collegamento scegli la connessione e premi «Usa questa cartella» " +
                "senza aprire l’elenco share.",
        )
    }

    fun writableProbeFailureHint(
        expectedShare: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    ): String {
        val share = cifsFolderPin(expectedShare)
        return "Posizione del Catalogo non scrivibile. Controlla in CIFS: Folder=$share " +
            "(non vuoto e non un path Windows), Read only disattivato, Safe data transfer attivo; " +
            "Wi‑Fi e PC non in sospensione. Poi ripeti Attiva collegamento sulla connessione."
    }
}
