package com.renatizzi.photovideomanager.domain.hub

import org.junit.Assert.assertEquals
import org.junit.Test

class PcSeatDeviceModelTest {

    @Test
    fun resolve_rejectsPhoneModel_usesPcWindows() {
        assertEquals(
            "PC Windows",
            PcSeatDeviceModel.resolve(
                typedModel = "S25 Ultra di RENATO",
                phoneSuggested = "S25 Ultra di RENATO",
                pcComputerName = "",
            ),
        )
    }

    @Test
    fun resolve_prefersComputerNameWhenPhonePrefilled() {
        assertEquals(
            "DELLRS",
            PcSeatDeviceModel.resolve(
                typedModel = "S25 Ultra di RENATO",
                phoneSuggested = "S25 Ultra di RENATO",
                pcComputerName = "DELLRS",
            ),
        )
    }

    @Test
    fun resolve_keepsUserEditedPcLabel() {
        assertEquals(
            "PC-UFFICIO",
            PcSeatDeviceModel.resolve(
                typedModel = "PC-UFFICIO",
                phoneSuggested = "S25 Ultra di RENATO",
                pcComputerName = "DELLRS",
            ),
        )
    }
}
