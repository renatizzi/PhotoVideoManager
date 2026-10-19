package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

/**
 * Chrome UIX — numerazione unica da template v2 (Passo N di 10).
 * Il corpo pagina non ripete numeri di passo (niente «Passo 1/4»).
 */
object WizardStepChrome {
    data class Subtitle(val text: String)

    private const val TOTAL_PC = 10

    fun subtitle(step: WizardStep, kind: CatalogSeatKind): Subtitle {
        return when (step) {
            WizardStep.S0_START -> Subtitle("Inizio")
            WizardStep.INTRO -> Subtitle("Passo 1 di $TOTAL_PC — Dove tenere il Catalogo")
            WizardStep.W1_CONFIRM -> Subtitle("Passo 2 di $TOTAL_PC — Su questo apparecchio")
            WizardStep.W2_CHOICE -> Subtitle("Passo 2 di $TOTAL_PC — Come preparare il PC")
            WizardStep.W2_ROUTINE_PARAMS -> Subtitle("Passo 2 di $TOTAL_PC — Prepara i file per il PC")
            WizardStep.W2_ROUTINE_READY -> Subtitle("Passo 3 di $TOTAL_PC — Copia i file sul Desktop")
            WizardStep.W2_SCRIPT_FIND -> Subtitle("Passo 4 di $TOTAL_PC — Avvia lo script sul PC (1/3)")
            WizardStep.W2_SCRIPT_LAUNCH -> Subtitle("Passo 5 di $TOTAL_PC — Avvia lo script sul PC (2/3)")
            WizardStep.W2_SCRIPT_RESULT -> Subtitle("Passo 6 di $TOTAL_PC — Avvia lo script sul PC (3/3)")
            WizardStep.W2_ROUTINE_CONFIRM -> Subtitle("Passo 4 di $TOTAL_PC — Avvia lo script sul PC")
            WizardStep.W2_SUMMARY,
            WizardStep.W2_CIFS_BRIDGE,
            -> Subtitle("Passo 7 di $TOTAL_PC — Collegamento dal telefono")
            WizardStep.W2_PREPARE -> Subtitle("Passo 2 di $TOTAL_PC — Cartella già condivisa")
            WizardStep.W2_CIFS_METHOD -> Subtitle("Passo 8 di $TOTAL_PC — Configura CIFS (1/2)")
            WizardStep.W2_CIFS -> Subtitle(
                if (kind == CatalogSeatKind.PC_NOTEBOOK || kind == CatalogSeatKind.NETWORK_NAS) {
                    "Passo 8 di $TOTAL_PC — Configura CIFS (2/2)"
                } else {
                    "Configura CIFS"
                },
            )
            WizardStep.W2_HELP,
            WizardStep.W2_PASSWORD_HELP,
            -> Subtitle("Aiuto — collegamento CIFS")
            WizardStep.W2_PICK -> Subtitle("Passo 9 di $TOTAL_PC — Scegli la cartella")
            WizardStep.W3_GUIDE -> Subtitle("Google Drive")
            WizardStep.W4_GUIDE -> Subtitle("NAS / disco di rete")
            WizardStep.DONE -> Subtitle("Passo 10 di $TOTAL_PC — Configurazione completata")
        }
    }
}
