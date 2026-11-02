package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.model.MediaKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaKindDetectorTest {
    @Test
    fun mp4_byExtension() {
        assertEquals(MediaKind.VIDEO, MediaKindDetector.detect("mare.mp4", null))
        assertEquals(MediaKind.VIDEO, MediaKindDetector.detect("Mare.MP4", "application/octet-stream"))
    }

    @Test
    fun mp4_byAtypicalMime() {
        assertEquals(MediaKind.VIDEO, MediaKindDetector.detect("clip", "application/mp4"))
        assertEquals(MediaKind.VIDEO, MediaKindDetector.detect("clip", "video/mp4; charset=utf-8"))
    }

    @Test
    fun mp4_fromLocatorWhenDisplayNameHasNoExt() {
        assertEquals(
            MediaKind.VIDEO,
            MediaKindDetector.detect(
                fileName = "video",
                mimeType = null,
                locatorHint = "content://tree/primary%3ADCIM/document/primary%3ADCIM%2Fmare.mp4",
            ),
        )
    }

    @Test
    fun photo_byExtension() {
        assertEquals(MediaKind.PHOTO, MediaKindDetector.detect("nonna.jpg", null))
    }

    @Test
    fun unknown_returnsNull() {
        assertNull(MediaKindDetector.detect("report.pdf", "application/pdf"))
    }
}
