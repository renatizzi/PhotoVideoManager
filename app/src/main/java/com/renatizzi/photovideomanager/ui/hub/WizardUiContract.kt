package com.renatizzi.photovideomanager.ui.hub

/**
 * Contratto UIX Setup Catalogo — allineato al TO-BE pin integrazione 30/10
 * (`PVM_Proposta_Editing_UI_Template_TOBE.html` @ c1ef993).
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
     * Sottotitoli filiera PC (GUIDATA 1–4 + import BT + CIFS).
     * Indice 0 = legacy INTRO; 1–4 GUIDATA; 5 = importa bundle; 6 = Collegamento CIFS.
     */
    val PC_STEP_SUBTITLES: List<String> = listOf(
        "Dove tenere il Catalogo",
        "1 – Verifica della disponibilità di quanto necessario",
        "2 – Bluetooth: associazione e invio al PC",
        "2b – Conferma file sul Desktop",
        "3 – Esegui CONFIGURA sul PC",
        "4 – Importa i dati CIFS sul telefono",
        "Collegamento CIFS",
    )
}
