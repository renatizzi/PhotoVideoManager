package com.renatizzi.photovideomanager.domain.hub

/**
 * Documento completo da scrivere. Le schede in conflitto restano quelle del fascicolo già letto.
 * [catalogId] non viene mai sostituito né inventato.
 */
object HubApplyPlan {
    fun nextPackage(
        original: HubPackage,
        resolved: HubDiffPreview,
        now: Long,
        deviceId: String,
        sources: List<HubSourceDescriptor>,
        displayLabel: String?,
        receiveOnly: Boolean,
    ): HubPackage {
        val held = resolved.conflicts.map { it.cardId }.toSet()
        val byId = original.cards.associateBy { it.id }.toMutableMap()
        for (change in resolved.toReceive) {
            if (change.cardId in held) continue
            val remote = change.remote ?: continue
            byId[remote.id] = remote
        }
        if (!receiveOnly) {
            for (change in resolved.toPublish) {
                if (change.cardId in held) continue
                val local = change.local ?: continue
                byId[local.id] = local
            }
        }
        return original.copy(
            catalogId = original.catalogId.blankToNull(),
            displayLabel = if (receiveOnly) {
                original.displayLabel.blankToNull()
            } else {
                original.displayLabel.blankToNull() ?: displayLabel.blankToNull()
            },
            sources = if (receiveOnly) original.sources else mergedSources(original.sources, sources),
            updatedAtEpochMs = now,
            updatedByDeviceId = deviceId,
            cards = byId.values.sortedBy { it.id },
        )
    }

    /** Le sorgenti già nel fascicolo restano. Quelle di questo apparecchio si aggiungono o si aggiornano per sourceKey. */
    fun mergedSources(
        original: List<HubSourceDescriptor>,
        local: List<HubSourceDescriptor>,
    ): List<HubSourceDescriptor> {
        val localByKey = local.associateBy { it.sourceKey }
        val seen = mutableSetOf<String>()
        val merged = mutableListOf<HubSourceDescriptor>()
        for (source in original) {
            merged += localByKey[source.sourceKey] ?: source
            seen += source.sourceKey
        }
        for (source in local) {
            if (source.sourceKey !in seen) merged += source
        }
        return merged
    }

    fun stampsFor(
        resolved: HubDiffPreview,
        previous: Map<String, HubDiffEngine.SyncStamp>,
        receiveOnly: Boolean,
    ): Map<String, HubDiffEngine.SyncStamp> {
        val held = resolved.conflicts.map { it.cardId }.toSet()
        val stamps = previous.toMutableMap()
        for (change in resolved.toReceive) {
            if (change.cardId in held) continue
            val remote = change.remote ?: continue
            stamps[remote.id] = HubDiffEngine.SyncStamp(
                updatedAtEpochMs = remote.updatedAtEpochMs,
                updatedByDeviceId = remote.updatedByDeviceId,
                trashed = remote.trashed,
            )
        }
        if (!receiveOnly) {
            for (change in resolved.toPublish) {
                if (change.cardId in held) continue
                val local = change.local ?: continue
                stamps[local.id] = HubDiffEngine.SyncStamp(
                    updatedAtEpochMs = local.updatedAtEpochMs,
                    updatedByDeviceId = local.updatedByDeviceId,
                    trashed = local.trashed,
                )
            }
        }
        return stamps
    }
}
