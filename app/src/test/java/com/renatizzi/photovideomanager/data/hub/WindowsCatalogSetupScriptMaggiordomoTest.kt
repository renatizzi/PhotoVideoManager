package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsCatalogSetupScriptMaggiordomoTest {
    @Test
    fun generatedPs1_containsMaggiordomoAndDuplicateCleanup() {
        val pkg = WindowsCatalogSetupScript.build(
            WindowsCatalogSetupScript.Params(
                temporaryPassword = "TempPass1!",
                setupToken = "abc123token",
            ),
        )
        assertTrue(pkg.ps1Content.contains("Invoke-PvmMaggiordomo"))
        assertTrue(pkg.ps1Content.contains("DATI_CIFS"))
        assertTrue(pkg.ps1Content.contains("\\([0-9]+\\)") || pkg.ps1Content.contains("([0-9]+)"))
        assertTrue(pkg.ps1Content.contains("Bluetooth Exchange Folder") || pkg.ps1Content.contains("File ricevuti"))
        // Canonici da PSScriptRoot con Copy-Item -Force su Desktop/Download (overwrite).
        assertTrue(pkg.ps1Content.contains("Copy-Item") && pkg.ps1Content.contains("-Force"))
        assertTrue(pkg.ps1Content.contains("CONFIGURA.cmd") && pkg.ps1Content.contains("QRCoder.dll"))
    }
}
