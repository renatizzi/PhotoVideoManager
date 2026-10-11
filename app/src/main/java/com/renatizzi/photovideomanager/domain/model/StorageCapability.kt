package com.renatizzi.photovideomanager.domain.model

enum class StorageCapability {
    LIST,
    READ,
    WRITE,
    CREATE_DIRECTORY,
    RENAME,
    MOVE,
    DELETE,
    RANDOM_READ,
    SEQUENTIAL_READ,
    METADATA,
    FINGERPRINT_STREAM,
    AVAILABILITY,
}
