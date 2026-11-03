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
 * LOCK Tab S9 #1–#3: una cartella scelta → census ricorsivo include le sottocartelle
 * (non espandere in più righe Acquisisci).
 */
class SafFolderExpandContractTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun censusRecursesIntoChildren_singleChosenFolder() = runBlocking {
        val test1 = tmp.newFolder("Test1")
        val duplicati = File(test1, "Duplicati").also { assertTrue(it.mkdirs()) }
        File(test1, "a.mp4").writeBytes(ByteArray(4))
        File(test1, "b.mp4").writeBytes(ByteArray(4))
        File(test1, "c.mp4").writeBytes(ByteArray(4))
        File(duplicati, "d.jpg").writeText("p")
        File(duplicati, "e.mp4").writeBytes(ByteArray(4))
        File(duplicati, "f.mp4").writeBytes(ByteArray(4))

        val adapter = LocalFilesystemStorageAdapter(test1)
        val media = bfsMediaNames(adapter)
        assertEquals(6, media.size)
        assertTrue(media.containsAll(listOf("a.mp4", "b.mp4", "c.mp4", "d.jpg", "e.mp4", "f.mp4")))
        // Una sola «riga» scelta = root Test1 (nessuna espansione figli).
        assertEquals(listOf("Test1"), listOf(test1.name))
    }

    @Test
    fun leafFolder_stillHasOwnMedia() = runBlocking {
        val leaf = tmp.newFolder("solo")
        File(leaf, "a.jpg").writeText("x")
        val adapter = LocalFilesystemStorageAdapter(leaf)
        assertEquals(listOf("a.jpg"), bfsMediaNames(adapter))
    }

    private suspend fun bfsMediaNames(adapter: LocalFilesystemStorageAdapter): List<String> {
        val queue = ArrayDeque<String>()
        queue.add("")
        val names = mutableListOf<String>()
        var dirs = 0
        while (queue.isNotEmpty() && dirs < 200) {
            val current = queue.removeFirst()
            dirs++
            for (child: StorageEntry in adapter.listChildren(current)) {
                if (child.isDirectory) {
                    queue.add(child.opaqueLocator)
                } else {
                    val ext = child.displayName.substringAfterLast('.', "").lowercase()
                    if (ext in setOf("jpg", "jpeg", "png", "mp4", "mov")) {
                        names += child.displayName
                    }
                }
            }
        }
        return names
    }
}
