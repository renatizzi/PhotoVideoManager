package com.renatizzi.photovideomanager.ui.census

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CensusUiStateGroupingTest {

    @Test
    fun deviceGroups_groupsByDeviceAndSkipsBuiltInPersonal() {
        val state = CensusUiState(
            sources = listOf(
                source("loc.personal", "Catalogo", "Catalogo", builtIn = true),
                source("loc.cam", "Camera", "Telefono Renato"),
                source("loc.dcim", "DCIM", "Telefono Renato"),
                source("loc.share", "MediaManagerCatalog", "PC / notebook"),
                source("loc.drive", "Foto", "Google Drive", Availability.UNAVAILABLE),
            ),
            selection = emptyMap(),
            loading = false,
        )
        val groups = state.deviceGroups
        assertEquals(3, groups.size)
        assertEquals(
            listOf("Google Drive", "PC / notebook", "Telefono Renato"),
            groups.map { it.deviceLabel },
        )
        val phone = groups.first { it.deviceLabel == "Telefono Renato" }
        assertEquals(2, phone.sources.size)
        assertEquals(2, phone.availableCount)
        val drive = groups.first { it.deviceLabel == "Google Drive" }
        assertEquals(0, drive.availableCount)
        assertTrue(state.visibleSources.none { it.isBuiltInPersonal })
    }

    @Test
    fun selectionOf_defaultsToNotSelected() {
        val state = CensusUiState()
        assertEquals(SourceCensusSelection.NOT_SELECTED, state.selectionOf("missing"))
    }

    private fun source(
        id: String,
        name: String,
        device: String,
        availability: Availability = Availability.AVAILABLE,
        builtIn: Boolean = false,
    ) = SourceSummary(
        locationId = id,
        archiveId = "arch.$id",
        displayName = name,
        deviceLabel = device,
        pathLabel = name,
        adapterKind = StorageAdapterKind.SAF_TREE,
        availability = availability,
        isSharedArchive = false,
        isBuiltInPersonal = builtIn,
    )
}
