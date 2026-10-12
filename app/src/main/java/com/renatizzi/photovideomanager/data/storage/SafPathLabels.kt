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
    const val DEVICE_NETWORK_NAS = "Rete / NAS"
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

    /** Dispositivo / provider per un albero SAF (telefono vs Drive vs rete vs altro). */
    fun deviceLabelForTree(context: Context, treeUri: Uri, localDeviceAlias: String): String =
        when (providerOf(treeUri)) {
            SafProvider.LOCAL_STORAGE -> localDeviceAlias
            SafProvider.GOOGLE_DRIVE -> DEVICE_GOOGLE_DRIVE
            SafProvider.DOWNLOADS -> DEVICE_DOWNLOADS
            SafProvider.NETWORK_NAS -> DEVICE_NETWORK_NAS
            SafProvider.OTHER -> DEVICE_CLOUD_OTHER
        }

    fun providerOf(treeUri: Uri): SafProvider = providerOfAuthority(treeUri.authority)

    fun providerOfAuthority(authority: String?): SafProvider {
        val auth = authority.orEmpty().lowercase()
        return when {
            auth == AUTH_EXTERNAL -> SafProvider.LOCAL_STORAGE
            auth.startsWith(AUTH_DRIVE_PREFIX) -> SafProvider.GOOGLE_DRIVE
            auth == AUTH_DOWNLOADS || auth.contains("downloads") -> SafProvider.DOWNLOADS
            isNetworkAuthority(auth) -> SafProvider.NETWORK_NAS
            auth.isBlank() -> SafProvider.LOCAL_STORAGE
            else -> SafProvider.OTHER
        }
    }

    /** Provider SAF tipici di SMB/NAS / storage di rete. */
    fun isNetworkAuthority(authority: String): Boolean {
        val a = authority.lowercase()
        return a.contains("smb") ||
            a.contains("samba") ||
            a.contains("cif") ||
            a.contains("nas") ||
            a.contains("networkstorage") ||
            a.contains("network_storage") ||
            a.contains("sambadocuments") ||
            a.contains("synology") ||
            a.contains("qnap") ||
            a.contains("webdav") ||
            a.contains("share.") ||
            a.endsWith(".share") ||
            a.contains("lanstorage")
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
     * Percorso UI **senza** ripetere il nome dispositivo (quello sta su [deviceLabelForTree]).
     * Locale: es. `Memoria principale/Pictures/Camera`.
     * Drive/NAS: percorso/cartella intera, non «Google Drive/…» duplicato.
     */
    fun humanPathOrCloud(context: Context, treeUri: Uri): String = storagePathLabel(context, treeUri)

    /** Percorso storage (riga «percorso» in Acquisisci), distinto dal dispositivo. */
    fun storagePathLabel(context: Context, treeUri: Uri): String {
        humanPath(treeUri)?.let { return stripDevicePrefix(it) }
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
        val decoded = docId?.let { decode(it) }.orEmpty()
        // Alcuni provider rete/Drive usano id tipo share:path/to/folder
        if (decoded.contains(':')) {
            val after = decoded.substringAfter(':').trim('/').replace('\\', '/')
            if (after.isNotBlank() && after.contains('/')) {
                return stripDevicePrefix(after)
            }
            if (after.isNotBlank() && providerOf(treeUri) != SafProvider.LOCAL_STORAGE) {
                val folder = folderTitle(context, treeUri, after)
                // Preferisci percorso dopo ':' se leggibile; altrimenti titolo cartella.
                val path = when {
                    after.contains('/') -> after
                    !folder.equals(after, ignoreCase = true) && !looksIllegible(folder) -> folder
                    else -> after
                }
                return stripDevicePrefix(path)
            }
        } else if (decoded.contains('/') || decoded.contains('\\')) {
            return stripDevicePrefix(decoded.trim('/').replace('\\', '/'))
        }
        return stripDevicePrefix(
            folderTitle(context, treeUri, defaultFolderFallback(providerOf(treeUri))),
        )
    }

    /**
     * Rimuove un eventuale prefisso «Google Drive/» (o altri device) dal percorso,
     * così non si ripete sotto la riga dispositivo.
     */
    fun stripDevicePrefix(path: String): String {
        val trimmed = path.trim().trimStart('/')
        val prefixes = listOf(
            DEVICE_GOOGLE_DRIVE,
            DEVICE_DOWNLOADS,
            DEVICE_NETWORK_NAS,
            DEVICE_CLOUD_OTHER,
            "Disco di Rete",
            "Network Storage",
        )
        for (prefix in prefixes) {
            if (trimmed.equals(prefix, ignoreCase = true)) return trimmed
            val withSlash = "$prefix/"
            if (trimmed.startsWith(withSlash, ignoreCase = true)) {
                return trimmed.substring(withSlash.length).ifBlank { trimmed }
            }
        }
        return trimmed
    }

    /**
     * Percorso UI sotto il nome (come in Importa): niente prefisso dispositivo
     * e niente foglia finale uguale al nome in grassetto (file/cartella).
     * Esempio: nome `FilmoraGo`, path grezzo `Google Drive/FilmoraGo` → `""`
     * (il dispositivo è già sulla riga sopra); nome `MediaManager_test`,
     * path `Memoria principale/Pictures/MediaManager_test` → `Memoria principale/Pictures`.
     */
    fun pathUnderName(path: String, displayName: String, deviceLabel: String = ""): String {
        var p = stripDevicePrefix(path)
        if (deviceLabel.isNotBlank() && p.equals(deviceLabel, ignoreCase = true)) return ""
        // Ripeti strip se il path inizia ancora col device (es. dopo alias).
        if (deviceLabel.isNotBlank()) {
            val withSlash = "${deviceLabel.trim()}/"
            if (p.startsWith(withSlash, ignoreCase = true)) {
                p = p.substring(withSlash.length)
            }
        }
        val leaf = displayName.trim()
        if (leaf.isEmpty()) return p
        if (p.equals(leaf, ignoreCase = true)) return ""
        val suffix = "/$leaf"
        if (p.length > suffix.length &&
            p.regionMatches(p.length - suffix.length, suffix, 0, suffix.length, ignoreCase = true)
        ) {
            p = p.substring(0, p.length - suffix.length)
        }
        return p.trim().trim('/')
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
        SafProvider.NETWORK_NAS -> "Cartella di rete"
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
        NETWORK_NAS,
        OTHER,
    }
}
