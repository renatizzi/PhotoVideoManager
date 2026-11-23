package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
            UsbSetupCheckpointMessages.SCRIPT_DOWNLOAD_KO.contains("OK"),
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
    fun highlight_mapsSevenPhases() {
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
        assertEquals(6, UsbSetupCheckpointMessages.highlightIndex(WizardStep.W2_CIFS, none))
    }

    @Test
    fun usbChrome_includesAvvioAndRiepilogo() {
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.S0_START))
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.PC_GUIDE))
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.W2_CIFS_INSTALL))
        assertTrue(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.DONE))
        assertFalse(UsbSetupCheckpointMessages.showsUsbChrome(WizardStep.W3_GUIDE))
    }

    @Test
    fun phaseTitle_replacesSubtitleOnPhasePages() {
        val none = List(5) { false }
        assertEquals(
            "Fase 1 – Creazione file di configurazione",
            UsbSetupCheckpointMessages.phaseTitleForSubtitle(WizardStep.PC_GUIDE, none),
        )
        assertEquals(
            PcSetupGuideContent.cifs61Title(),
            UsbSetupCheckpointMessages.phaseTitleForSubtitle(WizardStep.W2_CIFS_INSTALL, none),
        )
        assertEquals(
            PcSetupGuideContent.cifs62Title(),
            UsbSetupCheckpointMessages.phaseTitleForSubtitle(WizardStep.W2_CIFS, none),
        )
        assertNull(
            UsbSetupCheckpointMessages.phaseTitleForSubtitle(WizardStep.PC_AVAILABILITY, none),
        )
    }

    @Test
    fun totalPhases_areSeven() {
        assertEquals(7, PcSetupGuideContent.TOTAL_PHASES)
    }
}
