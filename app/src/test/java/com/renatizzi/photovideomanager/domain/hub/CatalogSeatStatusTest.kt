package com.renatizzi.photovideomanager.domain.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSeatStatusTest {

    @Test
    fun kindLabels_coverW1toW4() {
        assertEquals("Questo dispositivo", CatalogSeatKindLabels.label(CatalogSeatKind.LOCAL_DEVICE))
        assertEquals("PC Windows", CatalogSeatKindLabels.label(CatalogSeatKind.PC_NOTEBOOK))
        assertEquals("Cloud", CatalogSeatKindLabels.label(CatalogSeatKind.CLOUD_DRIVE))
        assertEquals("Disco di Rete", CatalogSeatKindLabels.label(CatalogSeatKind.NETWORK_NAS))
        assertEquals("", CatalogSeatKindLabels.label(CatalogSeatKind.NONE))
    }

    @Test
    fun status_notConfiguredWhenKindNone() {
        val status = CatalogSeatStatus(
            seat = CatalogSeat(kind = CatalogSeatKind.NONE),
            reachability = CatalogSeatReachability.NOT_CONFIGURED,
        )
        assertFalse(status.configured)
        assertEquals(CatalogSeatReachability.NOT_CONFIGURED, status.reachability)
    }

    @Test
    fun status_configuredPcSeat() {
        val seat = CatalogSeat(
            kind = CatalogSeatKind.PC_NOTEBOOK,
            treeUri = "content://tree/abc",
            displayLabel = "\\\\192.168.1.20\\MediaManagerCatalog",
            catalogName = "MyMedia",
            deviceModel = "PC-UFFICIO",
        )
        assertTrue(seat.isConfigured)
        val status = CatalogSeatStatus(seat, CatalogSeatReachability.REACHABLE)
        assertTrue(status.configured)
        assertEquals("MyMedia", status.catalogName)
        assertEquals(
            "PC-UFFICIO \\\\192.168.1.20\\MediaManagerCatalog - 🛜 Online",
            CatalogSeatRowFormatter.detailLine(status),
        )
    }

    @Test
    fun rowFormatter_localUsesSdOnlyWhenSecondary_andBackslashPath() {
        val primary = CatalogSeat(
            kind = CatalogSeatKind.LOCAL_DEVICE,
            displayLabel = "Pictures/Catalogo",
            catalogName = "MyMedia",
            deviceModel = "S25 Ultra",
            memoryLabel = CatalogSeat.MEMORY_PRIMARY,
        )
        val online = CatalogSeatStatus(primary, CatalogSeatReachability.REACHABLE)
        assertEquals(
            "S25 Ultra \\Pictures\\Catalogo - 🛜 Online",
            CatalogSeatRowFormatter.detailLine(online),
        )
        val offline = CatalogSeatStatus(primary, CatalogSeatReachability.UNREACHABLE)
        assertEquals(
            "S25 Ultra \\Pictures\\Catalogo - ⚠ Offline",
            CatalogSeatRowFormatter.detailLine(offline),
        )
        val nv = CatalogSeatStatus(primary, CatalogSeatReachability.UNKNOWN)
        assertEquals(
            "S25 Ultra \\Pictures\\Catalogo - ? Non verificabile",
            CatalogSeatRowFormatter.detailLine(nv),
        )

        val secondary = primary.copy(memoryLabel = CatalogSeat.MEMORY_SECONDARY)
        assertEquals(
            "S25 Ultra SD \\Pictures\\Catalogo - 🛜 Online",
            CatalogSeatRowFormatter.detailLine(
                CatalogSeatStatus(secondary, CatalogSeatReachability.REACHABLE),
            ),
        )
    }

    @Test
    fun displayPath_prefixesBackslash_keepsUnc() {
        assertEquals("\\pictures\\vacanze", CatalogSeatRowFormatter.displayPath("pictures/vacanze"))
        assertEquals("\\pictures\\vacanze", CatalogSeatRowFormatter.displayPath("\\pictures\\vacanze"))
        assertEquals(
            "\\\\192.168.1.20\\share",
            CatalogSeatRowFormatter.displayPath("\\\\192.168.1.20\\share"),
        )
        assertEquals("—", CatalogSeatRowFormatter.displayPath(""))
    }

    @Test
    fun defaults_areMyMediaMyDevice() {
        assertEquals("MyMedia", CatalogSeat.DEFAULT_CATALOG_NAME)
        assertEquals("MyDevice", CatalogSeat.DEFAULT_DEVICE_MODEL)
    }
}
