package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsSetupResultParserTest {

    @Test
    fun parse_completedResult_readsUserHostAndShare() {
        // Ordinary "…" strings: \\\\ → \\ in JSON text (escaped backslash).
        val json =
            "{\"app\":\"MediaManager\",\"version\":1,\"status\":\"completed\"," +
                "\"computerName\":\"MINI_IT13\"," +
                "\"folderPath\":\"G:\\\\MediaManager\\\\Catalogo\"," +
                "\"shareName\":\"MediaManagerCatalog\"," +
                "\"userName\":\"MediaManager\"," +
                "\"userFormatHint\":\"MINI_IT13\\\\MediaManager\"," +
                "\"ipv4\":[\"192.168.1.40\"]}"
        val result = WindowsSetupResultParser.parse(json)
        assertNotNull(result)
        assertTrue(result!!.isCompleted)
        assertEquals("MINI_IT13", result.computerName)
        assertEquals("MINI_IT13\\MediaManager", WindowsSetupResultParser.resolvedUser(result))
        assertEquals("192.168.1.40", result.primaryIpv4)
        assertEquals("MediaManagerCatalog", result.shareName)
    }

    @Test
    fun parse_missingHint_buildsFromComputerName() {
        val json =
            "{\"status\":\"completed\",\"computerName\":\"DESKTOP01\"," +
                "\"userName\":\"MediaManager\",\"userFormatHint\":\"\"," +
                "\"shareName\":\"MediaManagerCatalog\"," +
                "\"folderPath\":\"C:\\\\MediaManager\\\\Catalogo\",\"ipv4\":[]}"
        val result = WindowsSetupResultParser.parse(json)
        assertNotNull(result)
        assertEquals("DESKTOP01\\MediaManager", WindowsSetupResultParser.resolvedUser(result!!))
        assertEquals("", result.primaryIpv4)
        assertFalse(result.primaryIpv4.isNotEmpty())
    }
}
