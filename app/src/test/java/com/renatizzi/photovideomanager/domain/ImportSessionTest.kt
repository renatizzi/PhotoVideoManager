package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.AcquireResult
import com.renatizzi.photovideomanager.domain.model.ImportSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImportSessionTest {
    @Test
    fun acquireResult_completed_counts_are_preserved() {
        val result = AcquireResult(
            sessionId = "import.test",
            state = ImportSessionState.COMPLETED,
            acquired = 3,
            skippedAlreadyPresent = 1,
            failed = 0,
        )
        assertEquals(ImportSessionState.COMPLETED, result.state)
        assertEquals(3, result.acquired)
        assertEquals(1, result.skippedAlreadyPresent)
        assertEquals(0, result.failed)
        assertNull(result.message)
    }

    @Test
    fun importSessionState_covers_m06_lifecycle() {
        val expected = listOf(
            ImportSessionState.CREATED,
            ImportSessionState.PREFLIGHT,
            ImportSessionState.RUNNING,
            ImportSessionState.COMPLETED,
            ImportSessionState.FAILED,
            ImportSessionState.CANCELLED,
        )
        assertEquals(expected, ImportSessionState.entries)
    }
}
