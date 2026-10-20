package com.renatizzi.photovideomanager.ui.hub

import com.renatizzi.photovideomanager.data.hub.WindowsCatalogSetupScript
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WizardStepChromeTest {
    @Test
    fun deviceSelection_usesDropdownNotButtonStack() {
        assertTrue(WizardUiContract.DEVICE_SELECTION_USES_DROPDOWN)
    }

    @Test
    fun step0_subtitleHasNoNumber() {
        assertTrue(WizardUiContract.STEP0_SUBTITLE_HAS_NO_NUMBER)
        val text = WizardStepChrome.subtitle(WizardStep.INTRO, CatalogSeatKind.PC_NOTEBOOK).text
        assertEquals("Dove tenere il Catalogo", text)
        assertFalse(text.first().isDigit())
        assertFalse(text.startsWith("0"))
    }

    @Test
    fun pcPipeline_subtitlesMatchVideateUtente() {
        val kind = CatalogSeatKind.PC_NOTEBOOK
        val expected = WizardUiContract.PC_STEP_SUBTITLES
        assertEquals(expected[0], WizardStepChrome.subtitle(WizardStep.INTRO, kind).text)
        assertEquals(expected[1], WizardStepChrome.subtitle(WizardStep.PC_AVAILABILITY, kind).text)
        assertEquals(expected[2], WizardStepChrome.subtitle(WizardStep.PC_USB_PREPARE, kind).text)
        assertEquals(expected[3], WizardStepChrome.subtitle(WizardStep.PC_COPY_FILES, kind).text)
        assertEquals(expected[4], WizardStepChrome.subtitle(WizardStep.PC_RUN_SCRIPT, kind).text)
        assertEquals(expected[5], WizardStepChrome.subtitle(WizardStep.W2_CIFS, kind).text)

        val all = WizardStep.entries.map { WizardStepChrome.subtitle(it, kind).text }
        assertFalse(all.any { it.contains("Passo ") && it.contains(" di 10") })
        assertFalse(all.any { it.contains("di 7") })
    }

    @Test
    fun setupFiles_areConfiguraAndSup() {
        assertEquals("CONFIGURA.cmd", WindowsCatalogSetupScript.CMD_FILE_NAME)
        assertEquals("sup.ps1", WindowsCatalogSetupScript.PS1_FILE_NAME)
    }

    @Test
    fun allChecklistChecked_requiresEveryFlag() {
        assertTrue(allChecklistChecked(booleanArrayOf(true, true, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf(true, false, true)))
        assertTrue(!allChecklistChecked(booleanArrayOf()))
    }
}
