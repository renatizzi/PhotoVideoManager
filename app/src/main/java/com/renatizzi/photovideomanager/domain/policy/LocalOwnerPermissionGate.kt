package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.port.PermissionGate

/**
 * Gate iniziale single-user local-first.
 * Ruoli familiari (M13) sostituiranno/estenderanno questa policy.
 */
class LocalOwnerPermissionGate : PermissionGate {
    override fun canView(scope: DomainScope): Boolean = true
    override fun canMutateCatalog(scope: DomainScope): Boolean = true
    override fun canConsolidateToShared(): Boolean = true
    override fun canDestructiveShared(): Boolean = true
}
