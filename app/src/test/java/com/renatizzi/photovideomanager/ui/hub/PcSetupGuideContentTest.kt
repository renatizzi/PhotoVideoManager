package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcSetupGuideContentTest {
    @Test
    fun modeLabels_haveNoLetterPrefixes() {
        PcLinkMode.entries.forEach { mode ->
            val label = PcSetupGuideContent.modeLabel(mode)
            assertFalse(label.startsWith("A)"))
            assertFalse(label.startsWith("B)"))
            assertFalse(label.startsWith("C)"))
        }
        assertTrue(PcSetupGuideContent.modeLabel(PcLinkMode.ALREADY_PAIRED).contains("Telefono"))
        assertTrue(PcSetupGuideContent.modeLabel(PcLinkMode.NEED_PAIRING).contains("Telefono"))
        assertTrue(PcSetupGuideContent.modeLabel(PcLinkMode.USB).contains("USB"))
    }

    @Test
    fun alreadyPaired_doesNotForcePairing_andMentionsDesktopCopy() {
        val body = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED)[0].body
        assertTrue(body.contains("non ripetere"))
        assertTrue(body.contains("Desktop"))
        assertTrue(body.contains("ricezione") || body.contains("File ricevuti"))
        assertFalse(body.contains("Confermo di aver"))
    }

    @Test
    fun needPairing_startsFromPhoneSettings() {
        val body = PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING)[0].body
        assertTrue(body.contains("Bluetooth"))
        assertTrue(body.contains("codice"))
    }

    @Test
    fun scriptPhase_mentionsDatiCifsAndListening() {
        val body = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED)[1].body
        assertTrue(body.contains("DATI_CIFS.json"))
        assertTrue(body.contains("CONFIGURA"))
        assertTrue(body.contains("ascolta") || body.contains("ascolto"))
    }

    @Test
    fun actionLabels_areIdenticalAcrossModes_andNotConfirmations() {
        val a = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED).map { it.actionLabel }
        val b = PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING).map { it.actionLabel }
        val c = PcSetupGuideContent.phases(PcLinkMode.USB).map { it.actionLabel }
        assertEquals(a, b)
        assertEquals(a, c)
        assertEquals(4, a.size)
        a.forEach { label ->
            assertFalse(label.contains("Confermo"))
            assertFalse(label.equals("Continua", ignoreCase = true))
            assertFalse(label.contains("Puoi passare"))
        }
        assertTrue(a[0].contains("Invia"))
        assertTrue(a[2].contains("Usa questi dati"))
    }

    @Test
    fun guideDescription_hasInlinePrintAnchor() {
        assertTrue(PcSetupGuideContent.guideDescription().endsWith("qui"))
    }
}
