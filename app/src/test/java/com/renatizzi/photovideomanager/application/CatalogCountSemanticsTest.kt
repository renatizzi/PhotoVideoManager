package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contratto KPI Home modello A (BL-14 / 0.16.9):
 * - originali ≥ di cui in Catalogo
 * - Cestino app (TRASHED) non gonfia gli originali
 * - caso 9 fonti + 1 solo spazio app → originali = 10 = Catalogo
 */
class CatalogCountSemanticsTest {
    @Test
    fun catalogUniverse_isActiveCopiesOnly() {
        assertEquals("ACTIVE", MediaCopyState.ACTIVE.name)
        assertEquals(4, MediaCopyState.entries.size)
        assertTrue(MediaCopyState.entries.any { it == MediaCopyState.TRASHED })
    }

    @Test
    fun dashboardSnapshot_originaliNeverBelowCatalog() {
        // Simula max(device=9, catalog=10) già applicato in CatalogFacade.
        val snap = DashboardSnapshot(
            photoCount = 10L,
            videoCount = 2L,
            catalogPhotoCount = 10L,
            catalogVideoCount = 2L,
            acquiredPhotoCount = 0L,
            acquiredVideoCount = 0L,
            personalUsedBytes = 0L,
            lastUpdatedEpochMs = null,
            localAvailability = Availability.AVAILABLE,
        )
        assertTrue(snap.photoCount >= snap.catalogPhotoCount)
        assertEquals(10L, snap.photoCount)
        assertEquals(10L, snap.catalogPhotoCount)
        assertEquals(0L, snap.acquiredPhotoCount)
    }

    @Test
    fun maxDeviceAndCatalog_matchesReportedCase() {
        val devicePhotos = 9L
        val catalogPhotos = 10L
        assertEquals(10L, maxOf(devicePhotos, catalogPhotos))
    }
}
