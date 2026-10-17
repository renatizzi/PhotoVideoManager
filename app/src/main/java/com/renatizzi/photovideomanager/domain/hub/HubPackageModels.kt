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
    /** Assente nei fascicoli storici: non va inventato in lettura. */
    val catalogId: String? = null,
    val displayLabel: String? = null,
    val sources: List<HubSourceDescriptor> = emptyList(),
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

/**
 * Descrizione portabile di una sorgente. Non contiene URI SAF, permessi o credenziali.
 * [documentId] e [humanPath] sono indizi, non un'identità trasferibile.
 */
data class HubSourceDescriptor(
    val sourceKey: String,
    val displayName: String,
    val provider: String,
    val humanPath: String = "",
    val documentId: String? = null,
    val shareable: Boolean = false,
)

enum class IdentityAction {
    NOT_A_CATALOG,
    SAME_ID,
    ADOPT_FILE_ID,
    BLOCK_DIFFERENT_IDS,
    NEEDS_CONFIRM_NEW_ID,
    NEEDS_CONFIRM_STAMP,
}

enum class OverlapHint {
    NO_LOCAL,
    NONE,
    PARTIAL,
    FULL,
}

data class IdentityAssessment(
    val action: IdentityAction,
    val fileCardCount: Int,
    val sharedCardCount: Int,
    val fileCatalogId: String?,
    val adoptedCatalogId: String?,
    val hint: OverlapHint,
    val updatedAtEpochMs: Long = 0L,
    val displayLabel: String? = null,
)
