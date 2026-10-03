package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.SearchKindFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterTest {
    @Test
    fun searchKindFilter_coversAllPhotoVideo() {
        assertEquals(3, SearchKindFilter.entries.size)
        assertTrue(SearchKindFilter.entries.contains(SearchKindFilter.ALL))
        assertTrue(SearchKindFilter.entries.contains(SearchKindFilter.PHOTO))
        assertTrue(SearchKindFilter.entries.contains(SearchKindFilter.VIDEO))
    }
}
