package com.renatizzi.photovideomanager.domain.model

/** Tipo di adapter fisico associato a una StorageLocation. */
enum class StorageAdapterKind {
    LOCAL_FS,
    SAF_TREE,
    MEDIA_STORE,
    SMB,
}
