package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupCifsBundleTest {

    @Test
    fun parse_requiresPasswordForComplete() {
        val noPwd =
            "{\"v\":3,\"m\":\"PVMSETUP1\",\"t\":\"tok1\"," +
                "\"ip\":\"192.168.43.10\",\"user\":\"DELLRS\\\\MyMedia\"," +
                "\"share\":\"MyMedia\",\"pc\":\"DELLRS\"}"
        val partial = SetupCifsBundle.parse(noPwd, "tok1")
        assertNotNull(partial)
        assertFalse(partial!!.isComplete)

        val withPwd =
            "{\"v\":3,\"m\":\"PVMSETUP1\",\"t\":\"tok1\"," +
                "\"ip\":\"192.168.43.10\",\"user\":\"DELLRS\\\\MyMedia\"," +
                "\"password\":\"Abcd1234!xyz\",\"share\":\"MyMedia\",\"pc\":\"DELLRS\"," +
                "\"port\":\"445\",\"folder\":\"MyMedia\",\"name\":\"MyMedia\"}"
        val full = SetupCifsBundle.parse(withPwd, "tok1")
        assertNotNull(full)
        assertTrue(full!!.isComplete)
        assertEquals("192.168.43.10", full.hostIp)
        assertEquals("DELLRS\\MyMedia", full.userQualified)
        assertEquals("Abcd1234!xyz", full.password)
    }

    @Test
    fun parse_rejectsWrongToken() {
        val raw =
            "{\"v\":3,\"m\":\"PVMSETUP1\",\"t\":\"tokA\"," +
                "\"ip\":\"10.0.0.2\",\"user\":\"PC\\\\MyMedia\"," +
                "\"password\":\"secret\"}"
        assertNull(SetupCifsBundle.parse(raw, "tokB"))
    }

    @Test
    fun toJsonCompact_roundTripsThroughParse() {
        val bundle = SetupCifsBundle(
            token = "tokRound",
            hostIp = "192.168.43.5",
            userQualified = "MINI\\MyMedia",
            password = "p@ss",
            computerName = "MINI",
        )
        val json = SetupCifsBundle.toJsonCompact(bundle)
        assertTrue(json.contains("\"password\":\"p@ss\""))
        val again = SetupCifsBundle.parse(json, "tokRound")
        assertNotNull(again)
        assertTrue(again!!.isComplete)
        assertEquals(bundle.hostIp, again.hostIp)
        assertEquals(bundle.password, again.password)
    }

    @Test
    fun bundleFileNameForToken_isUniqueAndRecognized() {
        val name = SetupCifsBundle.bundleFileNameForToken("abc123def456")
        assertEquals("MediaManager_cifs_bundle_abc123de.json", name)
        assertTrue(SetupCifsBundle.isBundleFileName(name))
        assertTrue(SetupCifsBundle.isBundleFileName(SetupCifsBundle.BUNDLE_FILE_NAME))
        assertFalse(SetupCifsBundle.isBundleFileName("MediaManager_setup_result.json"))
    }

    @Test
    fun looksLikeSetupResultNotBundle_detectsResultJson() {
        val result =
            "{\"status\":\"completed\",\"computerName\":\"DELLRS\"," +
                "\"userFormatHint\":\"DELLRS\\\\MyMedia\",\"ipv4\":[\"192.168.68.62\"]}"
        assertTrue(SetupCifsBundle.looksLikeSetupResultNotBundle(result))
        val bundle =
            "{\"m\":\"PVMSETUP1\",\"t\":\"t\",\"ip\":\"1.2.3.4\"," +
                "\"user\":\"A\\\\MyMedia\",\"password\":\"x\"}"
        assertFalse(SetupCifsBundle.looksLikeSetupResultNotBundle(bundle))
    }

    @Test
    fun lanPayload_isCompleteOnlyWithPassword() {
        val p = SetupLanAnnounce.parsePayload(
            "{\"m\":\"PVMSETUP1\",\"t\":\"t\",\"ip\":\"1.2.3.4\"," +
                "\"user\":\"A\\\\MyMedia\",\"password\":\"x\"}",
        )
        assertNotNull(p)
        assertTrue(p!!.isComplete)
        val noPwd = SetupLanAnnounce.parsePayload(
            "{\"m\":\"PVMSETUP1\",\"t\":\"t\",\"ip\":\"1.2.3.4\",\"user\":\"A\\\\MyMedia\"}",
        )
        assertNotNull(noPwd)
        assertFalse(noPwd!!.isComplete)
    }
}
