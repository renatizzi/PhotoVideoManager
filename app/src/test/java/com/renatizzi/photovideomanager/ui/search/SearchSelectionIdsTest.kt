package com.renatizzi.photovideomanager.ui.search

import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchSelectionIdsTest {
    @Test
    fun selectedMediaIds_only_selected_checkmarks() {
        val state = SearchUiState(
            selection = mapOf(
                "a" to SourceCensusSelection.SELECTED,
                "b" to SourceCensusSelection.NOT_SELECTED,
                "c" to SourceCensusSelection.EXCLUDED,
                "d" to SourceCensusSelection.SELECTED,
            ),
        )
        assertEquals(setOf("a", "d"), state.selectedMediaIds)
    }
}
