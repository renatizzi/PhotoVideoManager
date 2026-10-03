package com.renatizzi.photovideomanager.domain.model

data class CensusResult(
    val sessionId: String,
    val sourceLocationId: String,
    val filesSeen: Int,
    val mediaFound: Int,
    val mediaAdded: Int,
    val mediaSkippedExisting: Int,
    val state: ScanSessionState,
    val message: String? = null,
)
