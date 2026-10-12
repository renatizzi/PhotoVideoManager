package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.domain.policy.ExactDedupGrouping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactDedupGroupingTest {
    @Test
    fun sameItemSourceAndPersonal_notAGroup() {
        val item = item("m1", MediaKind.PHOTO, 1L)
        val copies = listOf(
            copy("c1", "m1", "loc.source"),
            copy("c2", "m1", "loc.personal"),
        )
        val fps = listOf(
            fp("c1", "abc"),
            fp("c2", "abc"),
        )
        val groups = ExactDedupGrouping.buildGroups(
            itemsById = mapOf("m1" to item),
            copies = copies,
            shaFingerprints = fps,
            locationNames = mapOf(
                "loc.source" to "Cartella",
                "loc.personal" to "App",
            ),
        )
        assertTrue(groups.isEmpty())
    }

    @Test
    fun twoDistinctItemsSameHash_formGroup() {
        val items = mapOf(
            "m1" to item("m1", MediaKind.PHOTO, 10L),
            "m2" to item("m2", MediaKind.PHOTO, 20L),
        )
        val copies = listOf(
            copy("c1", "m1", "loc.a"),
            copy("c2", "m2", "loc.b"),
        )
        val fps = listOf(fp("c1", "deadbeef"), fp("c2", "deadbeef"))
        val groups = ExactDedupGrouping.buildGroups(
            itemsById = items,
            copies = copies,
            shaFingerprints = fps,
            locationNames = mapOf("loc.a" to "A", "loc.b" to "B"),
        )
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].distinctItemCount)
        assertEquals(1, groups[0].extraItemCount)
        assertTrue(groups[0].members.any { it.isSuggestedKeep && it.mediaItem.id == "m1" })
        val (photos, videos) = ExactDedupGrouping.extraCountsByKind(groups)
        assertEquals(1L, photos)
        assertEquals(0L, videos)
    }

    private fun item(id: String, kind: MediaKind, created: Long) = MediaItem(
        id = id,
        kind = kind,
        domainScope = DomainScope.PERSONAL,
        displayTitle = id,
        createdAtEpochMs = created,
        updatedAtEpochMs = created,
    )

    private fun copy(id: String, itemId: String, locationId: String) = MediaCopy(
        id = id,
        mediaItemId = itemId,
        storageLocationId = locationId,
        opaqueLocator = id,
        state = MediaCopyState.ACTIVE,
        createdAtEpochMs = 1L,
    )

    private fun fp(copyId: String, value: String) = MediaFingerprint(
        id = "fp.$copyId",
        mediaCopyId = copyId,
        algorithm = ExactDedupGrouping.SHA256,
        level = ExactDedupGrouping.LEVEL,
        value = value,
        computedAtEpochMs = 1L,
    )
}
