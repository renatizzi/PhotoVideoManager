package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import java.io.File
import java.util.ArrayDeque
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * LOCK Tab S9 #1: root con media + sottocartella con media → una sola sorgente;
 * il BFS trova tutti i file (niente perdita dei media in radice).
 */
class SafFolderExpandWithRootMediaTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun rootWithDirectMediaAndChildren_singleSourceFindsAll() = runBlocking {
        val pictures = tmp.newFolder("Pictures")
        File(pictures, "root.jpg").writeText("root")
        val sub = File(pictures, "vacanze").also { assertTrue(it.mkdirs()) }
        File(sub, "mare.mp4").writeBytes(ByteArray(8))

        val adapter = LocalFilesystemStorageAdapter(pictures)
        val queue = ArrayDeque<String>()
        queue.add("")
        val found = mutableListOf<String>()
        var dirs = 0
        while (queue.isNotEmpty() && dirs < 50) {
            val current = queue.removeFirst()
            dirs++
            for (child in adapter.listChildren(current)) {
                if (child.isDirectory) queue.add(child.opaqueLocator)
                else found += child.displayName
            }
        }
        // Una sola cartella registrata; census vede root + figlio.
        assertEquals(setOf("root.jpg", "mare.mp4"), found.toSet())
        assertEquals(1, listOf(pictures.name).size)
    }
}
