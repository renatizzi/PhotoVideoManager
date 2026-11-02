package com.renatizzi.photovideomanager.ui.clean

import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.DuplicateMember
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind
import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicateAgeFilterTest {
    @Test
    fun olderFilter_selectsOlderKeepsNewest() {
        val groups = listOf(
            DuplicateGroup(
                fingerprintValue = "abc",
                algorithm = "SHA-256",
                members = listOf(
                    member("old", created = 1000L),
                    member("new", created = 2000L),
                ),
            ),
        )
        val selected = CleanViewModel.selectByAgeFilter(groups, DuplicateAgeFilter.OLDER)
        assertEquals(setOf("old"), selected)
    }

    @Test
    fun newerFilter_selectsNewerKeepsOldest() {
        val groups = listOf(
            DuplicateGroup(
                fingerprintValue = "abc",
                algorithm = "SHA-256",
                members = listOf(
                    member("old", created = 1000L),
                    member("new", created = 2000L),
                ),
            ),
        )
        val selected = CleanViewModel.selectByAgeFilter(groups, DuplicateAgeFilter.NEWER)
        assertEquals(setOf("new"), selected)
    }

    private fun member(id: String, created: Long) = DuplicateMember(
        mediaItem = MediaItem(
            id = id,
            kind = MediaKind.PHOTO,
            domainScope = DomainScope.PERSONAL,
            createdAtEpochMs = created,
            updatedAtEpochMs = created,
        ),
        mediaCopy = MediaCopy(
            id = "copy.$id",
            mediaItemId = id,
            storageLocationId = "loc",
            opaqueLocator = id,
            createdAtEpochMs = created,
        ),
        locationName = "Loc",
        isSuggestedKeep = false,
    )
}
