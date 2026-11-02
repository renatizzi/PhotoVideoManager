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

    @Test
    fun allSources_includesAdditional() {
        fun summary(id: String, name: String) = SourceSummary(
            locationId = id,
            archiveId = "a",
            displayName = name,
            deviceLabel = "Questo dispositivo",
            pathLabel = "\\Pictures\\$name",
            adapterKind = StorageAdapterKind.SAF_TREE,
            availability = Availability.AVAILABLE,
            isSharedArchive = false,
            isBuiltInPersonal = false,
        )
        val result = RegisterSourceResult(
            source = summary("1", "familiari"),
            alreadyPresent = false,
            additionalSources = listOf(summary("2", "vacanze"), summary("3", "work")),
        )
        assertEquals(3, result.allSources.size)
        assertEquals(listOf("familiari", "vacanze", "work"), result.allSources.map { it.displayName })
    }
}
