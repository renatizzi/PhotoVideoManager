package com.renatizzi.photovideomanager.ui.shell

import com.renatizzi.photovideomanager.ui.theme.ShellPalettes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShellContractTest {
    @Test
    fun shellTabs_homeFourMacroAreasAndSettings() {
        assertEquals(6, ShellTab.entries.size)
        assertTrue(ShellTab.entries.contains(ShellTab.HOME))
        assertTrue(ShellTab.entries.contains(ShellTab.ORGANIZZA))
        assertTrue(ShellTab.entries.contains(ShellTab.COMPONI))
        assertTrue(ShellTab.entries.contains(ShellTab.PUBBLICA))
        assertTrue(ShellTab.entries.contains(ShellTab.GESTISCI))
        assertTrue(ShellTab.entries.contains(ShellTab.CONFIGURA))
    }

    @Test
    fun uiScreenStates_coverNotaSet() {
        assertTrue(UiScreenState.entries.contains(UiScreenState.SOURCE_UNAVAILABLE))
        assertTrue(UiScreenState.entries.contains(UiScreenState.CONFIRM_DESTRUCTIVE))
        assertEquals(9, UiScreenState.entries.size)
    }

    @Test
    fun shellTokens_lightAndDarkDiffer() {
        val light = ShellPalettes.tokens(darkTheme = false)
        val dark = ShellPalettes.tokens(darkTheme = true)
        assertNotEquals(light.pageBackground, dark.pageBackground)
        assertEquals(light.topBarTitle, dark.topBarTitle)
    }
}
