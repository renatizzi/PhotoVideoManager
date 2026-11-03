package com.renatizzi.photovideomanager.application

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Specifica il decoding usato per i titoli Esclusi (anti %3A/%2F).
 */
class HumanFileNameFromLocatorTest {
    @Test
    fun decodesSafDocumentLeaf() {
        val locator =
            "content://com.android.externalstorage.documents/tree/primary%3APictures/" +
                "document/primary%3APictures%2Ffamiliari%2Fnonna.jpg"
        val raw = locator.substringAfterLast('/').substringAfterLast(':')
        val decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
        val leaf = decoded.substringAfterLast('/').substringAfterLast('\\')
        assertEquals("nonna.jpg", leaf)
    }

    @Test
    fun decodesDriveStyleTokenLeaf() {
        val raw = "6131-3061%3ATest2%2FMovies%2Fclip.mp4"
        val decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
        assertEquals("clip.mp4", decoded.substringAfterLast('/'))
    }
}
