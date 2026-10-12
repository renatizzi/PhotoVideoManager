package com.renatizzi.photovideomanager.ui.acquire

import com.renatizzi.photovideomanager.domain.model.AcquireCandidate
import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import org.junit.Assert.assertEquals
import org.junit.Test

/** BL-04: deselezionare non deve svuotare l’elenco Importa. */
class AcquireListedCandidatesTest {
    @Test
    fun listedCandidates_keepsDeselectedRows() {
        val a = candidate("m1")
        val b = candidate("m2")
        val state = AcquireUiState(
            candidates = listOf(a, b),
            selectedIds = setOf("m1"),
        )
        assertEquals(2, state.listedCandidates.size)
        assertEquals(setOf("m1"), state.selectedIds)
    }

    private fun candidate(id: String): AcquireCandidate {
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
            alreadyInPersonalArchive = false,
        )
    }
}
