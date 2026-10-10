package com.renatizzi.photovideomanager.ui.navigation

import com.renatizzi.photovideomanager.ui.shell.ShellTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabNavigationTest {
    @Test
    fun tabForRoute_mapsNestedAcquireToOrganizza() {
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/acquire"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/clean"))
    }

    @Test
    fun tabForRoute_mapsNestedTrashToUtility() {
        assertEquals(ShellTab.UTILITY, tabForRoute("utility/trash"))
        assertEquals(ShellTab.UTILITY, tabForRoute("utility/ripristina"))
        assertEquals(ShellTab.UTILITY, tabForRoute("utility"))
    }

    @Test
    fun tabForRoute_mapsNestedOrganizzaRoutes() {
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/aggiorna"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/search"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/acquire"))
        assertEquals(
            ShellTab.ORGANIZZA,
            tabForRoute("organizza/acquire/browse/location.saf.demo"),
        )
        assertTrue(
            PvmDestination.SourceBrowse.create("location.saf.demo")
                .startsWith("organizza/acquire/browse/"),
        )
    }

    @Test
    fun tabForRoute_configSelectsConfigura() {
        assertEquals(ShellTab.CONFIGURA, tabForRoute("config"))
    }

    @Test
    fun tabRootRoutes_areHubOnly() {
        assertEquals("home", PvmDestination.Home.route)
        assertEquals("organizza", PvmDestination.Organizza.route)
        assertFalse(PvmDestination.Acquire.route == PvmDestination.Organizza.route)
        assertTrue(PvmDestination.Acquire.route.startsWith("organizza"))
        assertTrue(PvmDestination.Trash.route.startsWith("utility"))
    }
}
