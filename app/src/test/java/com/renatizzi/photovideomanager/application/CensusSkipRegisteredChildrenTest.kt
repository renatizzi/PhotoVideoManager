package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.data.storage.SafTreeIdentity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contratto Tab S9: con root+figlio registrati, il BFS del root non scende nel figlio
 * (altrimenti 11+5=16). Stesso criterio di [CensusService] / anteprima live.
 */
class CensusSkipRegisteredChildrenTest {

    private val authority = "com.android.externalstorage.documents"

    private fun tree(docId: String): String {
        val enc = docId.replace(":", "%3A").replace("/", "%2F")
        return "content://$authority/tree/$enc"
    }

    private fun docUnder(parentTree: String, childDocId: String): String {
        val enc = childDocId.replace(":", "%3A").replace("/", "%2F")
        return "content://$authority/tree/${parentTree.replace(":", "%3A")}/document/$enc"
    }

    @Test
    fun bfs_skipsRegisteredChildFolder_countsOnlyRootMedia() {
        val rootLocator = tree("primary:Test1")
        val childRegistered = tree("primary:Test1/Duplicati")
        // Simula listing: root ha 3 media + cartella Duplicati; Duplicati ha 3 media.
        val rootDirs = listOf(docUnder("primary:Test1", "primary:Test1/Duplicati"))
        val rootFiles = listOf("a.mp4", "b.mp4", "c.jpg")
        val childFiles = listOf("d.jpg", "e.mp4", "f.mp4")

        val otherRegistered = listOf(childRegistered)
        val queue = ArrayDeque<String>()
        queue.add(rootLocator)
        val media = mutableListOf<String>()
        var dirs = 0
        while (queue.isNotEmpty() && dirs < 20) {
            val current = queue.removeFirst()
            dirs++
            val isRoot = SafTreeIdentity.sameDocumentFolder(current, rootLocator)
            if (isRoot) {
                for (dir in rootDirs) {
                    val claimed = otherRegistered.any {
                        SafTreeIdentity.isRegisteredFolderRoot(dir, it)
                    }
                    if (!claimed) queue.add(dir)
                }
                media += rootFiles
            } else {
                media += childFiles
            }
        }
        assertEquals(listOf("a.mp4", "b.mp4", "c.jpg"), media)
        assertEquals(1, dirs)
    }

    @Test
    fun childCensus_stillVisitsOwnFolder() {
        val childRegistered = tree("primary:Test1/Duplicati")
        val other = listOf(tree("primary:Test1"))
        // Entrare in Duplicati non è «claimed» rispetto al genitore (root ≠ child).
        assertFalse(
            other.any { SafTreeIdentity.isRegisteredFolderRoot(childRegistered, it) },
        )
        assertTrue(
            SafTreeIdentity.isSameOrDescendantDocId(
                "primary:Test1/Duplicati",
                "primary:Test1",
            ),
        )
    }
}
