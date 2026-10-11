package com.renatizzi.photovideomanager.ui.acquire

import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcquireSelectionLogicTest {
    @Test
    fun acquire_onlySelectedNotAlreadyInCatalog() {
        val a = candidate("a", already = false)
        val b = candidate("b", already = false)
        val c = candidate("c", already = true)
        val selected = setOf("a", "c", "ghost")
        assertEquals(
            setOf("a"),
            AcquireSelectionLogic.idsToAcquire(listOf(a, b, c), selected),
        )
    }

    @Test
    fun discard_deselectedNotAlreadyInCatalog() {
        val a = candidate("a", already = false)
        val b = candidate("b", already = false)
        val c = candidate("c", already = true)
        val selected = setOf("a")
        assertEquals(
            listOf("b"),
            AcquireSelectionLogic.idsToDiscard(listOf(a, b, c), selected),
        )
        assertTrue(
            AcquireSelectionLogic.idsToDiscard(listOf(a, b, c), selected)
                .none { it == "c" },
        )
    }

    private fun candidate(id: String, already: Boolean): AcquireCandidate {
        val item = MediaItem(
            id = id,
            kind = MediaKind.PHOTO,
            domainScope = DomainScope.PERSONAL,
            createdAtEpochMs = 0L,
            updatedAtEpochMs = 0L,
        )
        val copy = MediaCopy(
            id = "c.$id",
            mediaItemId = id,
            storageLocationId = "location.saf.demo",
            opaqueLocator = "doc/$id",
            createdAtEpochMs = 0L,
        )
        return AcquireCandidate(
            mediaItem = item,
            sourceCopy = copy,
            sourceLocationName = "Demo",
            alreadyInPersonalArchive = already,
        )
    }
}
