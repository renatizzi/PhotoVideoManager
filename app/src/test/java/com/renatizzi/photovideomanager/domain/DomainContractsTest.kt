package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.DomainScope
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.policy.DisabledSyncPort
import com.renatizzi.photovideomanager.domain.policy.LocalOwnerPermissionGate
import com.renatizzi.photovideomanager.domain.policy.LocalTrustAuthPort
import com.renatizzi.photovideomanager.domain.port.AuthResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainContractsTest {
    @Test
    fun permissionGate_localOwner_allowsPersonalAndFamilyView() {
        val gate = LocalOwnerPermissionGate()
        assertTrue(gate.canView(DomainScope.PERSONAL))
        assertTrue(gate.canView(DomainScope.FAMILY))
        assertTrue(gate.canConsolidateToShared())
    }

    @Test
    fun authPort_localTrust_isTrusted() = runBlocking {
        val auth = LocalTrustAuthPort()
        assertEquals(AuthResult.Trusted, auth.ensureAuthenticated("any"))
    }

    @Test
    fun syncPort_disabled_in_v1() {
        assertFalse(DisabledSyncPort().isEnabled)
    }

    @Test
    fun storageCapability_matrix_covers_required_set() {
        val required = setOf(
            StorageCapability.LIST,
            StorageCapability.READ,
            StorageCapability.WRITE,
            StorageCapability.CREATE_DIRECTORY,
            StorageCapability.RENAME,
            StorageCapability.MOVE,
            StorageCapability.DELETE,
            StorageCapability.RANDOM_READ,
            StorageCapability.SEQUENTIAL_READ,
            StorageCapability.METADATA,
            StorageCapability.FINGERPRINT_STREAM,
            StorageCapability.AVAILABILITY,
        )
        assertEquals(12, required.size)
    }
}
