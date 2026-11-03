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

    @Test
    fun parseDocumentId_readsDocumentSegment() {
        val uri =
            "content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures%2Ffamiliari"
        assertEquals("primary:Pictures/familiari", SafTreeIdentity.parseDocumentId(uri))
    }

    @Test
    fun isSecondaryVolume_detectsNonPrimary() {
        assertFalse(
            SafTreeIdentity.isSecondaryVolume(
                "content://com.android.externalstorage.documents/tree/primary%3APictures",
            ),
        )
        assertTrue(
            SafTreeIdentity.isSecondaryVolume(
                "content://com.android.externalstorage.documents/tree/ABCD-EF01%3ADCIM",
            ),
        )
    }

    @Test
    fun isMobileSecondaryVolume_onlyOnLocalStorage() {
        assertTrue(
            SafTreeIdentity.isMobileSecondaryVolume(
                "content://com.android.externalstorage.documents/tree/ABCD-EF01%3ADCIM",
            ),
        )
        assertFalse(
            SafTreeIdentity.isMobileSecondaryVolume(
                "content://com.google.android.apps.docs.storage/tree/root",
            ),
        )
        assertFalse(
            SafTreeIdentity.isMobileSecondaryVolume(
                "content://com.android.externalstorage.documents/tree/primary%3APictures",
            ),
        )
    }

    @Test
    fun childDocument_andSubtreeTree_shareCanonicalKey() {
        val doc =
            "content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures%2Fvacanze"
        val tree =
            "content://com.android.externalstorage.documents/tree/primary%3APictures%2Fvacanze"
        assertEquals(
            SafTreeIdentity.canonicalKeyFromUriString(doc),
            SafTreeIdentity.canonicalKeyFromUriString(tree),
        )
    }

    @Test
    fun documentUnderTree_parentAndChildIds() {
        val doc =
            "content://com.android.externalstorage.documents/tree/primary%3APictures/document/primary%3APictures%2Fvacanze"
        // Adapter usa treeId genitore; censimento parte da documentId figlio.
        assertEquals("primary:Pictures", SafTreeIdentity.parseTreeDocumentId(doc))
        assertEquals("primary:Pictures/vacanze", SafTreeIdentity.parseDocumentId(doc))
        assertEquals(
            "saf:com.android.externalstorage.documents|primary:Pictures/vacanze",
            SafTreeIdentity.canonicalKeyFromUriString(doc),
        )
    }

    @Test
    fun sameDocumentFolder_treeAndDocumentForms() {
        val doc =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1/document/primary%3ATest1%2FDuplicati"
        val tree =
            "content://com.android.externalstorage.documents/tree/primary%3ATest1%2FDuplicati"
        assertTrue(SafTreeIdentity.sameDocumentFolder(doc, tree))
        assertTrue(SafTreeIdentity.isRegisteredFolderRoot(doc, tree))
    }

    @Test
    fun isSameOrDescendantDocId_nestedPaths() {
        assertTrue(
            SafTreeIdentity.isSameOrDescendantDocId(
                "primary:Test1/Duplicati/file.mp4",
                "primary:Test1/Duplicati",
            ),
        )
        assertTrue(
            SafTreeIdentity.isSameOrDescendantDocId("primary:Test1/Duplicati", "primary:Test1"),
        )
        assertFalse(
            SafTreeIdentity.isSameOrDescendantDocId("primary:Test2", "primary:Test1"),
        )
    }
}
