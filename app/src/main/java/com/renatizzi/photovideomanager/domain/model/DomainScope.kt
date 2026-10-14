package com.renatizzi.photovideomanager.domain.model

/**
 * Dominio logico del MediaItem (non della singola MediaCopy).
 * PERSONAL → FAMILY solo per condivisione/consolidamento esplicito.
 */
enum class DomainScope {
    PERSONAL,
    FAMILY,
}
