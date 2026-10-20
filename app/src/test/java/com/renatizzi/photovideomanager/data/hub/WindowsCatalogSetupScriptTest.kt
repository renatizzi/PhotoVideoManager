package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
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
    fun validateFolderPath_requireDriveC_rejectsOtherLetters() {
        assertNull(
            WindowsCatalogSetupScript.validateFolderPath(
                "C:\\MediaManager\\Catalogo",
                requireDriveC = true,
            ),
        )
        assertNotNull(
            WindowsCatalogSetupScript.validateFolderPath(
                "D:\\Foto\\Catalogo",
                requireDriveC = true,
            ),
        )
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
        assertEquals("CONFIGURA.cmd", pkg.cmdFileName)
        assertEquals("sup.ps1", pkg.ps1FileName)
        assertFalse(pkg.cmdFileName.substringBeforeLast('.') == pkg.ps1FileName.substringBeforeLast('.'))
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
    fun build_shareRepointsWhenFolderPathChanges() {
        val ps1 = samplePkg().ps1Content
        assertTrue(ps1.contains("Remove-SmbShare"))
        assertTrue(ps1.contains("ripruntata"))
        assertTrue(ps1.contains("-ieq"))
        assertFalse(ps1.contains("Rimuovila o scegli un altro nome"))
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

    @Test
    fun build_deliverablesAreAsciiOnly() {
        val pkg = samplePkg()
        assertTrue(pkg.ps1Content.all { it.code <= 127 })
        assertTrue(pkg.cmdContent.all { it.code <= 127 })
    }

    @Test
    fun utf8BomBytes_startsWithBomAndAsciiBody() {
        val pkg = samplePkg()
        val bytes = WindowsCatalogSetupScript.utf8BomBytes(pkg.ps1Content)
        assertTrue(bytes.size >= 3)
        assertTrue(bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte())
        val body = bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
        assertTrue(body.all { it.code <= 127 })
        assertTrue(body.contains("Tutto pronto"))
    }

    @Test
    fun build_userFormatHintHasSingleBackslashInFormatString() {
        val ps1 = samplePkg().ps1Content
        // PowerShell does not escape backslash; format must be "{0}\{1}" not "{0}\\{1}".
        assertTrue(ps1.contains("(\"{0}\\{1}\" -f"))
        assertFalse(ps1.contains("(\"{0}\\\\{1}\" -f"))
    }

    @Test
    fun build_passwordNotWrittenViaWriteLogTemplates() {
        val ps1 = samplePkg().ps1Content
        val logLines = ps1.lines().filter { it.contains("Write-Log") || it.contains("Write-Step") }
        assertTrue(logLines.none { it.contains("PlainPassword") && !it.contains("ConvertTo-SecureString") })
    }

    @Test
    fun cmdBytes_asciiCrlfNoBom_ps1HasBom() {
        val pkg = samplePkg()
        val cmd = WindowsCatalogSetupScript.asciiCrlfNoBomBytes(pkg.cmdContent)
        val ps1 = WindowsCatalogSetupScript.utf8BomBytes(pkg.ps1Content)
        assertTrue(cmd.isNotEmpty() && cmd[0] == '@'.code.toByte())
        assertFalse(cmd.size >= 3 && cmd[0] == 0xEF.toByte() && cmd[1] == 0xBB.toByte())
        assertTrue(String(cmd, Charsets.US_ASCII).contains("\r\n"))
        assertTrue(ps1[0] == 0xEF.toByte() && ps1[1] == 0xBB.toByte() && ps1[2] == 0xBF.toByte())
    }

    @Test
    fun build_uncLineUsesChar92NotStackedEscapes() {
        val ps1 = samplePkg().ps1Content
        assertTrue(ps1.contains("[char]92"))
        assertFalse(ps1.contains("Condivisione:  \\\\{0}"))
    }

    @Test
    fun emitScriptsForCi() {
        val pkg = samplePkg()
        val out = java.io.File("build/windows_setup_ci").apply {
            deleteRecursively()
            mkdirs()
        }
        java.io.File(out, pkg.ps1FileName)
            .writeBytes(WindowsCatalogSetupScript.utf8BomBytes(pkg.ps1Content))
        java.io.File(out, pkg.cmdFileName)
            .writeBytes(WindowsCatalogSetupScript.asciiCrlfNoBomBytes(pkg.cmdContent))
        assertTrue(java.io.File(out, pkg.ps1FileName).exists())
        assertTrue(java.io.File(out, pkg.cmdFileName).exists())
    }
}
