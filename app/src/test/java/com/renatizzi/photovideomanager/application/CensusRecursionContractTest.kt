package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.LocalFilesystemStorageAdapter
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import java.io.File
import java.util.ArrayDeque
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.Rule

/**
 * Contratto acquisizione: la cartella scelta include le sottocartelle
 * (es. Pictures → familiari, vacanze), come in [CensusService] (BFS).
 */
class CensusRecursionContractTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun bfs_includesMediaInNestedFolders_likePicturesLayout() = runBlocking {
        val pictures = tmp.newFolder("Pictures")
        val familiari = File(pictures, "familiari").also { assertTrue(it.mkdirs()) }
        val vacanze = File(pictures, "vacanze").also { assertTrue(it.mkdirs()) }
        File(familiari, "nonna.jpg").writeText("photo")
        File(vacanze, "mare.mp4").writeBytes(ByteArray(8))
        File(pictures, "readme.txt").writeText("ignore")

        val adapter = LocalFilesystemStorageAdapter(pictures)
        val queue = ArrayDeque<String>()
        queue.add("")
        var dirsVisited = 0
        val mediaNames = mutableListOf<String>()
        while (queue.isNotEmpty() && dirsVisited < CensusService.DEFAULT_MAX_DIRS) {
            val current = queue.removeFirst()
            dirsVisited++
            val children: List<StorageEntry> = adapter.listChildren(current)
            for (child in children) {
                if (child.isDirectory) {
                    queue.add(child.opaqueLocator)
                } else {
                    val ext = child.displayName.substringAfterLast('.', "").lowercase()
                    if (ext in setOf("jpg", "jpeg", "png", "mp4", "mov")) {
                        mediaNames.add(child.displayName)
                    }
                }
            }
        }
        assertEquals(setOf("nonna.jpg", "mare.mp4"), mediaNames.toSet())
        assertTrue("deve visitare Pictures + 2 sottocartelle", dirsVisited >= 3)
    }
}
