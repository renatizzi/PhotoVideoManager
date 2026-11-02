package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import java.io.File
import java.util.ArrayDeque
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * NOVITÀ Nota 0.16.57: se si sceglie una root (es. Pictures) con sottocartelle
 * che contengono media, ogni sottocartella con media diventa una riga elenco
 * (corrispondenza biunivoca), non la sola root.
 */
class SafFolderExpandContractTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun expand_listsOnlyImmediateChildrenWithMedia() = runBlocking {
        val pictures = tmp.newFolder("Pictures")
        val familiari = File(pictures, "familiari").also { assertTrue(it.mkdirs()) }
        val vacanze = File(pictures, "vacanze").also { assertTrue(it.mkdirs()) }
        val empty = File(pictures, "vuota").also { assertTrue(it.mkdirs()) }
        File(familiari, "nonna.jpg").writeText("photo")
        File(vacanze, "mare.mp4").writeBytes(ByteArray(8))
        File(empty, "readme.txt").writeText("ignore")

        val adapter = LocalFilesystemStorageAdapter(pictures)
        val children = adapter.listChildren("").filter { it.isDirectory }
        val mediaChildren = children.filter { child ->
            hasMediaInSubtree(adapter, child.opaqueLocator)
        }.map { it.displayName }.sorted()

        assertEquals(listOf("familiari", "vacanze"), mediaChildren)
    }

    @Test
    fun leafFolder_registersItself() = runBlocking {
        val leaf = tmp.newFolder("solo")
        File(leaf, "a.jpg").writeText("x")
        val adapter = LocalFilesystemStorageAdapter(leaf)
        val children = adapter.listChildren("").filter { it.isDirectory }
        val mediaChildren = children.filter { hasMediaInSubtree(adapter, it.opaqueLocator) }
        assertTrue(mediaChildren.isEmpty())
        assertTrue(hasMediaInSubtree(adapter, ""))
    }

    private suspend fun hasMediaInSubtree(
        adapter: LocalFilesystemStorageAdapter,
        start: String,
    ): Boolean {
        val queue = ArrayDeque<String>()
        queue.add(start)
        var dirs = 0
        while (queue.isNotEmpty() && dirs < 200) {
            val current = queue.removeFirst()
            dirs++
            for (child: StorageEntry in adapter.listChildren(current)) {
                if (child.isDirectory) {
                    queue.add(child.opaqueLocator)
                } else {
                    val ext = child.displayName.substringAfterLast('.', "").lowercase()
                    if (ext in setOf("jpg", "jpeg", "png", "mp4", "mov")) return true
                }
            }
        }
        return false
    }
}
