package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcCatalogSeatGateTest {

    @Test
    fun cifsFolderPin_isShareNameNotWindowsPath() {
        assertEquals("MyMedia", PcCatalogSeatGate.cifsFolderPin())
        assertEquals("MyMedia", PcCatalogSeatGate.cifsFolderPin(""))
        assertEquals("CatalogShare", PcCatalogSeatGate.cifsFolderPin("CatalogShare"))
    }

    @Test
    fun assessChildNames_rejectsShareEnumerationRoot() {
        // Folder vuoto + pick connessione: figli = elenco share del PC (include MyMedia).
        val bad = PcCatalogSeatGate.assessChildNames(
            listOf("ADMIN$", "C$", "IPC$", "MyMedia", "Users"),
            expectedShare = "MyMedia",
        )
        assertFalse(bad.ok)
        assertTrue(bad.reason!!.contains("Folder=MyMedia"))
        assertTrue(bad.reason!!.contains("elenco delle share"))
    }

    @Test
    fun assessChildNames_acceptsPinnedShareContents() {
        // Folder=MyMedia: figli = contenuto della share (non la share stessa).
        val okEmpty = PcCatalogSeatGate.assessChildNames(emptyList(), "MyMedia")
        assertTrue(okEmpty.ok)

        val okWithHub = PcCatalogSeatGate.assessChildNames(
            listOf("MediaManager", "foto.jpg"),
            expectedShare = "MyMedia",
        )
        assertTrue(okWithHub.ok)
    }

    @Test
    fun assessChildNames_isCaseInsensitiveOnShare() {
        val bad = PcCatalogSeatGate.assessChildNames(listOf("mymedia"), "MyMedia")
        assertFalse(bad.ok)
    }

    @Test
    fun writableProbeFailureHint_mentionsFolderAndReadOnly() {
        val hint = PcCatalogSeatGate.writableProbeFailureHint("MyMedia")
        assertTrue(hint.contains("Folder=MyMedia"))
        assertTrue(hint.contains("Read only", ignoreCase = true))
    }
}
