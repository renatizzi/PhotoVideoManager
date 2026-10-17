package com.renatizzi.photovideomanager.data.storage

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Vincolo SAF: listing nested via DocumentsContract, non DocumentFile.fromSingleUri+listFiles.
 */
class SafListingContractTest {
    @Test
    fun saf_adapter_uses_documents_contract_for_children() {
        val relative = "src/main/java/com/renatizzi/photovideomanager/data/storage/SafTreeStorageAdapter.kt"
        val source = listOf(File(relative), File("app/$relative"), File("/workspace/app/$relative"))
            .firstOrNull { it.exists() }
        assertTrue("SafTreeStorageAdapter.kt non trovato", source != null)
        val text = source!!.readText()
        assertTrue(text.contains("buildChildDocumentsUriUsingTree"))
        assertTrue(text.contains("buildDocumentUriUsingTree"))
        assertTrue(text.contains("COLUMN_DOCUMENT_ID"))
        assertTrue(text.contains("SingleDocumentFile") || text.contains("UnsupportedOperationException"))
    }
}
