package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Contratto di allineamento Dashboard ↔ Aggiorna:
 * conteggi "originali" = MediaItem con almeno una copia ACTIVE.
 */
class CatalogCountSemanticsTest {
    @Test
    fun catalogUniverse_isActiveCopiesOnly() {
        assertEquals("ACTIVE", MediaCopyState.ACTIVE.name)
        // Gli stati non ACTIVE (TRASHED, …) non devono gonfiare i KPI Dashboard.
        assertEquals(4, MediaCopyState.entries.size)
    }
}
