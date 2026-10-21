package com.renatizzi.photovideomanager.ui.hub

/**
 * Contratto UIX da `videate-utente-setup-catalogo-pc.md` (PPT V2 + Q1–Q7).
 * I test unitari falliscono se questi valori divergono dalla UI implementata.
 */
object WizardUiContract {
    /**
     * Passo 0: la scelta dispositivo è un menu a tendina chiuso,
     * non quattro pulsanti a tutta larghezza.
     */
    const val DEVICE_SELECTION_USES_DROPDOWN = true

    /**
     * Passo 0: sottotitolo senza numero (passo comune a tutti i dispositivi).
     * I passi 1–4 del ramo PC restano numerati (copia file fusa nel passo 2).
     */
    const val STEP0_SUBTITLE_HAS_NO_NUMBER = true

    /** Sottotitoli filiera PC (passo 0 senza «0 –»; senza pagina «copia» separata). */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "Dove tenere il Catalogo",
        "1 – Verifica della disponibilità di quanto necessario",
        "2 – Verifica preliminare, preparazione dei file e collegamento",
        "3 – Generazione password e altri dati necessari per configurare la cartella condivisa",
        "4 – Accedere alla cartella del Catalogo mediante CIFS",
    )
}
