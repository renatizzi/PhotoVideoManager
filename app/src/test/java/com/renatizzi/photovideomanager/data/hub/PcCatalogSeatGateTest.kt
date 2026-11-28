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
    fun assessChildNames_rejectsHostShareEnumeration() {
        // Folder vuoto: figli = elenco share (ADMIN$/IPC$), non disco.
        val bad = PcCatalogSeatGate.assessChildNames(
            listOf("ADMIN$", "C$", "IPC$", "MyMedia", "Users"),
            expectedShare = "MyMedia",
        )
        assertFalse(bad.ok)
        assertTrue(bad.reason!!.contains("Folder=MyMedia"))
        assertTrue(bad.reason!!.contains("elenco delle share") || bad.reason!!.contains("ADMIN$"))
    }

    @Test
    fun assessChildNames_acceptsDriveRootEvenIfCatalogFolderPresent() {
        // Share = radice disco MyTEST: figli tipici di C:\ inclusa cartella Catalogo omonima.
        val ok = PcCatalogSeatGate.assessChildNames(
            listOf(
                "Users", "Windows", "Program Files", "MyTEST",
                "Dell XPS 15 7590", "ProgramData",
            ),
            expectedShare = "MyTEST",
        )
        assertTrue(ok.ok)
    }

    @Test
    fun assessChildNames_acceptsCatalogFolderWithHub() {
        val ok = PcCatalogSeatGate.assessChildNames(
            listOf("MediaManager", "foto.jpg"),
            expectedShare = "MyMedia",
        )
        assertTrue(ok.ok)
    }

    @Test
    fun assessChildNames_acceptsEmpty() {
        assertTrue(PcCatalogSeatGate.assessChildNames(emptyList(), "MyMedia").ok)
    }

    @Test
    fun writableProbeFailureHint_mentionsFolderAndRetry() {
        val hint = PcCatalogSeatGate.writableProbeFailureHint("MyTEST")
        assertTrue(hint.contains("Folder=MyTEST"))
        assertTrue(hint.contains("Read only", ignoreCase = true))
        assertTrue(hint.contains("Attiva Catalogo"))
    }
}
