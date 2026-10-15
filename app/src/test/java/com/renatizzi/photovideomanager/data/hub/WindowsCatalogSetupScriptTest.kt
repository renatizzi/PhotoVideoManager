package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsCatalogSetupScriptTest {

    private fun samplePkg() = WindowsCatalogSetupScript.build(
        WindowsCatalogSetupScript.Params(
            folderPath = "C:\\MediaManager\\Catalogo",
            shareName = "MediaManagerCatalog",
            userName = "MediaManager",
            temporaryPassword = "TempPass!23456",
        ),
    )

    @Test
    fun validateFolderPath_acceptsWindowsAbsolute() {
        assertNull(WindowsCatalogSetupScript.validateFolderPath("C:\\MediaManager\\Catalogo"))
        assertNull(WindowsCatalogSetupScript.validateFolderPath("D:\\Foto\\Catalogo"))
    }

    @Test
    fun validateFolderPath_rejectsInvalid() {
        assertNotNull(WindowsCatalogSetupScript.validateFolderPath(""))
        assertNotNull(WindowsCatalogSetupScript.validateFolderPath("/home/catalogo"))
        assertNotNull(WindowsCatalogSetupScript.validateFolderPath("C:/MediaManager"))
    }

    @Test
    fun build_embedsParamsAndKeepsPasswordOutOfResultJsonTemplate() {
        val password = "TempPass!23456"
        val pkg = samplePkg()
        assertTrue(pkg.ps1Content.contains("'C:\\MediaManager\\Catalogo'"))
        assertTrue(pkg.ps1Content.contains(password))
        assertTrue(pkg.ps1Content.contains("MediaManagerCatalog"))
        assertTrue(pkg.ps1Content.contains("#Requires -RunAsAdministrator"))
        assertTrue(pkg.ps1Content.contains("New-LocalUser") || pkg.ps1Content.contains("Set-LocalUser"))
        assertTrue(pkg.ps1Content.contains("MediaManager_setup_result.json"))
        assertFalse(pkg.ps1Content.contains("password ="))
        assertTrue(pkg.cmdContent.contains(WindowsCatalogSetupScript.PS1_FILE_NAME))
        assertTrue(pkg.cmdContent.contains("powershell"))
    }

    @Test
    fun build_ntfsUsesConstructorNewAndSids() {
        val ps1 = samplePkg().ps1Content
        assertTrue(ps1.contains("[System.Security.AccessControl.FileSystemAccessRule]::new("))
        assertTrue(ps1.contains("S-1-5-32-544"))
        assertTrue(ps1.contains("S-1-5-18"))
        assertTrue(ps1.contains("@FirewallAPI.dll,-28502"))
        assertFalse(
            ps1.contains(
                "New-Object System.Security.AccessControl.FileSystemAccessRule(",
            ),
        )
    }

    @Test
    fun build_cmdAutoElevates() {
        val cmd = samplePkg().cmdContent
        assertTrue(cmd.contains("net session"))
        assertTrue(cmd.contains("RunAs"))
    }

    @Test
    fun build_userFormatHintSingleBackslashAndResultOutsideShare() {
        val ps1 = samplePkg().ps1Content
        assertTrue(ps1.contains("(\"{0}\\{1}\" -f"))
        assertTrue(ps1.contains("Join-Path") && ps1.contains("ParentDir") && ps1.contains("ResultFileName"))
        assertTrue(ps1.contains("Fail-Step"))
        assertTrue(ps1.contains("Fail-Step 60") || ps1.contains("60"))
        assertTrue(ps1.contains("Tutto pronto"))
        assertTrue(ps1.contains("Remove-Item"))
        assertTrue(ps1.contains("Test-NetConnection"))
        assertTrue(ps1.contains("0.0.0.0/0"))
    }

    @Test
    fun generatePassword_hasRequiredLength() {
        val pwd = WindowsCatalogSetupScript.generatePassword(14)
        assertTrue(pwd.length == 14)
    }

    @Test
    fun package_fileNamesHaveCmdAndPs1Extensions() {
        val pkg = samplePkg()
        assertTrue(pkg.cmdFileName.endsWith(".cmd"))
        assertTrue(pkg.ps1FileName.endsWith(".ps1"))
        assertFalse(pkg.cmdFileName.endsWith(".txt"))
        assertFalse(pkg.ps1FileName.endsWith(".txt"))
    }
}
