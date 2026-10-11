package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.model.TrashEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contratto UI Cestino: una riga per elemento di Catalogo, anche se ci sono
 * più copie fisiche (file del dispositivo + copia gestita dall’app).
 */
class TrashGroupingTest {
    @Test
    fun groupsTrashedCopies_byMediaItem() {
        val item = MediaItem(
            id = "media.1",
            kind = MediaKind.PHOTO,
            domainScope = DomainScope.PERSONAL,
            displayTitle = "foto.jpg",
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
        )
        val device = MediaCopy(
            id = "copy.device",
            mediaItemId = item.id,
            storageLocationId = "location.saf.eraser",
            opaqueLocator = "content://eraser/foto.jpg",
            state = MediaCopyState.TRASHED,
            createdAtEpochMs = 2L,
        )
        val personal = MediaCopy(
            id = "copy.personal",
            mediaItemId = item.id,
            storageLocationId = "location.personal.local.root",
            opaqueLocator = "foto.jpg",
            state = MediaCopyState.TRASHED,
            createdAtEpochMs = 3L,
        )
        val grouped = listOf(device, personal)
            .groupBy { it.mediaItemId }
            .map { (mediaItemId, copies) ->
                assertEquals(item.id, mediaItemId)
                val display = copies.firstOrNull {
                    it.storageLocationId != "location.personal.local.root"
                } ?: copies.first()
                TrashEntry(
                    mediaItem = item,
                    mediaCopy = display,
                    locationName = "Eraser",
                    isPersonalArchive = copies.any {
                        it.storageLocationId == "location.personal.local.root"
                    },
                    trashedCopyIds = copies.map { it.id },
                )
            }

        assertEquals(1, grouped.size)
        assertEquals("copy.device", grouped[0].mediaCopy.id)
        assertTrue(grouped[0].isPersonalArchive)
        assertEquals(2, grouped[0].trashedCopyIds.size)
        assertFalse(grouped[0].trashedCopyIds.isEmpty())
    }
}
