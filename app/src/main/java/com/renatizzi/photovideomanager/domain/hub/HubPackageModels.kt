package com.renatizzi.photovideomanager.domain.hub

/**
 * Fascicolo schede in sede (H1). Solo metadati Catalogo personale, non i file media.
 */
data class HubPackage(
    val formatVersion: Int = FORMAT_VERSION,
    val seatId: String,
    val updatedAtEpochMs: Long,
    val updatedByDeviceId: String,
    val cards: List<HubCard> = emptyList(),
) {
    companion object {
        const val FORMAT_VERSION = 1
        const val RELATIVE_DIR = "MediaManager"
        const val FILE_NAME = "catalog_hub.json"
    }
}

data class HubCard(
    val id: String,
    val kind: String,
    val displayTitle: String?,
    val updatedAtEpochMs: Long,
    val updatedByDeviceId: String,
    val trashed: Boolean = false,
    val sha256: String? = null,
)

enum class HubCardChangeKind {
    PUBLISH,
    RECEIVE,
    CONFLICT,
}

data class HubCardChange(
    val cardId: String,
    val title: String,
    val kind: HubCardChangeKind,
    val local: HubCard?,
    val remote: HubCard?,
)

data class HubDiffPreview(
    val toPublish: List<HubCardChange>,
    val toReceive: List<HubCardChange>,
    val conflicts: List<HubCardChange>,
) {
    val isEmpty: Boolean
        get() = toPublish.isEmpty() && toReceive.isEmpty() && conflicts.isEmpty()
}

enum class ConflictResolution {
    KEEP_LOCAL,
    TAKE_REMOTE,
    DEFER,
}
