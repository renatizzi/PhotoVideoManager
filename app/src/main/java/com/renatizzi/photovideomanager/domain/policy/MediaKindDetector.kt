package com.renatizzi.photovideomanager.domain.policy

import com.renatizzi.photovideomanager.domain.model.MediaKind

/**
 * Riconoscimento foto/video da MIME e/o estensione (censimento SAF / import).
 * Nota bis: alcuni provider espongono MIME atipici (es. application/mp4) o
 * displayName senza estensione — in quel caso si guarda anche il locator.
 */
object MediaKindDetector {
    private val PHOTO_EXT = setOf(
        "jpg", "jpeg", "jpe", "jfif", "png", "webp", "heic", "heif",
        "gif", "bmp", "dng", "tif", "tiff", "raw", "cr2", "nef", "arw",
    )
    private val VIDEO_EXT = setOf(
        "mp4", "m4v", "mov", "mkv", "webm", "avi", "3gp", "3gpp",
        "mpeg", "mpg", "wmv", "flv", "ts", "mts", "m2ts",
    )

    fun detect(
        fileName: String,
        mimeType: String?,
        locatorHint: String? = null,
    ): MediaKind? {
        mimeKind(mimeType)?.let { return it }
        extensionOf(fileName)?.let { ext ->
            when (ext) {
                in PHOTO_EXT -> return MediaKind.PHOTO
                in VIDEO_EXT -> return MediaKind.VIDEO
            }
        }
        val locatorName = locatorHint
            ?.substringAfterLast('/')
            ?.substringAfterLast(':')
            .orEmpty()
        if (locatorName.isNotBlank() && locatorName != fileName) {
            extensionOf(locatorName)?.let { ext ->
                when (ext) {
                    in PHOTO_EXT -> return MediaKind.PHOTO
                    in VIDEO_EXT -> return MediaKind.VIDEO
                }
            }
        }
        return null
    }

    private fun mimeKind(mimeType: String?): MediaKind? {
        val mime = mimeType?.lowercase()?.substringBefore(';')?.trim().orEmpty()
        if (mime.isEmpty()) return null
        if (mime.startsWith("image/")) return MediaKind.PHOTO
        if (mime.startsWith("video/")) return MediaKind.VIDEO
        // Provider atipici / container generici.
        return when (mime) {
            "application/mp4", "application/mpeg4", "application/x-mp4" -> MediaKind.VIDEO
            "application/ogg" -> MediaKind.VIDEO
            else -> null
        }
    }

    private fun extensionOf(name: String): String? {
        val base = name.substringBefore('?').substringBefore('#').trim()
        if (base.isEmpty() || !base.contains('.')) return null
        val ext = base.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return ext.takeIf { it.isNotBlank() && it.length <= 8 }
    }
}
