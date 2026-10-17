package com.renatizzi.photovideomanager.domain.hub

import com.renatizzi.photovideomanager.domain.model.MediaCopyState
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogMultiDeviceTest {

    private fun card(
        id: String,
        at: Long = 10L,
        by: String = "phone",
        title: String = id,
        trashed: Boolean = false,
    ) = HubCard(
        id = id,
        kind = "PHOTO",
        displayTitle = title,
        updatedAtEpochMs = at,
        updatedByDeviceId = by,
        trashed = trashed,
    )

    private fun pkg(
        cards: List<HubCard>,
        catalogId: String? = null,
        sources: List<HubSourceDescriptor> = emptyList(),
    ) = HubPackage(
        seatId = "seat",
        updatedAtEpochMs = 1L,
        updatedByDeviceId = "phone",
        cards = cards,
        catalogId = catalogId,
        sources = sources,
    )

    private fun source(key: String, provider: String = "NETWORK_NAS") = HubSourceDescriptor(
        sourceKey = key,
        displayName = "Foto",
        provider = provider,
        humanPath = "Catalogo",
        documentId = "doc-$key",
        shareable = HubSourcePolicy.shareable(provider),
    )

    @Test
    fun legacyJsonDoesNotInventCatalogId() {
        val raw = """
            {"formatVersion":1,"seatId":"s","updatedAtEpochMs":1,"updatedByDeviceId":"phone",
            "cards":[{"id":"a","kind":"PHOTO","displayTitle":"A","updatedAtEpochMs":1,"updatedByDeviceId":"phone","trashed":false}]}
        """.trimIndent()
        val before = raw
        val parsed = HubPackageCodec.parse(raw)
        assertEquals(before, raw)
        assertNull(parsed.catalogId)
        assertTrue(parsed.sources.isEmpty())
        assertFalse(HubPackageCodec.toJson(parsed).has("catalogId"))
        assertFalse(HubPackageCodec.toJsonString(parsed).contains("catalogId"))
    }

    @Test
    fun assessDoesNotTreatOverlapAsProofOrWriteAnId() {
        val file = pkg(listOf(card("shared"), card("only-file")))
        val oneShared = CatalogIdentityRules.assess(file, adoptedCatalogId = null, localCardIds = setOf("shared", "only-local"))
        assertEquals(IdentityAction.NEEDS_CONFIRM_NEW_ID, oneShared.action)
        assertEquals(OverlapHint.PARTIAL, oneShared.hint)
        assertNull(file.catalogId)

        val full = CatalogIdentityRules.assess(
            pkg(listOf(card("a"), card("b"))),
            adoptedCatalogId = null,
            localCardIds = setOf("a", "b"),
        )
        assertEquals(IdentityAction.NEEDS_CONFIRM_NEW_ID, full.action)
        assertEquals(OverlapHint.FULL, full.hint)

        val none = CatalogIdentityRules.assess(
            pkg(listOf(card("file"))),
            adoptedCatalogId = "already",
            localCardIds = setOf("local"),
        )
        assertEquals(IdentityAction.NEEDS_CONFIRM_STAMP, none.action)
        assertEquals(OverlapHint.NONE, none.hint)
    }

    @Test
    fun differentCatalogIdsAreNotReplaced() {
        val file = pkg(listOf(card("a")), catalogId = "file-id")
        val assessment = CatalogIdentityRules.assess(file, adoptedCatalogId = "phone-id", localCardIds = setOf("a"))
        assertEquals(IdentityAction.BLOCK_DIFFERENT_IDS, assessment.action)
        val next = HubApplyPlan.nextPackage(
            original = file,
            resolved = HubDiffPreview(emptyList(), emptyList(), emptyList()),
            now = 99L,
            deviceId = "phone",
            sources = emptyList(),
            displayLabel = "altro",
            receiveOnly = false,
        )
        assertEquals("file-id", next.catalogId)
    }

    @Test
    fun updateLeavesMissingCatalogIdAbsent() {
        val original = pkg(listOf(card("a")))
        val next = HubApplyPlan.nextPackage(
            original = original,
            resolved = HubDiffPreview(
                toPublish = listOf(
                    HubCardChange("a", "a", HubCardChangeKind.PUBLISH, card("a", title = "Nuovo"), card("a")),
                ),
                toReceive = emptyList(),
                conflicts = emptyList(),
            ),
            now = 50L,
            deviceId = "phone",
            sources = emptyList(),
            displayLabel = null,
            receiveOnly = false,
        )
        assertNull(next.catalogId)
        assertFalse(HubPackageCodec.toJson(next).has("catalogId"))
    }

    @Test
    fun emptyRoomReceiveKeepsRemoteCardsAndNoMediaPayload() {
        val remote = card("media.remote", title = "Dal fascicolo")
        val original = pkg(listOf(remote), catalogId = "cat-1")
        val preview = HubDiffEngine.preview(emptyList(), listOf(remote), emptyMap())
        assertEquals(1, preview.toReceive.size)
        val next = HubApplyPlan.nextPackage(original, preview, 20L, "tablet", emptyList(), null, false)
        assertEquals(listOf("media.remote"), next.cards.map { it.id })
        val json = HubPackageCodec.toJsonString(next)
        assertFalse(json.contains("content://"))
        assertFalse(json.contains("\"uri\""))
        assertFalse(HubPackageCodec.containsForbiddenKeys(json))
    }

    @Test
    fun receiveOnlyDoesNotPublishLocalCardsOrSources() {
        val remote = card("remote")
        val localOnly = card("local-only", title = "Solo qui")
        val remoteSource = source("from-file")
        val localSource = source("from-phone", provider = "GOOGLE_DRIVE")
        val original = pkg(listOf(remote), catalogId = "cat-1", sources = listOf(remoteSource))
        val resolved = HubDiffPreview(
            toPublish = listOf(HubCardChange("local-only", "Solo qui", HubCardChangeKind.PUBLISH, localOnly, null)),
            toReceive = listOf(HubCardChange("remote", "remote", HubCardChangeKind.RECEIVE, null, remote)),
            conflicts = emptyList(),
        )
        val next = HubApplyPlan.nextPackage(
            original, resolved, 30L, "tablet", listOf(localSource), "etichetta", receiveOnly = true,
        )
        assertEquals(listOf("remote"), next.cards.map { it.id })
        assertEquals(listOf(remoteSource), next.sources)
        assertEquals("cat-1", next.catalogId)
    }

    @Test
    fun trashVersusTitleIsConflictAndOtherCardStillApplies() {
        val stamp = mapOf("c" to HubDiffEngine.SyncStamp(10L, "phone", trashed = false))
        val local = card("c", at = 10L, title = "Vecchio", trashed = true)
        val remote = card("c", at = 20L, by = "tablet", title = "Nuovo", trashed = false)
        val other = card("d", at = 8L, by = "tablet", title = "Altra")
        val preview = HubDiffEngine.preview(listOf(local), listOf(remote, other), stamp)
        assertEquals(listOf("c"), preview.conflicts.map { it.cardId })
        assertEquals(listOf("d"), preview.toReceive.map { it.cardId })
        val held = HubDiffEngine.applyResolutions(preview, emptyMap())
        val next = HubApplyPlan.nextPackage(
            pkg(listOf(remote, other)),
            held,
            40L,
            "phone",
            emptyList(),
            null,
            false,
        )
        assertEquals(remote, next.cards.first { it.id == "c" })
        assertTrue(next.cards.any { it.id == "d" })
    }

    @Test
    fun keepLocalReplacesTheWholeCard() {
        val local = card("c", at = 15L, by = "phone", title = "Mia", trashed = true)
        val remote = card("c", at = 16L, by = "tablet", title = "Sua", trashed = false)
        val conflict = HubCardChange("c", "Mia", HubCardChangeKind.CONFLICT, local, remote)
        val resolved = HubDiffEngine.applyResolutions(
            HubDiffPreview(emptyList(), emptyList(), listOf(conflict)),
            mapOf("c" to ConflictResolution.KEEP_LOCAL),
        )
        val next = HubApplyPlan.nextPackage(pkg(listOf(remote)), resolved, 41L, "phone", emptyList(), null, false)
        assertEquals(local, next.cards.single())
    }

    @Test
    fun trashChoiceOnlyChangesCopyState() {
        assertEquals(MediaCopyState.TRASHED, HubCardApply.copyStateAfterRemote(true, MediaCopyState.ACTIVE))
        assertEquals(MediaCopyState.ACTIVE, HubCardApply.copyStateAfterRemote(false, MediaCopyState.TRASHED))
        assertEquals(MediaCopyState.STAGED, HubCardApply.copyStateAfterRemote(false, MediaCopyState.STAGED))
    }

    @Test
    fun receivedTrashedCardWithNoCopiesDoesNotNeedPublish() {
        val remote = card("a", trashed = true)
        val stamp = HubDiffEngine.SyncStamp(10L, "phone", trashed = true)
        val local = remote.copy(trashed = HubLocalCard.trashed(emptyList(), stamp))
        val preview = HubDiffEngine.preview(listOf(local), listOf(remote), mapOf("a" to stamp))
        assertTrue(preview.isEmpty)
        assertTrue(HubLocalCard.trashed(emptyList(), HubDiffEngine.SyncStamp(10L, "phone", trashed = false)).not())
    }

    @Test
    fun divergedCardsWithoutAChangedSideStayInConflict() {
        val stamp = mapOf("c" to HubDiffEngine.SyncStamp(10L, "phone", trashed = false))
        val preview = HubDiffEngine.preview(
            listOf(card("c", title = "A")),
            listOf(card("c", title = "B")),
            stamp,
        )
        assertEquals(1, preview.conflicts.size)
        assertTrue(preview.toPublish.isEmpty())
    }

    @Test
    fun publishedSourcesCarryNoPermissionOrSecret() {
        val descriptor = source("k1")
        val json = HubPackageCodec.toJson(pkg(emptyList(), catalogId = "id", sources = listOf(descriptor)))
        val sourceJson = json.getJSONArray("sources").getJSONObject(0)
        val keys = sourceJson.keys().asSequence().toSet()
        assertEquals(
            setOf("sourceKey", "displayName", "provider", "humanPath", "documentId", "shareable"),
            keys,
        )
        assertFalse(HubPackageCodec.containsForbiddenKeys(JSONObject().put("sources", json.getJSONArray("sources")).toString()))
        assertFalse(HubSourcePolicy.shareable("APP_PRIVATE"))
        assertFalse(HubSourcePolicy.shareable("LOCAL_STORAGE"))
        assertFalse(HubSourcePolicy.shareable("DOWNLOADS"))
        assertTrue(HubSourcePolicy.shareable("GOOGLE_DRIVE"))
        assertTrue(HubSourcePolicy.shareable("NETWORK_NAS"))
        assertTrue(HubSourcePolicy.shareable("OTHER"))
        assertFalse(HubSourcePolicy.linksWithoutExplicitConfirm())
    }

    @Test
    fun missingOrEmptyFileIsNotACatalog() {
        assertEquals(IdentityAction.NOT_A_CATALOG, CatalogIdentityRules.assess(null, null, emptySet()).action)
        val empty = CatalogIdentityRules.assess(pkg(emptyList()), "adopted", emptySet())
        assertEquals(IdentityAction.NOT_A_CATALOG, empty.action)
        assertNull(empty.fileCatalogId)
    }

    @Test
    fun sameFileIdIsAdoptedWithoutNeedingANewOne() {
        val file = pkg(listOf(card("a")), catalogId = "same")
        val assessment = CatalogIdentityRules.assess(file, "same", setOf("a"))
        assertEquals(IdentityAction.SAME_ID, assessment.action)
        val fresh = CatalogIdentityRules.assess(file, null, emptySet())
        assertEquals(IdentityAction.ADOPT_FILE_ID, fresh.action)
    }

    @Test
    fun fullUpdateKeepsRemoteOnlySources() {
        val remoteOnly = source("tablet-source")
        val local = source("phone-source", provider = "OTHER")
        val merged = HubApplyPlan.mergedSources(listOf(remoteOnly), listOf(local))
        assertEquals(listOf("tablet-source", "phone-source"), merged.map { it.sourceKey })
    }

    @Test
    fun heldConflictIsNotStamped() {
        val remote = card("c", at = 20L, by = "tablet", trashed = true)
        val conflict = HubCardChange("c", "c", HubCardChangeKind.CONFLICT, card("c", trashed = false), remote)
        val resolved = HubDiffEngine.applyResolutions(
            HubDiffPreview(emptyList(), emptyList(), listOf(conflict)),
            emptyMap(),
        )
        val stamps = HubApplyPlan.stampsFor(resolved, emptyMap(), receiveOnly = false)
        assertTrue(stamps.isEmpty())
    }
}
