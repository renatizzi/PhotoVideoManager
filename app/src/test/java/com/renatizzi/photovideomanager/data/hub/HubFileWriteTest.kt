package com.renatizzi.photovideomanager.data.hub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HubFileWriteTest {

    @Test
    fun verifiedWriteKeepsAReadableBackupOfThePreviousBytes() {
        val dir = File.createTempFile("hub", "").apply {
            delete()
            mkdirs()
        }
        val target = File(dir, "catalog_hub.json")
        val backup = File(dir, "catalog_hub.verified.bak")
        target.writeText("originale", Charsets.UTF_8)
        val report = HubFileWrite.writeVerified(target, "nuovo", backup)
        assertTrue(report.verified)
        assertFalse(report.unverified)
        assertEquals("nuovo", target.readText(Charsets.UTF_8))
        assertEquals("originale", backup.readText(Charsets.UTF_8))
        dir.deleteRecursively()
    }

    @Test
    fun failedBackupLeavesTheOriginalFileUntouched() {
        val dir = File.createTempFile("hub", "").apply {
            delete()
            mkdirs()
        }
        val target = File(dir, "catalog_hub.json")
        target.writeText("originale", Charsets.UTF_8)
        val backup = File(dir, "not-a-file")
        backup.mkdirs()
        val report = HubFileWrite.writeVerified(target, "nuovo", backup)
        assertFalse(report.verified)
        assertFalse(report.unverified)
        assertEquals("originale", target.readText(Charsets.UTF_8))
        assertTrue(report.message.isNotBlank())
        dir.deleteRecursively()
    }
}
