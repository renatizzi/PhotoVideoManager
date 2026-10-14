package com.renatizzi.photovideomanager.data.hub

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class WindowsSetupExportResult(
    val cacheDir: File,
    val ps1File: File,
    val cmdFile: File,
    val downloadsSaved: Boolean,
    val usbCopiedTo: String?,
    val shareUris: List<Uri>,
)

/**
 * Writes the Windows setup package to app cache, public Downloads, and removable USB when present.
 */
class WindowsSetupScriptExporter(
    private val context: Context,
) {
    fun export(pkg: WindowsCatalogSetupScript.Package): WindowsSetupExportResult {
        val dir = File(context.cacheDir, "windows_setup").apply {
            if (exists()) deleteRecursively()
            mkdirs()
        }
        val ps1 = File(dir, pkg.ps1FileName).also { it.writeText(pkg.ps1Content, Charsets.UTF_8) }
        val cmd = File(dir, pkg.cmdFileName).also { it.writeText(pkg.cmdContent, Charsets.UTF_8) }

        val downloadsOk = saveToDownloads(ps1, cmd)
        val usbLabel = copyToRemovableUsb(ps1, cmd)

        val authority = "${context.packageName}.fileprovider"
        val uris = listOf(ps1, cmd).map { file ->
            FileProvider.getUriForFile(context, authority, file)
        }

        return WindowsSetupExportResult(
            cacheDir = dir,
            ps1File = ps1,
            cmdFile = cmd,
            downloadsSaved = downloadsOk,
            usbCopiedTo = usbLabel,
            shareUris = uris,
        )
    }

    fun shareIntent(export: WindowsSetupExportResult): Intent {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            // Avoid text/plain: some receivers append ".txt" to .cmd/.ps1 names.
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(export.shareUris))
            putExtra(
                Intent.EXTRA_SUBJECT,
                "MediaManager — script configurazione Catalogo PC",
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Esegui sul PC Windows (doppio clic) il file MediaManager_Setup_Catalogo.cmd " +
                    "e conferma Amministratore. Poi torna all'app MediaManager.",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(intent, "Invia script al PC")
    }

    private fun saveToDownloads(ps1: File, cmd: File): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                purgePreviousDownloads()
                writeMediaStore(ps1) && writeMediaStore(cmd)
            } else {
                @Suppress("DEPRECATION")
                val downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                )
                if (!downloads.exists()) downloads.mkdirs()
                purgeLegacyDownloadFiles(downloads)
                ps1.copyTo(File(downloads, ps1.name), overwrite = true)
                cmd.copyTo(File(downloads, cmd.name), overwrite = true)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun mimeFor(fileName: String): String = when {
        fileName.endsWith(".cmd", ignoreCase = true) -> "application/x-bat"
        fileName.endsWith(".ps1", ignoreCase = true) -> "application/x-powershell"
        else -> "application/octet-stream"
    }

    private fun purgePreviousDownloads() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        // Exact names and Samsung-renamed "*.cmd.txt" / "*.ps1 (n).txt" leftovers.
        resolver.delete(
            collection,
            "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
            arrayOf("MediaManager_Setup_Catalogo%"),
        )
    }

    private fun purgeLegacyDownloadFiles(downloads: File) {
        downloads.listFiles()
            ?.filter { it.name.startsWith("MediaManager_Setup_Catalogo") }
            ?.forEach { it.delete() }
    }

    private fun writeMediaStore(file: File): Boolean {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, mimeFor(file.name))
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return false
        try {
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            } ?: run {
                resolver.delete(uri, null, null)
                return false
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            return false
        }
    }

    private fun copyToRemovableUsb(ps1: File, cmd: File): String? {
        val sm = context.getSystemService(StorageManager::class.java) ?: return null
        for (volume in sm.storageVolumes) {
            if (!volume.isRemovable) continue
            if (volume.state != Environment.MEDIA_MOUNTED &&
                volume.state != Environment.MEDIA_MOUNTED_READ_ONLY
            ) {
                continue
            }
            val root = volumeDirectory(volume) ?: continue
            if (!root.exists() || !root.canWrite()) continue
            return try {
                val targetDir = File(root, "MediaManager").apply { mkdirs() }
                FileOutputStream(File(targetDir, ps1.name)).use { ps1.inputStream().copyTo(it) }
                FileOutputStream(File(targetDir, cmd.name)).use { cmd.inputStream().copyTo(it) }
                volume.getDescription(context) ?: root.absolutePath
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun volumeDirectory(volume: android.os.storage.StorageVolume): File? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return volume.directory
        }
        return try {
            val method = volume.javaClass.getMethod("getPath")
            val path = method.invoke(volume) as? String
            path?.let { File(it) }
        } catch (_: Exception) {
            null
        }
    }
}
