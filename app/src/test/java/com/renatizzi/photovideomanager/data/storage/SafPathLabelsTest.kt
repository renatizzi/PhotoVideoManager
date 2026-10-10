package com.renatizzi.photovideomanager.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
        val path = SafPathLabels.humanizeDocumentId("primary:")
        val relative = path.substringAfter('/', missingDelimiterValue = "")
        val title = if (relative.isBlank()) SafPathLabels.ROOT_FOLDER_TITLE
        else relative.substringAfterLast('/')
        assertEquals(SafPathLabels.ROOT_FOLDER_TITLE, title)
    }

    @Test
    fun providerOfAuthority_drive() {
        assertEquals(
            SafPathLabels.SafProvider.GOOGLE_DRIVE,
            SafPathLabels.providerOfAuthority("com.google.android.apps.docs.storage"),
        )
    }

    @Test
    fun providerOfAuthority_localExternalStorage() {
        assertEquals(
            SafPathLabels.SafProvider.LOCAL_STORAGE,
            SafPathLabels.providerOfAuthority("com.android.externalstorage.documents"),
        )
    }

    @Test
    fun humanizeDocumentId_opaqueWithoutColon_notMemoriaPrincipale() {
        val label = SafPathLabels.humanizeDocumentId("opaqueDriveDocId123")
        assertFalse(label.contains("Memoria principale"))
        assertNotEquals(SafPathLabels.ROOT_FOLDER_TITLE, label)
    }
}
