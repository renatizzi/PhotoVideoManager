package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcSetupGuideContentTest {
    @Test
    fun usbPhases_areFive_withProntoAndFatto() {
        val phases = PcSetupGuideContent.usbPhases()
        assertEquals(5, phases.size)
        assertEquals(PcSetupGuideContent.PHASE_COUNT, phases.size)
        assertEquals(6, PcSetupGuideContent.TOTAL_PHASES)
        assertEquals("Pronto", phases[0].actionLabel)
        phases.drop(1).forEach { assertEquals("Fatto", it.actionLabel) }
    }

    @Test
    fun phase1_createsScripts_noBluetooth() {
        val body = PcSetupGuideContent.usbPhases()[0].body
        assertTrue(body.contains("script", ignoreCase = true))
        assertFalse(body.contains("Bluetooth", ignoreCase = true))
    }

    @Test
    fun phase2_isUsbCopyToDesktop() {
        val body = PcSetupGuideContent.usbPhases()[1].body
        assertTrue(body.contains("CONFIGURA.CMD"))
        assertTrue(body.contains("Download"))
        assertTrue(body.contains("Desktop"))
    }

    @Test
    fun phase4_sendsDatiCifsToPhone() {
        val body = PcSetupGuideContent.usbPhases()[3].body
        assertTrue(body.contains("DATI_CIFS.json"))
        assertTrue(body.contains("Download"))
        assertFalse(body.contains("CONFIGURA.CMD"))
    }

    @Test
    fun phase5_import_notEditable() {
        val body = PcSetupGuideContent.usbPhases()[4].body
        assertTrue(body.contains("import", ignoreCase = true) || body.contains("Import"))
        assertTrue(body.contains("non sono modificabili") || body.contains("niente digitazione"))
    }

    @Test
    fun pageGroups_matchTemplate() {
        assertEquals(0..0, PcSetupGuideContent.pageGroupFor(0))
        assertEquals(1..2, PcSetupGuideContent.pageGroupFor(1))
        assertEquals(1..2, PcSetupGuideContent.pageGroupFor(2))
        assertEquals(3..4, PcSetupGuideContent.pageGroupFor(3))
        assertEquals(3..4, PcSetupGuideContent.pageGroupFor(4))
    }

    @Test
    fun sixPhaseOverview_hasSixEntries() {
        assertEquals(6, PcSetupGuideContent.sixPhaseOverview().size)
        assertTrue(PcSetupGuideContent.sixPhaseOverview().last().contains("CIFS"))
    }

    @Test
    fun guideAsset_isUsbOnly() {
        assertTrue(PcSetupGuideContent.GUIDE_PDF_ASSET.contains("USB_ONLY"))
    }

    @Test
    fun guideDescription_hasInlinePrintAnchor() {
        assertTrue(PcSetupGuideContent.guideDescription().endsWith("qui"))
    }

    @Test
    fun cifsSteps_titlesPresent() {
        assertTrue(PcSetupGuideContent.cifs61Title().contains("6/1"))
        assertTrue(PcSetupGuideContent.cifs62Title().contains("6/2"))
        assertEquals(3, PcSetupGuideContent.cifs61Bullets().size)
        assertEquals(3, PcSetupGuideContent.cifs62Bullets().size)
    }

    @Test
    fun guideSubtitle_matchesContract() {
        assertEquals(
            WizardUiContract.PC_STEP_SUBTITLES[3],
            PcSetupGuideContent.guideSubtitle(PcLinkMode.USB),
        )
    }
}
