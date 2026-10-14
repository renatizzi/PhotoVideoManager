package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import com.renatizzi.photovideomanager.domain.model.cycleNext
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceCensusSelectionTest {
    @Test
    fun cycleNext_follows_bl03_three_states() {
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
    fun cycleNext_full_round_trip() {
        var state = SourceCensusSelection.SELECTED
        state = state.cycleNext()
        assertEquals(SourceCensusSelection.NOT_SELECTED, state)
        state = state.cycleNext()
        assertEquals(SourceCensusSelection.EXCLUDED, state)
        state = state.cycleNext()
        assertEquals(SourceCensusSelection.SELECTED, state)
    }
}
