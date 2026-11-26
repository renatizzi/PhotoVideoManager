package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertFalse
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
        assertTrue(pkg.ps1Content.contains("Test-PvmSetVariantName"))
        // Anti-(1)/(2): bytes → delete all → WriteAllBytes canonic (non Copy-Item sul locked).
        assertTrue(pkg.ps1Content.contains("ReadAllBytes"))
        assertTrue(pkg.ps1Content.contains("WriteAllBytes"))
        assertTrue(pkg.ps1Content.contains("GetFileNameWithoutExtension"))
        // Entrambi i pattern MTP: stem (n).ext E name.ext (n).
        assertTrue(pkg.ps1Content.contains("patBefore"))
        assertTrue(pkg.ps1Content.contains("patAfter"))
        assertTrue(pkg.ps1Content.contains("CONFIGURA.cmd") && pkg.ps1Content.contains("QRCoder.dll"))
        assertFalse(pkg.ps1Content.contains("CONFIGURA.cmd (*)"))
    }

    @Test
    fun generatedCmd_resolvesSupNumberedVariant() {
        val pkg = WindowsCatalogSetupScript.build(
            WindowsCatalogSetupScript.Params(
                temporaryPassword = "TempPass1!",
                setupToken = "abc123token",
            ),
        )
        assertTrue(pkg.cmdContent.contains("sup (*).ps1"))
        assertTrue(pkg.cmdContent.contains(":pvm_have_sup"))
    }

    @Test
    fun dumpGeneratedPackageForSimulation() {
        val pkg = WindowsCatalogSetupScript.build(
            WindowsCatalogSetupScript.Params(
                folderPath = "C:\\MyTest",
                shareName = "MyTest",
                userName = "MyTest",
                temporaryPassword = "TempPass1!",
                setupToken = "simtoken017",
            ),
        )
        val out = java.io.File("/opt/cursor/artifacts/generated-sup.ps1")
        out.writeText(pkg.ps1Content)
        java.io.File("/opt/cursor/artifacts/generated-CONFIGURA.cmd").writeText(pkg.cmdContent)
        assertTrue(out.length() > 1000)
        assertTrue(pkg.ps1Content.contains("PSCommandPath"))
        assertTrue(pkg.ps1Content.contains("Invoke-PvmIcaclsGrant"))
        // Nessun /T diretto sul FullName del profilo (hang Renato 25/11).
        assertFalse(
            Regex("""ACL profilo utente:[\s\S]{0,120}/T""").containsMatchIn(pkg.ps1Content),
        )
    }
}
