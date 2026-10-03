package com.renatizzi.photovideomanager.ui.navigation

import com.renatizzi.photovideomanager.ui.shell.ShellTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TabNavigationTest {
    @Test
    fun tabForRoute_mapsNestedAcquireToOrganizza() {
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/acquire"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza"))
    }

    @Test
    fun tabForRoute_mapsNestedCleanToGestisci() {
        assertEquals(ShellTab.GESTISCI, tabForRoute("gestisci/clean"))
        assertEquals(ShellTab.GESTISCI, tabForRoute("gestisci/trash"))
    }

    @Test
    fun tabForRoute_mapsNestedArchiveToOrganizza() {
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/archive"))
        assertEquals(ShellTab.ORGANIZZA, tabForRoute("organizza/search"))
    }

    @Test
    fun tabForRoute_configHasNoBottomSelection() {
        assertNull(tabForRoute("config"))
        assertNull(tabForRoute("config/archive"))
    }

    @Test
    fun tabRootRoutes_areHubOnly() {
        assertEquals("home", PvmDestination.Home.route)
        assertEquals("organizza", PvmDestination.Organizza.route)
        assertFalse(PvmDestination.Acquire.route == PvmDestination.Organizza.route)
        assertTrue(PvmDestination.Acquire.route.startsWith("organizza"))
    }
}
