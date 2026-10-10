package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.DuplicateMember
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaFingerprint
import com.renatizzi.photovideomanager.domain.model.MediaItem
import com.renatizzi.photovideomanager.domain.model.MediaKind

/**
 * Raggruppamento puro duplicati esatti (testabile senza Android).
 * Un gruppo esiste solo se lo stesso hash SHA-256 compare su almeno 2 MediaItem distinti.
 * Copie dello stesso item (sorgente + spazio app) da sole non formano un gruppo.
 */
object ExactDedupGrouping {
    const val SHA256 = "SHA-256"
    const val LEVEL = 2

    fun buildGroups(
        itemsById: Map<String, MediaItem>,
        copies: List<MediaCopy>,
        shaFingerprints: List<MediaFingerprint>,
        locationNames: Map<String, String>,
    ): List<DuplicateGroup> {
        val copyById = copies.associateBy { it.id }
        val byHash = linkedMapOf<String, MutableList<Pair<MediaFingerprint, MediaCopy>>>()
        for (fp in shaFingerprints) {
            if (fp.algorithm != SHA256 || fp.level != LEVEL) continue
            val copy = copyById[fp.mediaCopyId] ?: continue
            byHash.getOrPut(fp.value) { mutableListOf() }.add(fp to copy)
        }

        return byHash.mapNotNull { (hash, pairs) ->
            val distinctItems = pairs.map { it.second.mediaItemId }.toSet()
            if (distinctItems.size < 2) return@mapNotNull null

            val keepItemId = distinctItems
                .mapNotNull { itemsById[it] }
                .minByOrNull { it.createdAtEpochMs }
                ?.id
                ?: distinctItems.first()

            val members = pairs.mapNotNull { (_, copy) ->
                val item = itemsById[copy.mediaItemId] ?: return@mapNotNull null
                DuplicateMember(
                    mediaItem = item,
                    mediaCopy = copy,
                    locationName = locationNames[copy.storageLocationId] ?: copy.storageLocationId,
                    isSuggestedKeep = item.id == keepItemId,
                )
            }.sortedWith(
                compareByDescending<DuplicateMember> { it.isSuggestedKeep }
                    .thenBy { it.mediaItem.displayTitle.orEmpty() },
            )
            if (members.size < 2) return@mapNotNull null
            DuplicateGroup(
                fingerprintValue = hash,
                algorithm = SHA256,
                members = members,
            )
        }.sortedByDescending { it.extraItemCount }
    }

    fun extraCountsByKind(groups: List<DuplicateGroup>): Pair<Long, Long> {
        var photos = 0L
        var videos = 0L
        for (group in groups) {
            val itemIds = group.members.map { it.mediaItem.id }.toSet()
            val keepId = group.members.firstOrNull { it.isSuggestedKeep }?.mediaItem?.id
            for (itemId in itemIds) {
                if (itemId == keepId) continue
                when (group.members.firstOrNull { it.mediaItem.id == itemId }?.mediaItem?.kind) {
                    MediaKind.PHOTO -> photos++
                    MediaKind.VIDEO -> videos++
                    null -> Unit
                }
            }
        }
        return photos to videos
    }
}
