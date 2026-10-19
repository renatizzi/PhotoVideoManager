package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

/**
 * Chrome UIX: sottotitolo «Passo N di M — …» sul ramo as-is.
 * Numerazione ponte verso il target a regime (PPT), non ancora 1:1 con i template.
 */
object WizardStepChrome {
    data class Subtitle(val text: String)

    fun subtitle(step: WizardStep, kind: CatalogSeatKind): Subtitle? {
        return when (step) {
            WizardStep.S0_START -> Subtitle("Inizio")
            WizardStep.INTRO -> Subtitle("Passo 1 — Dove tenere il Catalogo")
            WizardStep.W1_CONFIRM -> Subtitle("Passo 2 — Su questo apparecchio")
            WizardStep.W2_CHOICE -> Subtitle("Passo 2 — Come preparare il PC")
            WizardStep.W2_ROUTINE_PARAMS -> Subtitle("Passo 2 di 7 — Prepara i file per il PC")
            WizardStep.W2_ROUTINE_READY -> Subtitle("Passo 3 di 7 — Copia i file sul Desktop")
            WizardStep.W2_ROUTINE_CONFIRM -> Subtitle("Passo 4 di 7 — Avvia la configurazione sul PC")
            WizardStep.W2_SUMMARY -> Subtitle("Passo 5 di 7 — Riepilogo (senza password)")
            WizardStep.W2_PREPARE -> Subtitle("Passo 2 — Cartella già condivisa")
            WizardStep.W2_CIFS -> Subtitle(
                if (kind == CatalogSeatKind.PC_NOTEBOOK) {
                    "Passo 6 di 7 — Collega CIFS"
                } else {
                    "Collega CIFS"
                },
            )
            WizardStep.W2_HELP,
            WizardStep.W2_PASSWORD_HELP,
            -> Subtitle("Aiuto — collegamento CIFS")
            WizardStep.W2_PICK -> Subtitle("Passo 7 di 7 — Verifica Posizione del Catalogo")
            WizardStep.W3_GUIDE -> Subtitle("Google Drive (percorso base)")
            WizardStep.W4_GUIDE -> Subtitle("NAS / rete (percorso base)")
            WizardStep.DONE -> Subtitle("Completato")
        }
    }
}
