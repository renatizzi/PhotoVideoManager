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

    @Test
    fun folderTitle_rootVolumeUsesExplicitName() {
        // humanPath("primary:") → "Memoria principale" → titolo radice
        assertEquals(
            SafPathLabels.ROOT_FOLDER_TITLE,
            SafPathLabels.humanizeDocumentId("primary:").let { path ->
                val relative = path.substringAfter('/', missingDelimiterValue = "")
                if (relative.isBlank()) SafPathLabels.ROOT_FOLDER_TITLE
                else relative.substringAfterLast('/')
            },
        )
    }
}
