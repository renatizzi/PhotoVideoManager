package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gate ambienti: testi vivi del percorso USB devono coincidere con la specifica REV4
 * (niente formulazioni USB2.3 / 6-fasi / Bluetooth nel contenuto mostrato).
 */
class UsbRev4ContentGateTest {
    @Test
    fun sevenPhases_titlesMatchRev4() {
        val phases = PcSetupGuideContent.usbPhases()
        assertEquals("Fase 1 – Creazione file di configurazione", phases[0].title)
        assertEquals("Fase 2 – Invio script al PC", phases[1].title)
        assertEquals("Fase 3 – Esecuzione script sul PC", phases[2].title)
        assertEquals("Fase 4 – Invio dati CIFS al Telefono", phases[3].title)
        assertEquals("Fase 5 – Import dati CIFS", phases[4].title)
        assertEquals("Fase 6 – Installa / Apri CIFS", PcSetupGuideContent.cifs61Title())
        assertEquals("Fase 7 – Configura CIFS e attiva Catalogo", PcSetupGuideContent.cifs62Title())
    }

    @Test
    fun liveGuideTexts_haveNoBluetoothOrSixOne() {
        val corpus = buildString {
            append(PcSetupGuideContent.usbPhases().joinToString("\n") { it.body })
            append('\n')
            append(PcSetupGuideContent.cifs61Body())
            append('\n')
            append(PcSetupGuideContent.cifs62Body())
            append('\n')
            append(PcSetupGuideContent.usbConnectBullets().joinToString("\n"))
            append('\n')
            append(PcSetupGuideContent.guideDescription())
            append('\n')
            append(PcSetupGuideContent.associationDescription())
        }
        assertFalse(corpus.contains("Bluetooth", ignoreCase = true))
        assertFalse(corpus.contains("6/1"))
        assertFalse(corpus.contains("6/2"))
        assertFalse(corpus.contains("Home"))
        assertFalse(corpus.contains("pairing", ignoreCase = true))
        assertTrue(corpus.contains("notifiche"))
        assertTrue(corpus.contains("Download"))
        assertTrue(corpus.contains("DATI_CIFS.json"))
    }

    @Test
    fun structure_singlePagePerPhase_noStripModel() {
        assertEquals(7, PcSetupGuideContent.TOTAL_PHASES)
        repeat(5) { i ->
            assertEquals(i..i, PcSetupGuideContent.pageGroupFor(i))
        }
        assertEquals(7, PcSetupGuideContent.sevenPhaseOverview().size)
    }

    @Test
    fun printGuideAsset_isUsbOnly() {
        assertTrue(PcSetupGuideContent.GUIDE_PDF_ASSET.contains("USB_ONLY"))
    }

    @Test
    fun phaseManagedLabels_matchApprovedUniformTexts() {
        assertEquals("Fase gestita dall’app", PcSetupGuideContent.PHASE_MANAGED_BY_APP)
        assertEquals("Fase gestita manualmente", PcSetupGuideContent.PHASE_MANAGED_MANUALLY)
        assertEquals("✔", PcSetupGuideContent.BULLET_MARK)
        val firsts = PcSetupGuideContent.usbPhases().map { it.bullets.first() } +
            listOf(
                PcSetupGuideContent.cifs61Bullets().first(),
                PcSetupGuideContent.cifs62Bullets().first(),
            )
        firsts.forEach { label ->
            assertTrue(
                label == PcSetupGuideContent.PHASE_MANAGED_BY_APP ||
                    label == PcSetupGuideContent.PHASE_MANAGED_MANUALLY,
            )
        }
    }
}
