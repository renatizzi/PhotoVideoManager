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
}
