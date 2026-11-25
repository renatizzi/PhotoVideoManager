package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcSetupGuideContentTest {
    @Test
    fun usbPhases_areFive_withOkAndFatto() {
        val phases = PcSetupGuideContent.usbPhases()
        assertEquals(5, phases.size)
        assertEquals(PcSetupGuideContent.PHASE_COUNT, phases.size)
        assertEquals(7, PcSetupGuideContent.TOTAL_PHASES)
        assertEquals("OK", phases[0].actionLabel)
        phases.drop(1).forEach { assertEquals("Fatto", it.actionLabel) }
    }

    @Test
    fun phase1_createsFilesInDownload_noBluetooth() {
        val phase = PcSetupGuideContent.usbPhases()[0]
        assertTrue(phase.title.contains("Creazione file"))
        assertTrue(phase.body.contains("Download"))
        assertFalse(phase.body.contains("Bluetooth", ignoreCase = true))
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
    fun phase5_importViaSelector() {
        val body = PcSetupGuideContent.usbPhases()[4].body
        assertTrue(body.contains("Importa", ignoreCase = true) || body.contains("import"))
        assertTrue(body.contains("DATI_CIFS.json"))
        assertTrue(body.contains("selettore") || body.contains("Download"))
    }

    @Test
    fun pageGroups_areSinglePhaseEach() {
        repeat(PcSetupGuideContent.PHASE_COUNT) { i ->
            assertEquals(i..i, PcSetupGuideContent.pageGroupFor(i))
        }
    }

    @Test
    fun sevenPhaseOverview_hasSevenEntries() {
        assertEquals(7, PcSetupGuideContent.sevenPhaseOverview().size)
        assertTrue(PcSetupGuideContent.sevenPhaseOverview().last().contains("Catalogo"))
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
    fun cifsSteps_arePhase6And7() {
        assertTrue(PcSetupGuideContent.cifs61Title().startsWith("Fase 6"))
        assertTrue(PcSetupGuideContent.cifs62Title().startsWith("Fase 7"))
        assertFalse(PcSetupGuideContent.cifs61Title().contains("6/1"))
        assertFalse(PcSetupGuideContent.cifs62Title().contains("6/2"))
        assertEquals(3, PcSetupGuideContent.cifs61Bullets().size)
        assertEquals(9, PcSetupGuideContent.cifs62Bullets().size)
    }

    @Test
    fun phaseManagedLabels_areOnlyTwoUniformTexts() {
        val phases = PcSetupGuideContent.usbPhases()
        assertEquals(PcSetupGuideContent.PHASE_MANAGED_BY_APP, phases[0].bullets.first())
        assertEquals(PcSetupGuideContent.PHASE_MANAGED_MANUALLY, phases[1].bullets.first())
        assertEquals(PcSetupGuideContent.PHASE_MANAGED_MANUALLY, phases[2].bullets.first())
        assertEquals(PcSetupGuideContent.PHASE_MANAGED_MANUALLY, phases[3].bullets.first())
        assertEquals(PcSetupGuideContent.PHASE_MANAGED_BY_APP, phases[4].bullets.first())
        assertEquals(
            PcSetupGuideContent.PHASE_MANAGED_MANUALLY,
            PcSetupGuideContent.cifs61Bullets().first(),
        )
        assertEquals(
            PcSetupGuideContent.PHASE_MANAGED_MANUALLY,
            PcSetupGuideContent.cifs62Bullets().first(),
        )
        val corpus = buildString {
            append(phases.joinToString("\n") { it.body })
            append('\n')
            append(PcSetupGuideContent.cifs61Body())
            append('\n')
            append(PcSetupGuideContent.cifs62Body())
        }
        assertFalse(corpus.contains("dopo la tua conferma"))
        assertFalse(corpus.contains("dopo la conferma dell’utente"))
        assertFalse(corpus.contains("gestita dall’utente"))
        assertFalse(corpus.contains("manualmente dall’utente"))
        assertEquals("✔", PcSetupGuideContent.BULLET_MARK)
        assertTrue(PcSetupGuideContent.cifs62Bullets()[1].startsWith("Aperto CIFS"))
        assertFalse(PcSetupGuideContent.cifs62Body().contains("Apri CIFS >"))
    }

    @Test
    fun usbConnect_usesNotificationPanelNotHome() {
        val joined = PcSetupGuideContent.usbConnectBullets().joinToString(" ")
        assertTrue(joined.contains("notifiche"))
        assertFalse(joined.contains("Home"))
    }

    @Test
    fun currentGuidePhaseIndex_advancesWithDoneFlags() {
        assertEquals(0, PcSetupGuideContent.currentGuidePhaseIndex(List(5) { false }))
        assertEquals(
            2,
            PcSetupGuideContent.currentGuidePhaseIndex(listOf(true, true, false, false, false)),
        )
        assertEquals(4, PcSetupGuideContent.currentGuidePhaseIndex(List(5) { true }))
    }
}
