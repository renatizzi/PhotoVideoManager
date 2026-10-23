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
    fun providerOfAuthority_smbIsNetworkNas_notCloud() {
        assertEquals(
            SafPathLabels.SafProvider.NETWORK_NAS,
            SafPathLabels.providerOfAuthority("com.google.android.sambadocumentsprovider"),
        )
        assertEquals(
            SafPathLabels.SafProvider.NETWORK_NAS,
            SafPathLabels.providerOfAuthority("com.synology.dsfile.documents"),
        )
        assertEquals(
            SafPathLabels.SafProvider.OTHER,
            SafPathLabels.providerOfAuthority("com.example.unknown.cloud"),
        )
        assertEquals("Disco di rete / NAS", SafPathLabels.DEVICE_NETWORK_NAS)
        assertEquals("PC / notebook", SafPathLabels.DEVICE_PC_NOTEBOOK)
    }

    @Test
    fun networkDeviceLabel_mediaManagerShareIsPc() {
        assertEquals(
            SafPathLabels.DEVICE_PC_NOTEBOOK,
            SafPathLabels.networkDeviceLabelFromHints(
                displayHint = "MediaManagerCatalog",
                path = "",
                uriString = "content://com.wa2c.android.cifsdocumentsprovider.documents/tree/smb%3A%2F%2Fpc%2FMediaManagerCatalog",
            ),
        )
    }

    @Test
    fun networkDeviceLabel_synologyIsNas() {
        assertEquals(
            SafPathLabels.DEVICE_NETWORK_NAS,
            SafPathLabels.networkDeviceLabelFromHints(
                displayHint = "photos",
                path = "",
                uriString = "content://com.synology.dsfile.documents/tree/share",
            ),
        )
    }

    @Test
    fun networkDeviceLabel_genericShareDefaultsToPc() {
        assertEquals(
            SafPathLabels.DEVICE_PC_NOTEBOOK,
            SafPathLabels.networkDeviceLabelFromHints(
                displayHint = "Foto",
                path = "Foto",
                uriString = "content://com.wa2c.android.cifsdocumentsprovider.documents/tree/smb",
            ),
        )
    }

    @Test
    fun humanizeDocumentId_opaqueWithoutColon_notMemoriaPrincipale() {
        val label = SafPathLabels.humanizeDocumentId("opaqueDriveDocId123")
        assertFalse(label.contains("Memoria principale"))
        assertNotEquals(SafPathLabels.ROOT_FOLDER_TITLE, label)
    }

    @Test
    fun stripDevicePrefix_removesGoogleDriveDuplicate() {
        assertEquals(
            "test2_mediamanager",
            SafPathLabels.stripDevicePrefix("Google Drive/test2_mediamanager"),
        )
        assertEquals(
            "share/famiglia",
            SafPathLabels.stripDevicePrefix("Rete / NAS/share/famiglia"),
        )
        assertEquals(
            "Cartella Drive",
            SafPathLabels.stripDevicePrefix("Cartella Drive"),
        )
    }

    @Test
    fun pathUnderName_stripsDeviceAndLeafName() {
        assertEquals(
            "",
            SafPathLabels.pathUnderName("Google Drive/FilmoraGo", "FilmoraGo", "Google Drive"),
        )
        assertEquals(
            "Memoria principale/Pictures",
            SafPathLabels.pathUnderName(
                "Memoria principale/Pictures/MediaManager_test",
                "MediaManager_test",
                "Tab S9 di RENATO",
            ),
        )
        assertEquals(
            "",
            SafPathLabels.pathUnderName("Rete / NAS/MediaManager", "MediaManager", "Rete / NAS"),
        )
    }
}
