package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.HubPackage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * LOCK UX: se la sede Catalogo è PC e la sorgente CIFS è la cartella hub
 * (`MediaManager`) o lo share Catalogo, Acquisisci deve etichettare PC Windows
 * con path `C:\Nome\MediaManager` — non «Disco di rete / NAS \MediaManager».
 */
class SourceRegistryPcSeatMatchTest {

    @Test
    fun opaqueCifs_mediaManagerHub_isPcWhenSeatIsPc() {
        SafPathLabels.rememberPcCatalogShare("MyC")
        val opaque =
            "content://com.wa2c.android.cifsdocumentsprovider.documents/tree/" +
                "c8353c54-96f1-4896-a525-296d49ad9b15%3A"
        // Stesso criterio usato da SourceRegistry.matchesCatalogSeatTree (nome hub).
        assertTrue(
            HubPackage.RELATIVE_DIR.equals("MediaManager", ignoreCase = true),
        )
        assertEquals(
            SafPathLabels.DEVICE_PC_NOTEBOOK,
            SafPathLabels.networkDeviceKindFromHints(
                displayHint = "MyC",
                path = "MyC",
                uriString = opaque,
            ),
        )
    }

    @Test
    fun seatPathForDisplay_windowsStyle() {
        val seat = com.renatizzi.photovideomanager.domain.hub.CatalogSeat(
            kind = com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.PC_NOTEBOOK,
            treeUri = "content://tree/x",
            catalogName = "MyC",
            windowsFolderPath = "C:\\MyC",
        )
        assertEquals("C:\\MyC\\MediaManager", seat.seatPathForDisplay)
    }
}
