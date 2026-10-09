package com.renatizzi.photovideomanager.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SafPathLabelsTest {
    @Test
    fun humanizeDocumentId_primaryPictures() {
        assertEquals(
            "Memoria principale/Pictures",
            SafPathLabels.humanizeDocumentId("primary:Pictures"),
        )
    }

    @Test
    fun humanizeDocumentId_decodesUrlPath() {
        assertEquals(
            "Memoria principale/Movies/Revive",
            SafPathLabels.humanizeDocumentId("primary%3AMovies%2FRevive"),
        )
    }

    @Test
    fun volumeLabel_primary() {
        assertEquals("Memoria principale", SafPathLabels.volumeLabel("primary"))
    }

    @Test
    fun humanize_doesNotLookLikeEncodedToken() {
        val label = SafPathLabels.humanizeDocumentId("primary:DCIM/Camera")
        assertFalse(label.contains("encoded=", ignoreCase = true))
        assertFalse(label.startsWith("acc="))
    }
}
