package com.renatizzi.photovideomanager.ui.hub

/**
 * Contratto UIX da `revisione_guida.pptx` (passi 0–5 ramo PC).
 * I test unitari falliscono se questi valori divergono dalla UI implementata.
 */
object WizardUiContract {
    /**
     * Passo 0: la scelta dispositivo è un menu a tendina chiuso,
     * non quattro pulsanti a tutta larghezza.
     * Deve restare allineato a [DeviceKindDropdown] in CatalogSeatWizardScreen.
     */
    const val DEVICE_SELECTION_USES_DROPDOWN = true

    /** Sottotitoli filiera PC (numerazione PPT, non «Passo N di 10»). */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "0 — Dove tenere il Catalogo",
        "1 — Verifica della disponibilità di quanto necessario",
        "2 — Verifica preliminare, preparazione dei file e collegamento",
        "3 — Copia manuale dei file di configurazione",
        "4 — Generazione password e dati per la cartella condivisa",
        "5 — Accedere alla cartella mediante CIFS",
    )
}
