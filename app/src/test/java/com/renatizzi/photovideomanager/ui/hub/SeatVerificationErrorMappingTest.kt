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
            m.contains("not found") ||
            m.contains("elenco delle share") ||
            m.contains("folder=") ||
            m.contains("read only") ||
            m.contains("non scrivibile")
    }

    @Test
    fun mapsFascicoloAndScritturaAsUnreachable() {
        assertTrue(isUnreachableSeatError("Impossibile creare il fascicolo nella sede"))
        assertTrue(isUnreachableSeatError("Scrittura sede non disponibile"))
        assertTrue(isUnreachableSeatError("Permission denied"))
        assertTrue(isUnreachableSeatError("Hai selezionato l’elenco delle share"))
        assertTrue(isUnreachableSeatError("Folder=MyMedia e Read only"))
        assertTrue(isUnreachableSeatError("Posizione del Catalogo non scrivibile"))
    }

    @Test
    fun otherErrorsStayGenericFailed() {
        assertFalse(isUnreachableSeatError(""))
        assertFalse(isUnreachableSeatError("qualcosa di inatteso"))
    }
}
