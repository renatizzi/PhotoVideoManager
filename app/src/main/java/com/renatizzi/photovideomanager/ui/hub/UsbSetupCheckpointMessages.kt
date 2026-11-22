package com.renatizzi.photovideomanager.ui.hub

/**
 * Messaggi checkpoint USB-only (verificabili in unit test).
 */
object UsbSetupCheckpointMessages {
    const val SCRIPT_DOWNLOAD_KO =
        "Creazione script non riuscita in Download. " +
            "Verifica lo spazio e i permessi, poi tocca di nuovo Pronto."

    const val IMPORT_KO =
        "Import non riuscito. Verifica che DATI_CIFS.json sia in Download " +
            "e tocca Importa Dati CIFS, poi Fatto."

    const val CIFS_APP_KO =
        "CIFS Documents Provider non risulta installata. " +
            "Tocca Installa / Apri CIFS, completa l’installazione, poi Fatto."

    fun canCompletePhase1(downloadsSaved: Boolean): Boolean = downloadsSaved

    fun canCompletePhase5(setupResultImported: Boolean): Boolean = setupResultImported

    fun canCompletePhase61(cifsInstalled: Boolean): Boolean = cifsInstalled

    /**
     * Indice evidenziato nella strip 1..6 (0-based), o -1 se nessuna fase guida attiva.
     */
    fun highlightIndex(step: WizardStep, guidePhaseDone: List<Boolean>): Int = when (step) {
        WizardStep.PC_AVAILABILITY,
        WizardStep.PC_LINK_MODE,
        -> -1
        WizardStep.PC_GUIDE,
        WizardStep.PC_USB_PREPARE,
        WizardStep.PC_COPY_FILES,
        WizardStep.PC_RUN_SCRIPT,
        -> (0 until PcSetupGuideContent.PHASE_COUNT)
            .firstOrNull { !guidePhaseDone.getOrElse(it) { false } }
            ?: (PcSetupGuideContent.PHASE_COUNT - 1)
        WizardStep.W2_CIFS_INSTALL,
        WizardStep.W2_CIFS,
        WizardStep.W2_PICK,
        -> 5
        else -> -1
    }

    fun showsUsbChrome(step: WizardStep): Boolean = when (step) {
        WizardStep.PC_AVAILABILITY,
        WizardStep.PC_LINK_MODE,
        WizardStep.PC_GUIDE,
        WizardStep.PC_USB_PREPARE,
        WizardStep.PC_COPY_FILES,
        WizardStep.PC_RUN_SCRIPT,
        WizardStep.PC_BT_DOWNLOAD_BUNDLE,
        WizardStep.W2_CIFS_INSTALL,
        WizardStep.W2_CIFS,
        WizardStep.W2_PICK,
        -> true
        else -> false
    }
}
