package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashPolicyTest {
    @Test
    fun mediaCopyState_includesTrashed() {
        assertTrue(MediaCopyState.entries.contains(MediaCopyState.TRASHED))
        assertTrue(MediaCopyState.entries.contains(MediaCopyState.ACTIVE))
        assertEquals(4, MediaCopyState.entries.size)
    }
}
