package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupSetFileNamesTest {

    @Test
    fun recognizesWindowsMtpNumberedVariants() {
        assertTrue(SetupSetFileNames.isCanonicalOrVariant("CONFIGURA.cmd", "CONFIGURA.cmd"))
        assertTrue(SetupSetFileNames.isCanonicalOrVariant("CONFIGURA (1).cmd", "CONFIGURA.cmd"))
        assertTrue(SetupSetFileNames.isCanonicalOrVariant("CONFIGURA (12).cmd", "CONFIGURA.cmd"))
        assertTrue(SetupSetFileNames.isCanonicalOrVariant("sup (1).ps1", "sup.ps1"))
        assertTrue(SetupSetFileNames.isCanonicalOrVariant("QRCoder (2).dll", "QRCoder.dll"))
    }

    @Test
    fun rejectsWrongPatternsThatCausedPriorBugs() {
        // Pattern sbagliato storico: "CONFIGURA.cmd (1)" — non è il naming Windows.
        assertFalse(SetupSetFileNames.isCanonicalOrVariant("CONFIGURA.cmd (1)", "CONFIGURA.cmd"))
        assertFalse(SetupSetFileNames.isCanonicalOrVariant("DATI_CIFS (1).json", "CONFIGURA.cmd"))
        assertFalse(SetupSetFileNames.isCanonicalOrVariant("superduper.ps1", "sup.ps1"))
        assertFalse(SetupSetFileNames.isCanonicalOrVariant("no_pw (1).json", "sup.ps1"))
    }

    @Test
    fun isAnySetupSetFile_coversAllThree() {
        assertTrue(SetupSetFileNames.isAnySetupSetFile("CONFIGURA (3).cmd"))
        assertTrue(SetupSetFileNames.isAnySetupSetFile("sup.ps1"))
        assertTrue(SetupSetFileNames.isAnySetupSetFile("QRCoder.dll"))
        assertFalse(SetupSetFileNames.isAnySetupSetFile("DATI_CIFS.json"))
    }
}
