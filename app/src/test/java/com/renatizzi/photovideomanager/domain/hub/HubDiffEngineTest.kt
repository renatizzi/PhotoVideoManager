package com.renatizzi.photovideomanager.domain.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HubDiffEngineTest {

    private fun card(
        id: String,
        at: Long,
        by: String,
        title: String = id,
    ) = HubCard(
        id = id,
        kind = "PHOTO",
        displayTitle = title,
        updatedAtEpochMs = at,
        updatedByDeviceId = by,
    )

    @Test
    fun publishWhenOnlyLocal() {
        val preview = HubDiffEngine.preview(
            localCards = listOf(card("a", 10, "phone")),
            remoteCards = emptyList(),
            lastSynced = emptyMap(),
        )
        assertEquals(1, preview.toPublish.size)
        assertTrue(preview.toReceive.isEmpty())
        assertTrue(preview.conflicts.isEmpty())
    }

    @Test
    fun receiveWhenOnlyRemote() {
        val preview = HubDiffEngine.preview(
            localCards = emptyList(),
            remoteCards = listOf(card("b", 20, "tablet")),
            lastSynced = emptyMap(),
        )
        assertEquals(1, preview.toReceive.size)
    }

    @Test
    fun conflictWhenBothChangedSinceSync() {
        val synced = mapOf("c" to HubDiffEngine.SyncStamp(5, "phone"))
        val preview = HubDiffEngine.preview(
            localCards = listOf(card("c", 15, "phone")),
            remoteCards = listOf(card("c", 16, "tablet")),
            lastSynced = synced,
        )
        assertEquals(1, preview.conflicts.size)
    }

    @Test
    fun applyResolutionsMovesConflicts() {
        val conflict = HubCardChange(
            cardId = "c",
            title = "c",
            kind = HubCardChangeKind.CONFLICT,
            local = card("c", 15, "phone"),
            remote = card("c", 16, "tablet"),
        )
        val preview = HubDiffPreview(emptyList(), emptyList(), listOf(conflict))
        val resolved = HubDiffEngine.applyResolutions(
            preview,
            mapOf("c" to ConflictResolution.KEEP_LOCAL),
        )
        assertEquals(1, resolved.toPublish.size)
        assertTrue(resolved.conflicts.isEmpty())
    }
}
