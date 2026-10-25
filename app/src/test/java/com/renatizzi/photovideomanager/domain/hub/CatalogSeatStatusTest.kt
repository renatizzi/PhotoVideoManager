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
            displayLabel = "MediaManagerCatalog",
        )
        assertTrue(seat.isConfigured)
        val status = CatalogSeatStatus(seat, CatalogSeatReachability.REACHABLE)
        assertTrue(status.configured)
        assertEquals("MediaManagerCatalog", status.label)
    }
}
