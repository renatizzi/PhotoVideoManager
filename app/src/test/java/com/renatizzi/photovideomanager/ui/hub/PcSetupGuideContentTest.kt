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
    fun alreadyPaired_doesNotForcePairing() {
        val body = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED)[0].body
        assertTrue(body.contains("già associati") || body.contains("non ripetere"))
        assertTrue(body.contains("Desktop"))
        assertTrue(body.contains("MediaManager"))
    }

    @Test
    fun needPairing_startsFromPhoneAndMentionsBusyLink() {
        val body = PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING)[0].body
        assertTrue(body.contains("dal Telefono"))
        assertTrue(body.contains("impegnato") || body.contains("non si vedono"))
    }

    @Test
    fun scriptPhase_mentionsDatiCifsAndIgnoreNoPw() {
        val body = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED)[1].body
        assertTrue(body.contains("DATI_CIFS.json"))
        assertTrue(body.contains("no_pw.json"))
        assertTrue(body.contains("CONFIGURA"))
    }

    @Test
    fun confirmMessages_areIdenticalAcrossModes() {
        val a = PcSetupGuideContent.phases(PcLinkMode.ALREADY_PAIRED).map { it.confirmMessage }
        val b = PcSetupGuideContent.phases(PcLinkMode.NEED_PAIRING).map { it.confirmMessage }
        val c = PcSetupGuideContent.phases(PcLinkMode.USB).map { it.confirmMessage }
        assertEquals(a, b)
        assertEquals(a, c)
        assertEquals(4, a.size)
    }
}
