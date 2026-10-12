package com.renatizzi.photovideomanager.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafTreeIdentityTest {
    @Test
    fun sameTree_ignoresEncodingDifferences() {
        val a = "content://com.android.externalstorage.documents/tree/primary%3AEraser"
        val b = "content://com.android.externalstorage.documents/tree/primary:Eraser"
        assertTrue(SafTreeIdentity.sameTree(a, b))
        assertEquals(
            SafTreeIdentity.canonicalKeyFromUriString(a),
            SafTreeIdentity.canonicalKeyFromUriString(b),
        )
    }

    @Test
    fun differentFolders_areNotSameTree() {
        val eraser = "content://com.android.externalstorage.documents/tree/primary%3AEraser"
        val facebook = "content://com.android.externalstorage.documents/tree/primary%3AFacebook"
        assertFalse(SafTreeIdentity.sameTree(eraser, facebook))
    }

    @Test
    fun parseTreeDocumentId_readsEncodedSegment() {
        val uri = "content://com.android.externalstorage.documents/tree/primary%3ADCIM/document/primary%3ADCIM"
        assertEquals("primary:DCIM", SafTreeIdentity.parseTreeDocumentId(uri))
    }
}
