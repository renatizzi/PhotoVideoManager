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
     * I passi 1–5 del ramo PC restano numerati.
     */
    const val STEP0_SUBTITLE_HAS_NO_NUMBER = true

    /**
     * Sottotitoli filiera PC (passo 0 senza «0 –»).
     * Indici: 0=intro, 1=disponibilità, 2=BT prepare, 3=Desktop,
     * 4=script, 5=scarica bundle, 6=CIFS.
     */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "Dove tenere il Catalogo",
        "1 – Verifica della disponibilità di quanto necessario",
        "2 – Collegamento Bluetooth, preparazione e invio dei file",
        "3 – File di configurazione sul Desktop",
        "4 – Generazione password e altri dati necessari per configurare la cartella condivisa",
        "4b – Scarica i dati per CIFS",
        "5 – Accedere alla cartella del Catalogo mediante CIFS",
    )
}
