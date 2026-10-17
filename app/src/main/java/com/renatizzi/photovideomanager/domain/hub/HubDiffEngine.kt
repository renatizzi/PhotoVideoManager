package com.renatizzi.photovideomanager.domain.hub

/**
 * Confronta copia di lavoro (localCards) e fascicolo sede (remoteCards).
 * [lastSynced] = ultima revisione applicata con successo per ciascuna scheda (updatedAt|deviceId).
 */
object HubDiffEngine {

    data class SyncStamp(
        val updatedAtEpochMs: Long,
        val updatedByDeviceId: String,
        val trashed: Boolean = false,
    )

    fun preview(
        localCards: List<HubCard>,
        remoteCards: List<HubCard>,
        lastSynced: Map<String, SyncStamp>,
    ): HubDiffPreview {
        val localById = localCards.associateBy { it.id }
        val remoteById = remoteCards.associateBy { it.id }
        val ids = localById.keys + remoteById.keys
        val publish = mutableListOf<HubCardChange>()
        val receive = mutableListOf<HubCardChange>()
        val conflicts = mutableListOf<HubCardChange>()

        for (id in ids) {
            val local = localById[id]
            val remote = remoteById[id]
            val synced = lastSynced[id]
            val title = local?.displayTitle ?: remote?.displayTitle ?: id

            when {
                local != null && remote == null -> {
                    publish += HubCardChange(id, title, HubCardChangeKind.PUBLISH, local, null)
                }
                local == null && remote != null -> {
                    receive += HubCardChange(id, title, HubCardChangeKind.RECEIVE, null, remote)
                }
                local != null && remote != null -> {
                    if (sameRevision(local, remote)) {
                        // già allineate
                    } else {
                        val localChanged = changedSince(local, synced)
                        val remoteChanged = changedSince(remote, synced)
                        when {
                            localChanged && remoteChanged ->
                                conflicts += HubCardChange(id, title, HubCardChangeKind.CONFLICT, local, remote)
                            localChanged && !remoteChanged ->
                                publish += HubCardChange(id, title, HubCardChangeKind.PUBLISH, local, remote)
                            !localChanged && remoteChanged ->
                                receive += HubCardChange(id, title, HubCardChangeKind.RECEIVE, local, remote)
                            else ->
                                conflicts += HubCardChange(id, title, HubCardChangeKind.CONFLICT, local, remote)
                        }
                    }
                }
            }
        }
        return HubDiffPreview(publish, receive, conflicts)
    }

    fun applyResolutions(
        preview: HubDiffPreview,
        resolutions: Map<String, ConflictResolution>,
    ): HubDiffPreview {
        val stillConflict = mutableListOf<HubCardChange>()
        val extraPublish = mutableListOf<HubCardChange>()
        val extraReceive = mutableListOf<HubCardChange>()
        for (c in preview.conflicts) {
            when (resolutions[c.cardId] ?: ConflictResolution.DEFER) {
                ConflictResolution.KEEP_LOCAL ->
                    extraPublish += c.copy(kind = HubCardChangeKind.PUBLISH)
                ConflictResolution.TAKE_REMOTE ->
                    extraReceive += c.copy(kind = HubCardChangeKind.RECEIVE)
                ConflictResolution.DEFER -> stillConflict += c
            }
        }
        return HubDiffPreview(
            toPublish = preview.toPublish + extraPublish,
            toReceive = preview.toReceive + extraReceive,
            conflicts = stillConflict,
        )
    }

    private fun sameRevision(a: HubCard, b: HubCard): Boolean =
        a.updatedAtEpochMs == b.updatedAtEpochMs &&
            a.updatedByDeviceId == b.updatedByDeviceId &&
            a.trashed == b.trashed &&
            a.displayTitle == b.displayTitle

    private fun changedSince(card: HubCard, synced: SyncStamp?): Boolean {
        if (synced == null) return true
        return card.updatedAtEpochMs != synced.updatedAtEpochMs ||
            card.updatedByDeviceId != synced.updatedByDeviceId ||
            card.trashed != synced.trashed
    }
}
