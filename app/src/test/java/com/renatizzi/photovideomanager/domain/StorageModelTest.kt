package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageModelTest {
    @Test
    fun sourceSummary_distinguishesBuiltinPersonal() {
        val personal = SourceSummary(
            locationId = "location.personal.local.root",
            archiveId = "archive.personal.local",
            displayName = "Spazio interno app",
            adapterKind = StorageAdapterKind.LOCAL_FS,
            availability = Availability.AVAILABLE,
            isSharedArchive = false,
            isBuiltInPersonal = true,
        )
        assertTrue(personal.isBuiltInPersonal)
        assertFalse(personal.isSharedArchive)
        assertEquals(StorageAdapterKind.LOCAL_FS, personal.adapterKind)
    }

    @Test
    fun adapterKinds_includeSafAndFutureStubs() {
        val kinds = StorageAdapterKind.entries.toSet()
        assertTrue(kinds.contains(StorageAdapterKind.SAF_TREE))
        assertTrue(kinds.contains(StorageAdapterKind.MEDIA_STORE))
        assertTrue(kinds.contains(StorageAdapterKind.SMB))
    }
}
