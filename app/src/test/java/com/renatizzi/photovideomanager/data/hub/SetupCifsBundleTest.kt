package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCifsBundleTest {

    @Test
    fun isComplete_requiresIpv4QualifiedUserPasswordFolder() {
        val incomplete = SetupCifsBundle(
            token = "tok",
            hostIp = "not-an-ip",
            userQualified = "MediaManager",
            password = "x",
            folder = "MediaManagerCatalog",
            connectionName = "MediaManagerCatalog",
        )
        assertFalse(incomplete.isComplete)

        val full = SetupCifsBundle(
            token = "tok",
            hostIp = "192.168.68.62",
            userQualified = "DELLRS\\MediaManager",
            password = "vjqjNJiPv8c@Y8",
            folder = "MediaManagerCatalog",
            connectionName = "MediaManagerCatalog",
        )
        assertTrue(full.isComplete)
    }

    @Test
    fun parse_powerShellLongKeys_roundTrip() {
        val json =
            "{\"hostIp\":\"192.168.43.10\",\"userQualified\":\"DELLRS\\\\MyMedia\"," +
                "\"password\":\"Abcd1234!xyz\",\"port\":\"445\",\"connectionName\":\"MyMedia\"," +
                "\"folder\":\"MyMedia\",\"shareName\":\"MyMedia\",\"computerName\":\"DELLRS\"," +
                "\"storage\":\"SMB2,3 (jCIFS NG)\",\"token\":\"tok1\"}"
        val bundle = SetupCifsBundle.parse(json, "tok1")
        assertNotNull(bundle)
        assertTrue(bundle!!.isComplete)
        assertEquals("192.168.43.10", bundle.hostIp)
        assertEquals("DELLRS\\MyMedia", bundle.userQualified)
        assertEquals("Abcd1234!xyz", bundle.password)
        assertEquals("MyMedia", bundle.folder)
    }

    @Test
    fun parse_rejectsWrongToken() {
        val json =
            "{\"hostIp\":\"10.0.0.2\",\"userQualified\":\"PC\\\\MyMedia\"," +
                "\"password\":\"secret\",\"folder\":\"MyMedia\",\"connectionName\":\"MyMedia\"," +
                "\"token\":\"tokA\"}"
        assertNull(SetupCifsBundle.parse(json, "tokB"))
    }

    @Test
    fun parse_withoutPassword_notComplete() {
        val json =
            "{\"hostIp\":\"192.168.43.10\",\"userQualified\":\"DELLRS\\\\MyMedia\"," +
                "\"folder\":\"MyMedia\",\"connectionName\":\"MyMedia\",\"token\":\"tok1\"}"
        val partial = SetupCifsBundle.parse(json, "tok1")
        assertNotNull(partial)
        assertFalse(partial!!.isComplete)
    }

    @Test
    fun toJsonCompact_roundTripsThroughParse() {
        val bundle = SetupCifsBundle(
            token = "tokRound",
            hostIp = "192.168.43.5",
            userQualified = "MINI\\MediaManager",
            password = "p@ss",
            computerName = "MINI",
            folder = "MediaManagerCatalog",
            connectionName = "MediaManagerCatalog",
            shareName = "MediaManagerCatalog",
        )
        val json = SetupCifsBundle.toJsonCompact(bundle)
        assertTrue(json.contains("\"password\":\"p@ss\""))
        assertTrue(json.all { it.code <= 127 })
        val again = SetupCifsBundle.parse(json, "tokRound")
        assertNotNull(again)
        assertTrue(again!!.isComplete)
        assertEquals(bundle.hostIp, again.hostIp)
        assertEquals(bundle.password, again.password)
        assertEquals(bundle.folder, again.folder)
    }
}
