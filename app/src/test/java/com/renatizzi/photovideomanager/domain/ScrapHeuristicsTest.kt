package com.renatizzi.photovideomanager.domain

import com.renatizzi.photovideomanager.domain.policy.ScrapHeuristics
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrapHeuristicsTest {
    @Test
    fun albumArt_isScrap() {
        assertTrue(ScrapHeuristics.isPossibleScrap("AlbumArtSmall.jpg", 50_000))
    }

    @Test
    fun tinyFile_isScrap() {
        assertTrue(ScrapHeuristics.isPossibleScrap("foto_vacanze.jpg", 500))
    }

    @Test
    fun normalPhoto_notScrap() {
        assertFalse(ScrapHeuristics.isPossibleScrap("IMG_2024_spiaggia.jpg", 1_200_000))
    }

    @Test
    fun folderJpg_isScrap() {
        assertTrue(ScrapHeuristics.isPossibleScrap("Folder.jpg", 80_000))
    }
}
