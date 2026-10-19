package com.renatizzi.photovideomanager.ui.hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mirrors [CatalogSeatWizardViewModel] unreachable heuristics for S6 messages.
 * Keep in sync if the ViewModel mapping changes.
 */
class SeatVerificationErrorMappingTest {

    private fun isUnreachableSeatError(message: String): Boolean {
        val m = message.lowercase()
        return m.contains("fascicolo") ||
            m.contains("scrittura") ||
            m.contains("non disponibile") ||
            m.contains("permission") ||
            m.contains("eacces") ||
            m.contains("enoent") ||
            m.contains("not found")
    }

    @Test
    fun mapsFascicoloAndScritturaAsUnreachable() {
        assertTrue(isUnreachableSeatError("Impossibile creare il fascicolo nella sede"))
        assertTrue(isUnreachableSeatError("Scrittura sede non disponibile"))
        assertTrue(isUnreachableSeatError("Permission denied"))
    }

    @Test
    fun otherErrorsStayGenericFailed() {
        assertFalse(isUnreachableSeatError(""))
        assertFalse(isUnreachableSeatError("qualcosa di inatteso"))
    }
}
