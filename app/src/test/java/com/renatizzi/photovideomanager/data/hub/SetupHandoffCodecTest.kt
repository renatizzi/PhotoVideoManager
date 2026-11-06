package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupHandoffCodecTest {

    private val token = "abcTOKEN12345678"
    private val now = 1_700_000_000_000L

    @Test
    fun roundTrip_uri_preservesHostAndUser_noPassword() {
        val uri = SetupHandoffCodec.encodeUri(
            token = token,
            hostIp = "192.168.68.62",
            userQualified = "DELLRS\\MyMedia",
            shareName = "MyMedia",
            computerName = "DELLRS",
            nowEpochMs = now,
        )
        assertTrue(uri.startsWith(SetupHandoffCodec.URI_PREFIX))
        assertFalse(uri.contains("password", ignoreCase = true))
        assertFalse(uri.contains("vjqj", ignoreCase = true))

        val decoded = SetupHandoffCodec.decode(uri, expectedToken = token, nowEpochMs = now)
        assertTrue(decoded.isOk)
        val h = decoded.handoff!!
        assertEquals("192.168.68.62", h.hostIp)
        assertEquals("DELLRS\\MyMedia", h.userQualified)
        assertEquals("MyMedia", h.shareName)
        assertEquals("DELLRS", h.computerName)
        // QR/handoff senza password: utile per Host/User, ma NON bundle CIFS completo.
        assertTrue(h.isComplete)
        assertFalse(h.toLanPayload().isComplete)
        assertEquals("192.168.68.62", h.toLanPayload().hostForCifs)
    }

    @Test
    fun decode_rejectsWrongToken() {
        val uri = SetupHandoffCodec.encodeUri(
            token = token,
            hostIp = "10.0.0.2",
            userQualified = "PC\\MyMedia",
            nowEpochMs = now,
        )
        val decoded = SetupHandoffCodec.decode(uri, expectedToken = "otherToken999", nowEpochMs = now)
        assertFalse(decoded.isOk)
        assertTrue(decoded.error is SetupHandoffCodec.DecodeError.Token)
    }

    @Test
    fun decode_rejectsExpired() {
        val uri = SetupHandoffCodec.encodeUri(
            token = token,
            hostIp = "10.0.0.2",
            userQualified = "PC\\MyMedia",
            nowEpochMs = now,
            ttlMs = 1_000L,
        )
        val decoded = SetupHandoffCodec.decode(
            uri,
            expectedToken = token,
            nowEpochMs = now + 60_000L,
        )
        assertFalse(decoded.isOk)
        assertTrue(decoded.error is SetupHandoffCodec.DecodeError.Expired)
    }

    @Test
    fun decode_rejectsMissingIpv4() {
        val handoff = SetupHandoffCodec.Handoff(
            token = token,
            hostIp = "",
            userQualified = "PC\\MyMedia",
            shareName = "MyMedia",
            computerName = "PC",
            expiresAtEpochSec = now / 1000 + 3_600,
        )
        // encodeUri allows empty ip in object; decode must reject
        val uri = SetupHandoffCodec.encodeUri(handoff)
        val decoded = SetupHandoffCodec.decode(uri, token, now)
        assertFalse(decoded.isOk)
        assertTrue(decoded.error is SetupHandoffCodec.DecodeError.Incomplete)
    }

    @Test
    fun decode_acceptsLegacyLanJson() {
        val raw =
            "{\"v\":1,\"m\":\"PVMSETUP1\",\"t\":\"$token\"," +
                "\"ip\":\"192.168.1.40\"," +
                "\"user\":\"MINI_IT13\\\\MyMedia\"," +
                "\"share\":\"MyMedia\",\"pc\":\"MINI_IT13\"," +
                "\"exp\":${now / 1000 + 3600}}"
        val decoded = SetupHandoffCodec.decode(raw, token, now)
        assertTrue(decoded.isOk)
        assertEquals("192.168.1.40", decoded.handoff!!.hostIp)
    }

    @Test
    fun decode_rejectsPasswordField() {
        val raw =
            "{\"v\":2,\"m\":\"PVMSETUP1\",\"t\":\"$token\"," +
                "\"ip\":\"192.168.1.40\",\"user\":\"PC\\\\MyMedia\"," +
                "\"password\":\"secret\",\"exp\":${now / 1000 + 3600}}"
        val decoded = SetupHandoffCodec.decode(raw, token, now)
        assertFalse(decoded.isOk)
        assertNotNull(decoded.error)
        assertNull(decoded.handoff)
    }
}
