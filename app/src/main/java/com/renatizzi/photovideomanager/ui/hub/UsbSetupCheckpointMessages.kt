package com.renatizzi.photovideomanager.ui.hub

/**
 * Messaggi checkpoint USB-only (verificabili in unit test).
 * REV4: niente strip numerica; avanzamento = titolo fase.
 */
object UsbSetupCheckpointMessages {
    const val SCRIPT_DOWNLOAD_KO =
        "Creazione script non riuscita in Download. " +
            "Verifica lo spazio e i permessi, poi tocca di nuovo OK."

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
     * Indice fase corrente 0-based nella procedura 1..7, o -1 se fuori dalle fasi numerate.
     * Usato per titolo/sottotitolo; la strip numerica è rimossa (REV4).
     */
    fun highlightIndex(step: WizardStep, guidePhaseDone: List<Boolean>): Int = when (step) {
        WizardStep.PC_AVAILABILITY,
        WizardStep.PC_LINK_MODE,
        WizardStep.S0_START,
        WizardStep.DONE,
        -> -1
        WizardStep.PC_GUIDE,
        WizardStep.PC_USB_PREPARE,
        WizardStep.PC_COPY_FILES,
        WizardStep.PC_RUN_SCRIPT,
        -> PcSetupGuideContent.currentGuidePhaseIndex(guidePhaseDone)
        WizardStep.W2_CIFS_INSTALL,
        -> 5
        WizardStep.W2_CIFS,
        WizardStep.W2_PICK,
        -> 6
        else -> -1
    }

    /** Guida stampabile: tutte le schermate della procedura USB, incluso Avvio e Riepilogo. */
    fun showsUsbChrome(step: WizardStep): Boolean = when (step) {
        WizardStep.S0_START,
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
        WizardStep.DONE,
        -> true
        else -> false
    }

    /** Titolo fase (sostituisce il sottotitolo standard sulle pagine di fase). */
    fun phaseTitleForSubtitle(step: WizardStep, guidePhaseDone: List<Boolean>): String? =
        when (step) {
            WizardStep.PC_GUIDE,
            WizardStep.PC_USB_PREPARE,
            WizardStep.PC_COPY_FILES,
            WizardStep.PC_RUN_SCRIPT,
            -> {
                val idx = PcSetupGuideContent.currentGuidePhaseIndex(guidePhaseDone)
                PcSetupGuideContent.usbPhases().getOrNull(idx)?.title
            }
            WizardStep.W2_CIFS_INSTALL -> PcSetupGuideContent.cifs61Title()
            WizardStep.W2_CIFS,
            WizardStep.W2_PICK,
            -> PcSetupGuideContent.cifs62Title()
            else -> null
        }
}
