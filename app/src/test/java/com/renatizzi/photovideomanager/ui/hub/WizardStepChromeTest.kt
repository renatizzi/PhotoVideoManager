package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WizardStepChromeTest {
    @Test
    fun pcPathSubtitles_usePassoNDiM() {
        val kind = CatalogSeatKind.PC_NOTEBOOK
        assertEquals("Inizio", WizardStepChrome.subtitle(WizardStep.S0_START, kind)?.text)
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_ROUTINE_PARAMS, kind)!!.text
                .startsWith("Passo 2 di 7"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_ROUTINE_READY, kind)!!.text
                .startsWith("Passo 3 di 7"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_CIFS, kind)!!.text
                .startsWith("Passo 6 di 7"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_PICK, kind)!!.text
                .contains("Posizione del Catalogo"),
        )
    }

    @Test
    fun allChecklistChecked_requiresEveryFlag() {
        assertTrue(allChecklistChecked(booleanArrayOf(true, true, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf(true, false, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf()))
    }
}
