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
    fun avvioAndRapida_subtitlesMatchTobe() {
        assertEquals(
            "Avvio procedura",
            WizardStepChrome.subtitle(WizardStep.S0_START, CatalogSeatKind.LOCAL_DEVICE).text,
        )
        assertEquals(
            "Scelta della cartella",
            WizardStepChrome.subtitle(WizardStep.W1_CONFIRM, CatalogSeatKind.LOCAL_DEVICE).text,
        )
        assertEquals(
            "Configurazione del Catalogo su un PC / Notebook",
            WizardStepChrome.subtitle(WizardStep.PC_SHARE_READY, CatalogSeatKind.PC_NOTEBOOK).text,
        )
        assertEquals(
            "Configurazione del Catalogo su NAS/DISCO DI RETE",
            WizardStepChrome.subtitle(WizardStep.PC_SHARE_READY, CatalogSeatKind.NETWORK_NAS).text,
        )
        assertEquals(
            "Configurazione completata correttamente",
            WizardStepChrome.subtitle(WizardStep.DONE, CatalogSeatKind.LOCAL_DEVICE).text,
        )
    }

    @Test
    fun pcPipeline_subtitlesMatchTobe() {
        val kind = CatalogSeatKind.PC_NOTEBOOK
        val expected = WizardUiContract.PC_STEP_SUBTITLES
        assertEquals(expected[0], WizardStepChrome.subtitle(WizardStep.INTRO, kind).text)
        assertEquals(expected[1], WizardStepChrome.subtitle(WizardStep.PC_AVAILABILITY, kind).text)
        assertEquals(
            "2 – Verifica preliminare e collegamento",
            WizardStepChrome.subtitle(WizardStep.PC_USB_PREPARE, kind).text,
        )
        assertEquals(expected[3], WizardStepChrome.subtitle(WizardStep.PC_COPY_FILES, kind).text)
        assertEquals(
            "4 – Generazione password e dati",
            WizardStepChrome.subtitle(WizardStep.PC_RUN_SCRIPT, kind).text,
        )
        assertEquals(
            "Collegamento CIFS",
            WizardStepChrome.subtitle(WizardStep.W2_CIFS, kind).text,
        )
        assertEquals(expected[5], WizardStepChrome.subtitle(WizardStep.W2_PICK, kind).text)
        assertEquals(6, expected.size)

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
