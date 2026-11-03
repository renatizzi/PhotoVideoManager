package com.renatizzi.photovideomanager.application

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderPathLabelForFileTest {
    @Test
    fun fileInSourceRoot_usesSourceName() {
        val source =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1"
        val file =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1/document/primary%3ATest1%2Fa.mp4"
        assertEquals(
            "Test1",
            folderPathLabelForFile(file, source, "Test1"),
        )
    }

    @Test
    fun fileInSubfolder_includesRelativePath() {
        val source =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1"
        val file =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1/document/primary%3ATest1%2FDuplicati%2Fd.jpg"
        assertEquals(
            "Test1/Duplicati",
            folderPathLabelForFile(file, source, "Test1"),
        )
    }

    @Test
    fun driveEncodedDoc_fallsBackToSourceDisplayName() {
        val source =
            "content://com.google.android.apps.docs.storage/tree/test2_drive"
        val file =
            "content://com.google.android.apps.docs.storage/tree/test2_drive/document/acc%3D1%3Bdoc%3Dencoded%3DXhl5mDpBTtRf3NRbE6VtBg"
        assertEquals(
            "test2_drive",
            folderPathLabelForFile(file, source, "test2_drive"),
        )
    }

    @Test
    fun sanitize_stripsHostDuplicatedUnderFolder() {
        val uri =
            "content://com.wa2c.android.cifsdocumentsprovider.documents/tree/smb%3A%2F%2Ftest3_SDA1%2FMediaManager(sda1)"
        assertEquals(
            "MediaManager(sda1)",
            sanitizeRelativeFolderPath(
                "MediaManager(sda1)/test3_SDA1",
                "MediaManager(sda1)",
                uri,
            ),
        )
    }
}
