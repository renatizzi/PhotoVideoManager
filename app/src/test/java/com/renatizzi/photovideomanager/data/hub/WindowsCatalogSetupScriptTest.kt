package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsCatalogSetupScriptTest {

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
        val pkg = WindowsCatalogSetupScript.build(
            WindowsCatalogSetupScript.Params(
                folderPath = "C:\\MediaManager\\Catalogo",
                shareName = "MediaManagerCatalog",
                userName = "MediaManager",
                temporaryPassword = password,
            ),
        )
        assertTrue(pkg.ps1Content.contains("C:\\MediaManager\\Catalogo") ||
            pkg.ps1Content.contains("C:\\\\MediaManager\\\\Catalogo") ||
            pkg.ps1Content.contains("'C:\\MediaManager\\Catalogo'"))
        assertTrue(pkg.ps1Content.contains(password))
        assertTrue(pkg.ps1Content.contains("MediaManagerCatalog"))
        assertTrue(pkg.ps1Content.contains("#Requires -RunAsAdministrator"))
        assertTrue(pkg.ps1Content.contains("New-LocalUser") || pkg.ps1Content.contains("Set-LocalUser"))
        assertTrue(pkg.ps1Content.contains("MediaManager_setup_result.json"))
        // Result JSON object must not include a password field.
        assertFalse(pkg.ps1Content.contains("password ="))
        assertTrue(pkg.cmdContent.contains(WindowsCatalogSetupScript.PS1_FILE_NAME))
        assertTrue(pkg.cmdContent.contains("powershell"))
    }

    @Test
    fun generatePassword_hasRequiredLength() {
        val pwd = WindowsCatalogSetupScript.generatePassword(14)
        assertTrue(pwd.length == 14)
    }

    @Test
    fun package_fileNamesHaveCmdAndPs1Extensions() {
        val pkg = WindowsCatalogSetupScript.build(
            WindowsCatalogSetupScript.Params(temporaryPassword = "TempPass!23456"),
        )
        assertTrue(pkg.cmdFileName.endsWith(".cmd"))
        assertTrue(pkg.ps1FileName.endsWith(".ps1"))
        assertFalse(pkg.cmdFileName.endsWith(".txt"))
        assertFalse(pkg.ps1FileName.endsWith(".txt"))
    }
}
