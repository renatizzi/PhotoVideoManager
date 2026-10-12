package com.renatizzi.photovideomanager.domain.hub

enum class CatalogSeatKind {
    NONE,
    LOCAL_DEVICE,
    PC_NOTEBOOK,
    CLOUD_DRIVE,
    NETWORK_NAS,
}

data class CatalogSeat(
    val kind: CatalogSeatKind,
    val treeUri: String? = null,
    val displayLabel: String = "",
    val configuredAtEpochMs: Long = 0L,
) {
    val isConfigured: Boolean
        get() = kind != CatalogSeatKind.NONE &&
            (kind == CatalogSeatKind.LOCAL_DEVICE || !treeUri.isNullOrBlank())
}
