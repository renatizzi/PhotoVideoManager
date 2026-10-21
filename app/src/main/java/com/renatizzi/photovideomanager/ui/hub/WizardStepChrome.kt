package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

/**
 * Chrome UIX — videate utente Setup Catalogo (PPT V2).
 * Fonte: docs/reference/videate-utente-setup-catalogo-pc.md
 */
object WizardStepChrome {
    data class Subtitle(val text: String)

    @Suppress("UNUSED_PARAMETER", "DEPRECATION")
    fun subtitle(step: WizardStep, kind: CatalogSeatKind): Subtitle {
        val pc = WizardUiContract.PC_STEP_SUBTITLES
        check(WizardUiContract.STEP0_SUBTITLE_HAS_NO_NUMBER)
        check(!pc[0].first().isDigit()) {
            "Passo 0: sottotitolo senza numero"
        }
        return when (step) {
            WizardStep.S0_START -> Subtitle("Inizio")
            WizardStep.INTRO -> Subtitle(pc[0])
            WizardStep.W1_CONFIRM -> Subtitle("Su questo dispositivo")
            WizardStep.PC_AVAILABILITY -> Subtitle(pc[1])
            WizardStep.PC_USB_PREPARE -> Subtitle(pc[2])
            WizardStep.PC_COPY_FILES -> Subtitle(pc[3])
            WizardStep.PC_RUN_SCRIPT -> Subtitle(pc[4])
            WizardStep.W2_CIFS,
            WizardStep.W2_PICK,
            WizardStep.W2_SUMMARY,
            WizardStep.W2_CIFS_BRIDGE,
            WizardStep.W2_CIFS_METHOD,
            WizardStep.W2_PREPARE,
            WizardStep.W2_HELP,
            WizardStep.W2_PASSWORD_HELP,
            -> Subtitle(pc[5])
            WizardStep.W2_CHOICE,
            WizardStep.W2_ROUTINE_PARAMS,
            -> Subtitle(pc[1])
            WizardStep.W2_ROUTINE_READY -> Subtitle(pc[3])
            WizardStep.W2_SCRIPT_FIND,
            WizardStep.W2_SCRIPT_LAUNCH,
            WizardStep.W2_SCRIPT_RESULT,
            WizardStep.W2_ROUTINE_CONFIRM,
            -> Subtitle(pc[4])
            WizardStep.W3_GUIDE -> Subtitle("Google Drive")
            WizardStep.W4_GUIDE -> Subtitle("NAS / disco di rete")
            WizardStep.DONE -> Subtitle("Configurazione completata")
        }
    }
}
