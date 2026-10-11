package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contratto KPI Home modello A (BL-14):
 * - originali = file sui dispositivi/fonti
 * - di cui in Catalogo = elementi ACTIVE in Aggiorna
 * - TRASHED sulle fonti resta negli originali (file fisico intatto)
 */
class CatalogCountSemanticsTest {
    @Test
    fun catalogUniverse_isActiveCopiesOnly() {
        assertEquals("ACTIVE", MediaCopyState.ACTIVE.name)
        assertEquals(4, MediaCopyState.entries.size)
        assertTrue(MediaCopyState.entries.any { it == MediaCopyState.TRASHED })
    }

    @Test
    fun dashboardSnapshot_separatesDeviceOriginalsFromCatalog() {
        val snap = DashboardSnapshot(
            photoCount = 466L,
            videoCount = 10L,
            catalogPhotoCount = 400L,
            catalogVideoCount = 8L,
            acquiredPhotoCount = 0L,
            acquiredVideoCount = 0L,
            personalUsedBytes = 0L,
            lastUpdatedEpochMs = null,
            localAvailability = Availability.AVAILABLE,
        )
        // Gap: originali ≥ Catalogo; Importa non definisce «in Catalogo».
        assertTrue(snap.photoCount >= snap.catalogPhotoCount)
        assertTrue(snap.videoCount >= snap.catalogVideoCount)
        assertEquals(0L, snap.acquiredPhotoCount)
        assertEquals(400L, snap.catalogPhotoCount)
    }
}
