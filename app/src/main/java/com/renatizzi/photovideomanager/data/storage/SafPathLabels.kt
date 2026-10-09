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
 */
object SafPathLabels {
    fun deviceLabel(context: Context): String {
        val fromSettings = runCatching {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }.getOrNull()?.trim().orEmpty()
        if (fromSettings.isNotBlank()) return fromSettings
        return Build.MODEL.ifBlank { "Questo dispositivo" }
    }

    fun folderTitle(treeUri: Uri, fallback: String): String {
        val path = humanPath(treeUri) ?: return fallback.ifBlank { "Cartella" }
        return path.substringAfterLast('/').ifBlank { path }
    }

    fun humanPath(treeUri: Uri): String? {
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return decodeLastSegment(treeUri)
        return humanizeDocumentId(docId)
    }

    fun humanizeDocumentId(docId: String): String {
        val decoded = decode(docId)
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
}
