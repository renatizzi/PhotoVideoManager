package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SetupLanAnnounceTest {

    @Test
    fun parsePayload_readsTokenIpAndUser() {
        val raw =
            "{\"v\":1,\"m\":\"PVMSETUP1\",\"t\":\"tok123\"," +
                "\"ip\":\"192.168.1.40\"," +
                "\"user\":\"MINI_IT13\\\\MediaManager\"," +
                "\"share\":\"MediaManagerCatalog\",\"pc\":\"MINI_IT13\"}"
        val payload = SetupLanAnnounce.parsePayload(raw)
        assertNotNull(payload)
        assertEquals("tok123", payload!!.token)
        assertEquals("192.168.1.40", payload.hostIp)
        assertEquals("MINI_IT13\\MediaManager", payload.userQualified)
        assertEquals("MediaManagerCatalog", payload.shareName)
    }

    @Test
    fun parsePayload_rejectsWrongMagic() {
        assertNull(SetupLanAnnounce.parsePayload("{\"t\":\"x\",\"ip\":\"1.1.1.1\",\"user\":\"a\"}"))
    }
}
