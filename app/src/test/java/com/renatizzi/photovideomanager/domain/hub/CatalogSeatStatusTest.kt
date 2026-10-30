package com.renatizzi.photovideomanager.domain.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogSeatStatusTest {

    @Test
    fun kindLabels_coverW1toW4() {
        assertEquals("Su questo dispositivo", CatalogSeatKindLabels.label(CatalogSeatKind.LOCAL_DEVICE))
        assertEquals("Su PC / notebook", CatalogSeatKindLabels.label(CatalogSeatKind.PC_NOTEBOOK))
        assertEquals("Su Google Drive / cloud", CatalogSeatKindLabels.label(CatalogSeatKind.CLOUD_DRIVE))
        assertEquals("Su disco di rete / NAS", CatalogSeatKindLabels.label(CatalogSeatKind.NETWORK_NAS))
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
            catalogName = "MediaManagerCatalog",
            deviceModel = "PC-UFFICIO",
        )
        assertTrue(seat.isConfigured)
        val status = CatalogSeatStatus(seat, CatalogSeatReachability.REACHABLE)
        assertTrue(status.configured)
        assertEquals("MediaManagerCatalog", status.catalogName)
        assertEquals(
            "PC-UFFICIO, \\\\192.168.1.20\\MediaManagerCatalog - Online",
            CatalogSeatRowFormatter.detailLine(status),
        )
    }

    @Test
    fun rowFormatter_localUsesMemoryAndOnlineOfflineNv() {
        val seat = CatalogSeat(
            kind = CatalogSeatKind.LOCAL_DEVICE,
            displayLabel = "Pictures/Catalogo",
            catalogName = "MediaManagerCatalog",
            deviceModel = "S25 Ultra",
            memoryLabel = CatalogSeat.MEMORY_PRIMARY,
        )
        val online = CatalogSeatStatus(seat, CatalogSeatReachability.REACHABLE)
        assertEquals(
            "S25 Ultra, Principale, Pictures/Catalogo - Online",
            CatalogSeatRowFormatter.detailLine(online),
        )
        val offline = CatalogSeatStatus(seat, CatalogSeatReachability.UNREACHABLE)
        assertEquals(
            "S25 Ultra, Principale, Pictures/Catalogo - Offline",
            CatalogSeatRowFormatter.detailLine(offline),
        )
        val nv = CatalogSeatStatus(seat, CatalogSeatReachability.UNKNOWN)
        assertEquals(
            "S25 Ultra, Principale, Pictures/Catalogo - Non verificabile",
            CatalogSeatRowFormatter.detailLine(nv),
        )
    }
}
