package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WizardStepChromeTest {
    @Test
    fun pcPath_usesPassoNDi10_only() {
        val kind = CatalogSeatKind.PC_NOTEBOOK
        assertEquals("Inizio", WizardStepChrome.subtitle(WizardStep.S0_START, kind).text)
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.INTRO, kind).text
                .startsWith("Passo 1 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_ROUTINE_PARAMS, kind).text
                .startsWith("Passo 2 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_ROUTINE_READY, kind).text
                .startsWith("Passo 3 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_SCRIPT_FIND, kind).text
                .startsWith("Passo 4 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_CIFS, kind).text
                .startsWith("Passo 8 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.W2_PICK, kind).text
                .startsWith("Passo 9 di 10"),
        )
        assertTrue(
            WizardStepChrome.subtitle(WizardStep.DONE, kind).text
                .startsWith("Passo 10 di 10"),
        )
        // Nessuna numerazione legacy «di 7» o «1/4»
        val all = WizardStep.entries.map { WizardStepChrome.subtitle(it, kind).text }
        assertFalse(all.any { it.contains("di 7") })
        assertFalse(all.any { it.contains("1/4") })
    }

    @Test
    fun allChecklistChecked_requiresEveryFlag() {
        assertTrue(allChecklistChecked(booleanArrayOf(true, true, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf(true, false, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf()))
    }
}
