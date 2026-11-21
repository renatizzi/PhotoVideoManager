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
        assertEquals("Pronto", phases[0].actionLabel)
        assertEquals("Fatto", phases[1].actionLabel)
        assertEquals("Fatto", phases[2].actionLabel)
        assertEquals("Fatto", phases[3].actionLabel)
        assertEquals("Fatto", phases[4].actionLabel)
    }

    @Test
    fun phase1_createsScripts_noBluetooth() {
        val body = PcSetupGuideContent.usbPhases()[0].body
        assertTrue(body.contains("CONFIGURA.CMD"))
        assertTrue(body.contains("QRCoder.dll"))
        assertTrue(body.contains("sup.ps1"))
        assertFalse(body.contains("Bluetooth", ignoreCase = true))
    }

    @Test
    fun phase2_isUsbCopyToDesktop() {
        val body = PcSetupGuideContent.usbPhases()[1].body
        assertTrue(body.contains("Download"))
        assertTrue(body.contains("Desktop"))
        assertTrue(body.contains("USB") || body.contains("cavo"))
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
    }
}
