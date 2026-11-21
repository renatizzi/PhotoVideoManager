package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

/**
 * Chrome UIX — Setup Catalogo USB-only (ripristino USB2.3).
 */
object WizardStepChrome {
    data class Subtitle(val text: String)

    @Suppress("UNUSED_PARAMETER", "DEPRECATION")
    fun subtitle(step: WizardStep, kind: CatalogSeatKind, linkMode: PcLinkMode? = null): Subtitle {
        val pc = WizardUiContract.PC_STEP_SUBTITLES
        check(WizardUiContract.STEP0_SUBTITLE_HAS_NO_NUMBER)
        check(!pc[0].first().isDigit()) {
            "Passo 0: sottotitolo senza numero"
        }
        return when (step) {
            WizardStep.S0_START -> Subtitle("Avvio procedura")
            WizardStep.INTRO -> Subtitle(pc[0])
            WizardStep.W1_CONFIRM -> Subtitle("Scelta della cartella sul dispositivo")
            WizardStep.PC_SHARE_READY -> Subtitle(
                when (kind) {
                    CatalogSeatKind.NETWORK_NAS ->
                        "Configurazione del Catalogo su NAS/DISCO DI RETE"
                    else ->
                        "Configurazione del Catalogo su un PC / Notebook"
                },
            )
            WizardStep.PC_AVAILABILITY -> Subtitle(pc[1])
            WizardStep.PC_LINK_MODE -> Subtitle(pc[2])
            WizardStep.PC_GUIDE,
            WizardStep.PC_USB_PREPARE,
            WizardStep.PC_COPY_FILES,
            WizardStep.PC_RUN_SCRIPT,
            -> Subtitle(PcSetupGuideContent.guideSubtitle(PcLinkMode.USB))
            WizardStep.PC_BT_DOWNLOAD_BUNDLE -> Subtitle(pc[4])
            WizardStep.W2_CIFS_INSTALL -> Subtitle(PcSetupGuideContent.cifs61Title())
            WizardStep.W2_CIFS,
            WizardStep.W2_PICK,
            WizardStep.W2_SUMMARY,
            WizardStep.W2_CIFS_BRIDGE,
            WizardStep.W2_CIFS_METHOD,
            WizardStep.W2_PREPARE,
            WizardStep.W2_HELP,
            WizardStep.W2_PASSWORD_HELP,
            -> Subtitle(PcSetupGuideContent.cifs62Title())
            WizardStep.W2_CHOICE,
            WizardStep.W2_ROUTINE_PARAMS,
            -> Subtitle(pc[1])
            WizardStep.W2_ROUTINE_READY,
            WizardStep.W2_SCRIPT_FIND,
            WizardStep.W2_SCRIPT_LAUNCH,
            WizardStep.W2_SCRIPT_RESULT,
            WizardStep.W2_ROUTINE_CONFIRM,
            -> Subtitle(pc[3])
            WizardStep.W3_GUIDE -> Subtitle("Catalogo su Cloud — guida")
            WizardStep.W4_GUIDE -> Subtitle("Catalogo su disco di rete / NAS — guida")
            WizardStep.DONE -> Subtitle("Riepilogo e conferma")
        }
    }

    /** Compat test/legacy: senza linkMode. */
    fun subtitle(step: WizardStep, kind: CatalogSeatKind): Subtitle =
        subtitle(step, kind, linkMode = null)
}
