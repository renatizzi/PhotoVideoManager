package com.renatizzi.photovideomanager.ui.hub

/**
 * Contratto UIX Setup Catalogo — template BT v3 + Avvio procedura.
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
     * Sottotitoli filiera PC (template v3).
     * Indice: 0 intro, 1 prerequisiti, 2 modalità, 3 guida (dinamica), 4 import, 5 CIFS.
     */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "Dove tenere il Catalogo",
        "Verifica della disponibilità di quanto necessario",
        "Modalità di associazione tra Telefono e PC",
        "Guida collegamento Telefono e PC",
        "Importa i dati da copiare su CIFS",
        "Copia i dati su CIFS e attivazione collegamento",
    )
}
