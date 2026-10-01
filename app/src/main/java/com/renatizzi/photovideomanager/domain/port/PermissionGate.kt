package com.renatizzi.photovideomanager.domain.port

import com.renatizzi.photovideomanager.domain.model.DomainScope

/**
 * Autorizzazione logica applicativa, distinta da:
 * - permission OS
 * - capability fisiche dello StorageAdapter
 * - AuthPort verso la risorsa
 */
interface PermissionGate {
    fun canView(scope: DomainScope): Boolean
    fun canMutateCatalog(scope: DomainScope): Boolean
    fun canConsolidateToShared(): Boolean
    fun canDestructiveShared(): Boolean
}
