package com.renatizzi.photovideomanager.domain.hub

import com.renatizzi.photovideomanager.domain.model.Availability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogListRowFormatTest {
    @Test
    fun metaLine_includesSdOnlyWhenSecondary() {
        val withSd = CatalogListRowFormat.metaLine(
            deviceLabel = "Questo dispositivo",
            pathLabel = "DCIM/Camera",
            statusLabel = "Online",
            secondary = true,
        )
        assertTrue(withSd.contains("SD"))
        assertTrue(withSd.contains("Online"))

        val noSd = CatalogListRowFormat.metaLine(
            deviceLabel = "Questo dispositivo",
            pathLabel = "Pictures/familiari",
            statusLabel = "Online",
            secondary = false,
        )
        assertFalse(noSd.contains("SD"))
    }

    @Test
    fun metaLine_folderRow_omitsDeviceWhenEmpty() {
        val meta = CatalogListRowFormat.metaLine(
            deviceLabel = "",
            pathLabel = "Pictures/familiari",
            statusLabel = "Online",
            secondary = false,
        )
        assertTrue(meta.startsWith("\\") || meta.contains("Pictures"))
        assertTrue(meta.endsWith("Online") || meta.contains(" - Online"))
    }

    @Test
    fun availabilityLabel_mapsKnownStatesWithIcon() {
        assertEquals("🛜 Online", CatalogListRowFormat.availabilityLabel(Availability.AVAILABLE))
        assertEquals("⚠ Offline", CatalogListRowFormat.availabilityLabel(Availability.UNAVAILABLE))
        assertEquals(
            "? Non verificabile",
            CatalogListRowFormat.availabilityLabel(Availability.UNKNOWN),
        )
    }
}
