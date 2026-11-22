package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsbSetupCheckpointMessagesTest {
    @Test
    fun phase1_requiresDownloadsSaved() {
        assertFalse(UsbSetupCheckpointMessages.canCompletePhase1(false))
        assertTrue(UsbSetupCheckpointMessages.canCompletePhase1(true))
        assertTrue(
            UsbSetupCheckpointMessages.SCRIPT_DOWNLOAD_KO.contains("Download"),
        )
        assertTrue(
            UsbSetupCheckpointMessages.SCRIPT_DOWNLOAD_KO.contains("Pronto"),
        )
    }

    @Test
    fun phase5_requiresImport() {
        assertFalse(UsbSetupCheckpointMessages.canCompletePhase5(false))
        assertTrue(UsbSetupCheckpointMessages.canCompletePhase5(true))
        assertTrue(UsbSetupCheckpointMessages.IMPORT_KO.contains("DATI_CIFS.json"))
        assertTrue(UsbSetupCheckpointMessages.IMPORT_KO.contains("Importa"))
    }

    @Test
    fun phase61_requiresCifsInstalled() {
        assertFalse(UsbSetupCheckpointMessages.canCompletePhase61(false))
        assertTrue(UsbSetupCheckpointMessages.canCompletePhase61(true))
        assertTrue(UsbSetupCheckpointMessages.CIFS_APP_KO.contains("CIFS"))
        assertTrue(UsbSetupCheckpointMessages.CIFS_APP_KO.contains("Installa"))
    }

    @Test
    fun highlight_mapsSteps() {
        val none = List(5) { false }
        assertEquals(-1, UsbSetupCheckpointMessages.highlightIndex(WizardStep.PC_AVAILABILITY, none))
        assertEquals(-1, UsbSetupCheckpointMessages.highlightIndex(WizardStep.PC_LINK_MODE, none))
        assertEquals(0, UsbSetupCheckpointMessages.highlightIndex(WizardStep.PC_GUIDE, none))
        assertEquals(
            2,
            UsbSetupCheckpointMessages.highlightIndex(
                WizardStep.PC_GUIDE,
                listOf(true, true, false, false, false),
            ),
        )
        assertEquals(5, UsbSetupCheckpointMessages.highlightIndex(WizardStep.W2_CIFS_INSTALL, none))
        assertEquals(5, UsbSetupCheckpointMessages.highlightIndex(WizardStep.W2_CIFS, none))
    }

    @Test
    fun usbChrome_onUsbPipelineOnly() {
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.PC_GUIDE))
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.W2_CIFS_INSTALL))
        assertFalse(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.S0_START))
        assertFalse(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.W3_GUIDE))
    }

    @Test
    fun stripLabels_areSingleDigits() {
        repeat(6) { i ->
            assertEquals("${i + 1}", PcSetupGuideContent.lampLabel(i))
        }
        assertEquals(6, PcSetupGuideContent.TOTAL_PHASES)
    }
}
