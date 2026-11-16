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
            "Scelta della cartella sul dispositivo",
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
            "Riepilogo e conferma",
            WizardStepChrome.subtitle(WizardStep.DONE, CatalogSeatKind.LOCAL_DEVICE).text,
        )
    }

    @Test
    fun pcPipeline_subtitlesMatchTemplateV3() {
        val kind = CatalogSeatKind.PC_NOTEBOOK
        val expected = WizardUiContract.PC_STEP_SUBTITLES
        assertEquals(expected[0], WizardStepChrome.subtitle(WizardStep.INTRO, kind).text)
        assertEquals(expected[1], WizardStepChrome.subtitle(WizardStep.PC_AVAILABILITY, kind).text)
        assertEquals(expected[2], WizardStepChrome.subtitle(WizardStep.PC_LINK_MODE, kind).text)
        assertEquals(
            PcSetupGuideContent.guideSubtitle(PcLinkMode.ALREADY_PAIRED),
            WizardStepChrome.subtitle(
                WizardStep.PC_GUIDE,
                kind,
                PcLinkMode.ALREADY_PAIRED,
            ).text,
        )
        assertEquals(expected[4], WizardStepChrome.subtitle(WizardStep.PC_BT_DOWNLOAD_BUNDLE, kind).text)
        assertEquals(expected[5], WizardStepChrome.subtitle(WizardStep.W2_CIFS, kind).text)
        assertEquals(6, expected.size)

        val all = WizardStep.entries.map { WizardStepChrome.subtitle(it, kind).text }
        assertFalse(all.any { it.contains("Passo ") && it.contains(" di 10") })
        assertFalse(all.any { it.contains("di 7") })
        assertFalse(all.any { it.contains("A)") || it.contains("B)") || it.contains("C)") })
    }

    @Test
    fun pcGuide_confirmMessagesAreChannelAgnostic() {
        assertFalse(PcSetupGuideContent.CONFIRM_SEND_TO_PC.contains("BT", ignoreCase = true))
        assertFalse(PcSetupGuideContent.CONFIRM_SEND_TO_PC.contains("USB", ignoreCase = true))
        assertFalse(PcSetupGuideContent.CONFIRM_SCRIPT.contains("BT", ignoreCase = true))
        assertFalse(PcSetupGuideContent.CONFIRM_SEND_TO_PHONE.contains("USB", ignoreCase = true))
        assertEquals(4, PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING).size)
        assertTrue(
            PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING)[0].body.contains("pairing", ignoreCase = true),
        )
        assertTrue(
            PcSetupGuideContent.phases(PcLinkMode.USB)[0].title.contains("USB"),
        )
    }

    @Test
    fun analogGuides_haveSharedSkeletonWithoutInventedProtocols() {
        assertEquals(2, AnalogSetupGuideContent.localPhases("S25").size)
        assertEquals(2, AnalogSetupGuideContent.cloudPhases().size)
        assertEquals(2, AnalogSetupGuideContent.nasPhases().size)
        assertTrue(AnalogSetupGuideContent.nasPhases()[0].body.contains("CIFS"))
        assertFalse(AnalogSetupGuideContent.cloudPhases().any { it.body.contains("Bluetooth") })
    }

    @Test
    fun setupFiles_areConfiguraAndSup() {
        assertEquals("CONFIGURA.cmd", WindowsCatalogSetupScript.CMD_FILE_NAME)
        assertEquals("sup.ps1", WindowsCatalogSetupScript.PS1_FILE_NAME)
    }

    @Test
    fun allChecklistChecked_requiresEveryFlag() {
        assertTrue(allChecklistChecked(booleanArrayOf(true, true, true)))
        assertFalse(allChecklistChecked(booleanArrayOf(true, false, true)))
    }
}
