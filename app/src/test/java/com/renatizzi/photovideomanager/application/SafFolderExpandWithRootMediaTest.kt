package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Nota ter: se la cartella scelta ha media nella radice E sottocartelle con media,
 * vanno censiti entrambi (prima si perdevano i file in radice).
 */
class SafFolderExpandWithRootMediaTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun rootWithDirectMediaAndChildren_keepsRootAndChildren() = runBlocking {
        val pictures = tmp.newFolder("Pictures")
        File(pictures, "root.jpg").writeText("root")
        val sub = File(pictures, "vacanze").also { assertTrue(it.mkdirs()) }
        File(sub, "mare.mp4").writeBytes(ByteArray(8))

        val adapter = LocalFilesystemStorageAdapter(pictures)
        val children = adapter.listChildren("").filter { it.isDirectory }
        val mediaChildren = children.filter { child ->
            adapter.listChildren(child.opaqueLocator).any { !it.isDirectory }
        }
        val rootHasDirect = adapter.listChildren("").any { !it.isDirectory }

        assertTrue(rootHasDirect)
        assertEquals(1, mediaChildren.size)
        // Contratto atteso dal fix SourceRegistry: root + children.
        val targets = buildList {
            if (rootHasDirect || mediaChildren.isEmpty()) add("Pictures")
            addAll(mediaChildren.map { it.displayName })
        }
        // Con root media + children → entrambi.
        assertEquals(listOf("Pictures", "vacanze"), targets)
    }
}
