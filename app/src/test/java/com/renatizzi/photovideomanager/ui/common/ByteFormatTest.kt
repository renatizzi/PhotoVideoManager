package com.renatizzi.photovideomanager.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteFormatTest {
    @Test
    fun formatBytes_scalesUnits() {
        assertEquals("512 B", formatBytes(512))
        assertEquals("1.0 KB", formatBytes(1024))
        assertEquals("1.5 MB", formatBytes((1.5 * 1024 * 1024).toLong()))
    }
}
