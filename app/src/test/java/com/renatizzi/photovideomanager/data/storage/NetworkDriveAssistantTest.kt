package com.renatizzi.photovideomanager.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkDriveAssistantTest {
    @Test
    fun helperPackage_matchesBoxManager() {
        assertEquals(
            "com.wa2c.android.cifsdocumentsprovider",
            NetworkDriveAssistant.HELPER_PACKAGE,
        )
        assertTrue(NetworkDriveAssistant.HELPER_PLAY_URI.contains(NetworkDriveAssistant.HELPER_PACKAGE))
    }
}
