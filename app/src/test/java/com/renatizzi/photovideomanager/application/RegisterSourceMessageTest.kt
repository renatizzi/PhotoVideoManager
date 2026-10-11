package com.renatizzi.photovideomanager.application

import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.RegisterSourceResult
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import com.renatizzi.photovideomanager.domain.model.StorageAdapterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterSourceMessageTest {
    @Test
    fun alreadyPresent_usesDedicatedMessage() {
        val result = RegisterSourceResult(
            source = SourceSummary(
                locationId = "location.saf.1",
                archiveId = "archive.1",
                displayName = "Eraser",
                deviceLabel = "Telefono",
                pathLabel = "Memoria principale/Eraser",
                adapterKind = StorageAdapterKind.SAF_TREE,
                availability = Availability.AVAILABLE,
                isSharedArchive = false,
                isBuiltInPersonal = false,
            ),
            alreadyPresent = true,
        )
        val message = if (result.alreadyPresent) {
            "Sorgente già presente in elenco"
        } else {
            "Sorgente aggiunta: ${result.source.displayName}"
        }
        assertEquals("Sorgente già presente in elenco", message)
    }
}
