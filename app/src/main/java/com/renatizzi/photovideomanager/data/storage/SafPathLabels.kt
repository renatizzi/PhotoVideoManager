package com.renatizzi.photovideomanager.data.storage

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.Settings
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Etichette leggibili per alberi SAF (evita doc=encoded=… in UI).
 * Distingue storage locale da provider cloud (es. Google Drive).
 */
object SafPathLabels {
    const val ROOT_FOLDER_TITLE = "Tutta la memoria"
    const val DEVICE_GOOGLE_DRIVE = "Google Drive"
    const val DEVICE_DOWNLOADS = "Download"
    const val DEVICE_CLOUD_OTHER = "Cloud / altro archivio"

    private const val AUTH_EXTERNAL = "com.android.externalstorage.documents"
    private const val AUTH_DOWNLOADS = "com.android.providers.downloads.documents"
    private const val AUTH_DRIVE_PREFIX = "com.google.android.apps.docs"

    fun deviceLabel(context: Context): String {
        val fromSettings = runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.trim().orEmpty()
        if (fromSettings.isNotBlank()) return fromSettings
        return Build.MODEL.ifBlank { "Questo dispositivo" }
    }

    /** Dispositivo / provider per un albero SAF (telefono vs Drive vs altro). */
    fun deviceLabelForTree(context: Context, treeUri: Uri, localDeviceAlias: String): String =
        when (providerOf(treeUri)) {
            SafProvider.LOCAL_STORAGE -> localDeviceAlias
            SafProvider.GOOGLE_DRIVE -> DEVICE_GOOGLE_DRIVE
            SafProvider.DOWNLOADS -> DEVICE_DOWNLOADS
            SafProvider.OTHER -> DEVICE_CLOUD_OTHER
        }

    fun providerOf(treeUri: Uri): SafProvider = providerOfAuthority(treeUri.authority)

    fun providerOfAuthority(authority: String?): SafProvider {
        val auth = authority.orEmpty()
        return when {
            auth == AUTH_EXTERNAL -> SafProvider.LOCAL_STORAGE
            auth.startsWith(AUTH_DRIVE_PREFIX) -> SafProvider.GOOGLE_DRIVE
            auth == AUTH_DOWNLOADS || auth.contains("downloads", ignoreCase = true) ->
                SafProvider.DOWNLOADS
            auth.isBlank() -> SafProvider.LOCAL_STORAGE
            else -> SafProvider.OTHER
        }
    }

    /**
     * Titolo cartella per UI. Per cloud usa il DISPLAY_NAME del documento albero
     * (es. test2_mediamanager su Drive) invece di inventare «Tutta la memoria».
     */
    fun folderTitle(context: Context, treeUri: Uri, fallback: String): String {
        queryTreeDisplayName(context, treeUri)?.let { name ->
            if (name.isNotBlank() && !looksIllegible(name)) return name
        }
        return folderTitleFromPath(treeUri, fallback)
    }

    /** Variante senza Context (test / path già noti). */
    fun folderTitleFromPath(treeUri: Uri, fallback: String): String {
        val provider = providerOf(treeUri)
        val path = humanPath(treeUri) ?: return fallback.ifBlank { defaultFolderFallback(provider) }
        if (provider != SafProvider.LOCAL_STORAGE) {
            // Path opachi cloud: non interpretare come radice «Tutta la memoria».
            val leaf = path.substringAfterLast('/').ifBlank { path }
            return if (leaf.equals("Memoria principale", ignoreCase = true) ||
                leaf.equals(ROOT_FOLDER_TITLE, ignoreCase = true)
            ) {
                fallback.ifBlank { defaultFolderFallback(provider) }
            } else {
                leaf
            }
        }
        val relative = path.substringAfter('/', missingDelimiterValue = "")
        return if (relative.isBlank()) {
            ROOT_FOLDER_TITLE
        } else {
            relative.substringAfterLast('/').ifBlank { relative }
        }
    }

    fun humanPath(treeUri: Uri): String? {
        val provider = providerOf(treeUri)
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return decodeLastSegment(treeUri)
        if (provider != SafProvider.LOCAL_STORAGE) {
            // DocumentId Drive/cloud è opaco: non mapparlo su «Memoria principale».
            return null
        }
        return humanizeDocumentId(docId)
    }

    /**
     * Percorso UI: locale humanizzato, cloud = «Google Drive/nomeCartella».
     */
    fun humanPathOrCloud(context: Context, treeUri: Uri): String {
        humanPath(treeUri)?.let { return it }
        val folder = folderTitle(context, treeUri, defaultFolderFallback(providerOf(treeUri)))
        return when (providerOf(treeUri)) {
            SafProvider.GOOGLE_DRIVE -> "$DEVICE_GOOGLE_DRIVE/$folder"
            SafProvider.DOWNLOADS -> "$DEVICE_DOWNLOADS/$folder"
            SafProvider.OTHER -> "$DEVICE_CLOUD_OTHER/$folder"
            SafProvider.LOCAL_STORAGE -> folder
        }
    }

    fun humanizeDocumentId(docId: String): String {
        val decoded = decode(docId)
        if (!decoded.contains(':')) {
            // Non è un id volume:path (tipico Drive) → non inventare Memoria principale.
            return decoded.ifBlank { docId }
        }
        val volume = decoded.substringBefore(':', missingDelimiterValue = "")
        val path = decoded.substringAfter(':', missingDelimiterValue = "")
            .trim('/')
            .replace('\\', '/')
        val volumeLabel = volumeLabel(volume)
        return when {
            path.isBlank() && volumeLabel.isNotBlank() -> volumeLabel
            path.isBlank() -> decoded.ifBlank { docId }
            volumeLabel.isBlank() -> path
            else -> "$volumeLabel/$path"
        }
    }

    fun volumeLabel(volume: String): String = when (volume.lowercase()) {
        "", "primary" -> "Memoria principale"
        "home" -> "Memoria principale"
        else -> when {
            volume.startsWith("sdcard", ignoreCase = true) -> "Scheda SD"
            volume.length >= 4 && volume.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' || it == '-' } ->
                "Scheda SD / memoria secondaria"
            else -> volume
        }
    }

    fun queryTreeDisplayName(context: Context, treeUri: Uri): String? {
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return null
        val docUri = runCatching {
            DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        }.getOrNull() ?: return null
        return runCatching {
            context.contentResolver.query(
                docUri,
                arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val idx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                if (idx < 0) null else cursor.getString(idx)?.trim()?.takeIf { it.isNotEmpty() }
            }
        }.getOrNull()
    }

    fun looksIllegible(name: String): Boolean {
        if (name.isBlank()) return true
        val lower = name.lowercase()
        return lower.contains("encoded=") ||
            lower.startsWith("acc=") ||
            lower.contains("doc=") ||
            (name.length > 40 && !name.contains('/') && name.count { it == '-' || it == '_' } > 4)
    }

    private fun defaultFolderFallback(provider: SafProvider): String = when (provider) {
        SafProvider.GOOGLE_DRIVE -> "Cartella Drive"
        SafProvider.DOWNLOADS -> "Download"
        SafProvider.OTHER -> "Cartella cloud"
        SafProvider.LOCAL_STORAGE -> "Cartella"
    }

    private fun decodeLastSegment(uri: Uri): String? {
        val last = uri.lastPathSegment ?: return null
        if (last.contains("encoded=", ignoreCase = true) || last.startsWith("acc=")) {
            return null
        }
        return humanizeDocumentId(last)
    }

    private fun decode(value: String): String =
        runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)

    enum class SafProvider {
        LOCAL_STORAGE,
        GOOGLE_DRIVE,
        DOWNLOADS,
        OTHER,
    }
}
