package com.renatizzi.photovideomanager.data.hub

import android.content.ContentUris
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
    val qrCoderDllFile: File?,
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
        val ps1 = File(dir, pkg.ps1FileName).also {
            it.writeBytes(WindowsCatalogSetupScript.utf8BomBytes(pkg.ps1Content))
        }
        val cmd = File(dir, pkg.cmdFileName).also {
            // CMD: ASCII + CRLF, no BOM (BOM breaks "@echo off").
            it.writeBytes(WindowsCatalogSetupScript.asciiCrlfNoBomBytes(pkg.cmdContent))
        }
        val qrDll = copyQrCoderDll(dir)

        val downloadsOk = saveToDownloads(ps1, cmd, qrDll)
        val usbLabel = copyToRemovableUsb(ps1, cmd, qrDll)

        val authority = "${context.packageName}.fileprovider"
        val files = listOfNotNull(ps1, cmd, qrDll)
        val uris = files.map { file ->
            FileProvider.getUriForFile(context, authority, file)
        }

        return WindowsSetupExportResult(
            cacheDir = dir,
            ps1File = ps1,
            cmdFile = cmd,
            qrCoderDllFile = qrDll,
            downloadsSaved = downloadsOk,
            usbCopiedTo = usbLabel,
            shareUris = uris,
        )
    }

    private fun copyQrCoderDll(dir: File): File? = try {
        val out = File(dir, WindowsCatalogSetupScript.QRCODER_DLL_FILE_NAME)
        context.assets.open("windows_setup/${WindowsCatalogSetupScript.QRCODER_DLL_FILE_NAME}").use { input ->
            FileOutputStream(out).use { output -> input.copyTo(output) }
        }
        out.takeIf { it.isFile && it.length() > 0L }
    } catch (_: Exception) {
        null
    }

    /**
     * Piano B: bundle completo (con password) su chiavetta / Download.
     * Preferisce `DATI_CIFS.json`, poi legacy `MediaManager_cifs_bundle*.json` (più recente).
     */
    fun findCifsBundle(expectedToken: String): SetupCifsBundle? {
        val files = mutableListOf<File>()
        fun collectFromDir(dir: File?) {
            if (dir == null || !dir.isDirectory) return
            dir.listFiles()
                ?.filter { it.isFile && SetupCifsBundle.isBundleFileName(it.name) }
                ?.let { files += it }
        }
        try {
            @Suppress("DEPRECATION")
            collectFromDir(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            )
        } catch (_: Exception) {
        }
        try {
            val sm = context.getSystemService(StorageManager::class.java)
            sm?.storageVolumes?.forEach { volume ->
                if (!volume.isRemovable) return@forEach
                val root = volumeDirectory(volume) ?: return@forEach
                collectFromDir(root)
                collectFromDir(File(root, "MediaManager"))
            }
        } catch (_: Exception) {
        }
        files.sortedWith(
            compareByDescending<File> {
                it.name.equals(SetupCifsBundle.BUNDLE_FILE_NAME, ignoreCase = true)
            }.thenByDescending { it.lastModified() },
        ).forEach { file ->
            val text = runCatching { file.readText(Charsets.UTF_8) }.getOrNull() ?: return@forEach
            val bundle = SetupCifsBundle.parse(text, expectedToken) ?: return@forEach
            if (bundle.isComplete) return bundle
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val patterns = listOf(
                    SetupCifsBundle.BUNDLE_FILE_NAME,
                    "DATI_CIFS%",
                    "${SetupCifsBundle.LEGACY_BUNDLE_FILE_PREFIX}%",
                )
                for (pattern in patterns) {
                    val selection = if (pattern.contains('%')) {
                        "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?"
                    } else {
                        "${MediaStore.Downloads.DISPLAY_NAME} = ?"
                    }
                    resolver.query(
                        collection,
                        arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME),
                        selection,
                        arrayOf(pattern),
                        "${MediaStore.Downloads.DATE_MODIFIED} DESC",
                    )?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val display = cursor.getString(1).orEmpty()
                            if (!SetupCifsBundle.isBundleFileName(display)) continue
                            val id = cursor.getLong(0)
                            val uri = ContentUris.withAppendedId(collection, id)
                            resolver.openInputStream(uri)?.use { input ->
                                val text = input.bufferedReader(Charsets.UTF_8).readText()
                                val bundle = SetupCifsBundle.parse(text, expectedToken)
                                if (bundle != null && bundle.isComplete) return bundle
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    fun findSetupResultInDownloads(): WindowsSetupResult? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                readResultFromMediaStore()
            } else {
                @Suppress("DEPRECATION")
                val downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                )
                for (name in listOf(
                    WindowsCatalogSetupScript.RESULT_FILE_NAME,
                    WindowsCatalogSetupScript.LEGACY_RESULT_FILE_NAME,
                )) {
                    val file = File(downloads, name)
                    if (!file.isFile) continue
                    val parsed = WindowsSetupResultParser.parse(file.readText(Charsets.UTF_8))
                    if (parsed != null) return parsed
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readResultFromMediaStore(): WindowsSetupResult? {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        for (name in listOf(
            WindowsCatalogSetupScript.RESULT_FILE_NAME,
            WindowsCatalogSetupScript.LEGACY_RESULT_FILE_NAME,
        )) {
            resolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                arrayOf(name),
                "${MediaStore.Downloads.DATE_MODIFIED} DESC",
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use
                val id = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(collection, id)
                resolver.openInputStream(uri)?.use { input ->
                    val text = input.bufferedReader(Charsets.UTF_8).readText()
                    return WindowsSetupResultParser.parse(text)
                }
            }
        }
        return null
    }

    /**
     * Removes generated setup scripts from public Downloads after successful PC setup.
     * The .ps1 still contains the temporary password until deleted.
     */
    fun scrubDownloadsScripts(): Int {
        var removed = 0
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                for (prefix in WindowsCatalogSetupScript.DOWNLOAD_SCRUB_PREFIXES) {
                    removed += resolver.delete(
                        collection,
                        "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                        arrayOf("$prefix%"),
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                )
                downloads.listFiles()
                    ?.filter { file ->
                        WindowsCatalogSetupScript.DOWNLOAD_SCRUB_PREFIXES.any {
                            file.name.startsWith(it, ignoreCase = true)
                        }
                    }
                    ?.forEach {
                        if (it.delete()) removed++
                    }
            }
        } catch (_: Exception) {
        }
        runCatching {
            File(context.cacheDir, "windows_setup").deleteRecursively()
        }
        return removed
    }

    /**
     * Rimuove fantasma MTP `DATI_CIFS.json` a 0 byte / entry 1970 da MediaStore
     * (non tocca bundle validi con contenuto).
     */
    fun scrubEmptyDatiCifsGhosts(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0
        var removed = 0
        runCatching {
            val resolver = context.contentResolver
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            resolver.query(
                collection,
                arrayOf(
                    MediaStore.Downloads._ID,
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.SIZE,
                ),
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                arrayOf("DATI_CIFS%"),
                null,
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol).orEmpty()
                    if (!SetupCifsBundle.isBundleFileName(name)) continue
                    val size = cursor.getLong(sizeCol)
                    if (size > 0L) continue
                    val uri = ContentUris.withAppendedId(collection, cursor.getLong(idCol))
                    removed += resolver.delete(uri, null, null)
                }
            }
        }
        return removed
    }

    fun shareIntent(export: WindowsSetupExportResult): Intent {
        return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(export.shareUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(
                Intent.EXTRA_TEXT,
                "Copia sul PC: CONFIGURA, sup e QRCoder.dll insieme. " +
                    "Doppio clic su CONFIGURA (non su sup). A «Tutto pronto» scansiona il QR in MediaManager.",
            )
        }.let { Intent.createChooser(it, "Invia script al PC") }
    }

    private fun saveToDownloads(ps1: File, cmd: File, qrDll: File?): Boolean {
        return try {
            // Pulisci solo fantasmi DATI_CIFS vuoti; non cancellare bundle validi in export.
            scrubEmptyDatiCifsGhosts()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 1) Cancella TUTTI i canonici + (1)/(2) del set. 2) Insert unico.
                // Mai insert se resta un omonimo: MediaStore rinomina in "name (1).ext".
                for (name in SetupSetFileNames.CANONICAL) {
                    purgeAllSetupSetEntries(name)
                }
                val dllOk = qrDll == null || insertMediaStoreNew(qrDll)
                insertMediaStoreNew(ps1) && insertMediaStoreNew(cmd) && dllOk
            } else {
                @Suppress("DEPRECATION")
                val downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                )
                if (!downloads.exists()) downloads.mkdirs()
                purgeLegacySetupSetOnly(downloads)
                ps1.copyTo(File(downloads, ps1.name), overwrite = true)
                cmd.copyTo(File(downloads, cmd.name), overwrite = true)
                qrDll?.copyTo(File(downloads, qrDll.name), overwrite = true)
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

    /**
     * Elimina canonic + ogni `stem (n).ext` del file set in MediaStore Downloads.
     * Query ampia + filtro Kotlin: LIKE SQL da solo non basta (case/spazi/pending).
     */
    private fun purgeAllSetupSetEntries(canonical: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val ids = mutableListOf<Long>()
        runCatching {
            resolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME),
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                arrayOf(SetupSetFileNames.mediaStoreLikePrefix(canonical)),
                null,
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx).orEmpty()
                    if (SetupSetFileNames.isCanonicalOrVariant(name, canonical)) {
                        ids += cursor.getLong(idIdx)
                    }
                }
            }
        }
        // Anche match esatto (alcuni OEM non espongono bene il LIKE).
        runCatching {
            resolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                arrayOf(canonical),
                null,
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    ids += cursor.getLong(0)
                }
            }
        }
        for (id in ids.distinct()) {
            runCatching {
                resolver.delete(ContentUris.withAppendedId(collection, id), null, null)
            }
        }
    }

    private fun purgeLegacySetupSetOnly(downloads: File) {
        downloads.listFiles()
            ?.filter { SetupSetFileNames.isAnySetupSetFile(it.name) }
            ?.forEach { it.delete() }
    }

    private fun insertMediaStoreNew(file: File): Boolean {
        // Difesa: ripulisci di nuovo subito prima dell'insert (race / OEM).
        purgeAllSetupSetEntries(file.name)
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, mimeFor(file.name))
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return false
        return try {
            resolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            } ?: run {
                resolver.delete(uri, null, null)
                return false
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            // Se l'OEM ha comunque creato un (1), ripulisci i non-canonici lasciando il canonic.
            purgeNumberedVariantsOnly(file.name)
            true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    /** Dopo insert: elimina solo `stem (n).ext`, non il canonic. */
    private fun purgeNumberedVariantsOnly(canonical: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        runCatching {
            resolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME),
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ?",
                arrayOf(SetupSetFileNames.mediaStoreLikePrefix(canonical)),
                null,
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx).orEmpty()
                    if (name.equals(canonical, ignoreCase = true)) continue
                    if (SetupSetFileNames.isCanonicalOrVariant(name, canonical)) {
                        resolver.delete(
                            ContentUris.withAppendedId(collection, cursor.getLong(idIdx)),
                            null,
                            null,
                        )
                    }
                }
            }
        }
    }

    private fun copyToRemovableUsb(ps1: File, cmd: File, qrDll: File?): String? {
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
                listOfNotNull(ps1, cmd, qrDll).forEach { src ->
                    targetDir.listFiles()
                        ?.filter { SetupSetFileNames.isCanonicalOrVariant(it.name, src.name) }
                        ?.forEach { it.delete() }
                    FileOutputStream(File(targetDir, src.name)).use { src.inputStream().copyTo(it) }
                }
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
