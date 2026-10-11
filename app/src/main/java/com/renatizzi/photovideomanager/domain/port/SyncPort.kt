package com.renatizzi.photovideomanager.domain.port

/**
 * Estensione architetturale futura. Non autorizza sync/code/retry automatici in v1.
 */
interface SyncPort {
    val isEnabled: Boolean
        get() = false
}
