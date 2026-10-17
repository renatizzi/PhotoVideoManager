package com.renatizzi.photovideomanager.domain.hub

/**
 * Regole di identità del fascicolo. Non scrivono nulla.
 * Un id di scheda in comune è un indizio, non la prova che i due file siano lo stesso Catalogo per intero.
 */
object CatalogIdentityRules {
    fun assess(
        file: HubPackage?,
        adoptedCatalogId: String?,
        localCardIds: Set<String>,
    ): IdentityAssessment {
        if (file == null) {
            return IdentityAssessment(
                action = IdentityAction.NOT_A_CATALOG,
                fileCardCount = 0,
                sharedCardCount = 0,
                fileCatalogId = null,
                adoptedCatalogId = adoptedCatalogId.blankToNull(),
                hint = OverlapHint.NO_LOCAL,
            )
        }
        val fileIds = file.cards.map { it.id }.toSet()
        val shared = fileIds.intersect(localCardIds).size
        val hint = hintOf(fileIds, localCardIds, shared)
        val fileId = file.catalogId.blankToNull()
        val adopted = adoptedCatalogId.blankToNull()
        val action = when {
            fileId != null && adopted == null -> IdentityAction.ADOPT_FILE_ID
            fileId != null && adopted == fileId -> IdentityAction.SAME_ID
            fileId != null && adopted != fileId -> IdentityAction.BLOCK_DIFFERENT_IDS
            file.cards.isEmpty() -> IdentityAction.NOT_A_CATALOG
            adopted == null -> IdentityAction.NEEDS_CONFIRM_NEW_ID
            else -> IdentityAction.NEEDS_CONFIRM_STAMP
        }
        return IdentityAssessment(
            action = action,
            fileCardCount = file.cards.size,
            sharedCardCount = shared,
            fileCatalogId = fileId,
            adoptedCatalogId = adopted,
            hint = hint,
            updatedAtEpochMs = file.updatedAtEpochMs,
            displayLabel = file.displayLabel,
        )
    }

    private fun hintOf(fileIds: Set<String>, localIds: Set<String>, shared: Int): OverlapHint {
        if (localIds.isEmpty()) return OverlapHint.NO_LOCAL
        if (shared == 0) return OverlapHint.NONE
        if (fileIds == localIds) return OverlapHint.FULL
        return OverlapHint.PARTIAL
    }
}

internal fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
