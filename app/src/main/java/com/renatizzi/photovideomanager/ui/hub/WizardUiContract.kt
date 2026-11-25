package com.renatizzi.photovideomanager.ui.hub

/**
 * Contratto UIX Setup Catalogo — USB-only (specifica Cursor REV4).
 */
object WizardUiContract {
    /**
     * Passo 0 / Avvio: la scelta dispositivo è un menu a tendina chiuso,
     * non quattro pulsanti a tutta larghezza.
     */
    const val DEVICE_SELECTION_USES_DROPDOWN = true

    /**
     * Avvio / passo comune: sottotitolo senza numero.
     */
    const val STEP0_SUBTITLE_HAS_NO_NUMBER = true

    /**
     * Sottotitoli filiera PC (USB-only).
     * Indice: 0 intro, 1 prerequisiti, 2 USB fisico,
     * 3 fallback guida (REV4: sulle fasi 1–5 il titolo fase sostituisce questo testo),
     * 4 legacy import, 5 legacy CIFS.
     */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "Dove tenere il Catalogo",
        "Verifica della disponibilità di quanto necessario",
        "Collegamento fisico USB",
        "Guida collegamento Telefono e PC (USB)",
        "Importa i dati da copiare su CIFS",
        "Copia i dati su CIFS e attivazione collegamento",
    )
}
