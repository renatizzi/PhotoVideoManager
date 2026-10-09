package com.renatizzi.photovideomanager.application

import org.junit.Assert.assertTrue
import org.junit.Test

class SearchServiceLimitTest {
    @Test
    fun defaultLimit_coversLargeCatalogsBeyondFormerHardCap() {
        // Regressione: Aggiorna mostrava solo 300 elementi su cataloghi >1000.
        assertTrue(SearchService.DEFAULT_LIMIT > 300)
        assertTrue(SearchService.DEFAULT_LIMIT >= 10_000)
    }
}
