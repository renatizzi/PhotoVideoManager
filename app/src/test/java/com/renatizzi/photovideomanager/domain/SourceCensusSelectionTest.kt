package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.cycleNext
import com.renatizzi.photovideomanager.domain.model.cycleNextCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceCensusSelectionTest {
    @Test
    fun cycleNext_acquisisci_default_selected() {
        assertEquals(
            SourceCensusSelection.NOT_SELECTED,
            SourceCensusSelection.SELECTED.cycleNext(),
        )
        assertEquals(
            SourceCensusSelection.EXCLUDED,
            SourceCensusSelection.NOT_SELECTED.cycleNext(),
        )
        assertEquals(
            SourceCensusSelection.SELECTED,
            SourceCensusSelection.EXCLUDED.cycleNext(),
        )
    }

    @Test
    fun cycleNextCatalog_aggiorna_default_deselected() {
        assertEquals(
            SourceCensusSelection.SELECTED,
            SourceCensusSelection.NOT_SELECTED.cycleNextCatalog(),
        )
        assertEquals(
            SourceCensusSelection.EXCLUDED,
            SourceCensusSelection.SELECTED.cycleNextCatalog(),
        )
        assertEquals(
            SourceCensusSelection.NOT_SELECTED,
            SourceCensusSelection.EXCLUDED.cycleNextCatalog(),
        )
    }
}
