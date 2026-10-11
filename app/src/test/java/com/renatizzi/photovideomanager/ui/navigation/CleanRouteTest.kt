package com.renatizzi.photovideomanager.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanRouteTest {
    @Test
    fun create_encodes_selected_ids() {
        val route = PvmDestination.Clean.create(setOf("media.1", "media.2"))
        assertTrue(route.startsWith("organizza/clean?sel="))
        val sel = route.substringAfter("sel=")
        val ids = sel.split(',').toSet()
        assertEquals(setOf("media.1", "media.2"), ids)
    }

    @Test
    fun create_empty_selection() {
        assertEquals("organizza/clean?sel=", PvmDestination.Clean.create())
    }
}
