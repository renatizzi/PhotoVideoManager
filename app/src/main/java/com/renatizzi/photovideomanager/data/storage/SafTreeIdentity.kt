package com.renatizzi.photovideomanager.data.storage

import android.net.Uri
import android.provider.DocumentsContract
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Identità stabile di un albero SAF (BL-10): evita doppie fonti quando lo stesso
 * albero torna con URI stringa diversa (encoding, form tree vs document).
 *
 * Policy: le fonti in Acquisisci nascono **solo** da «+» / OpenDocumentTree.
 * Non si importano mai le `persistedUriPermissions` di sistema nel Catalogo.
 */
object SafTreeIdentity {
    /**
     * Chiave canonica: `saf:<authority>|<treeDocumentId>` oppure URI grezzo.
     */
    fun canonicalKey(treeUri: Uri): String {
        val authority = treeUri.authority.orEmpty()
        val docId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: parseTreeDocumentId(treeUri.toString())
        return if (!docId.isNullOrBlank() && authority.isNotBlank()) {
            "saf:$authority|${decode(docId)}"
        } else {
            treeUri.toString()
        }
    }

    /** Variante testabile senza Context Android (URI content://…/tree/…). */
    fun canonicalKeyFromUriString(uriString: String): String {
        val authority = authorityOf(uriString)
        val docId = parseTreeDocumentId(uriString)
        return if (!docId.isNullOrBlank() && !authority.isNullOrBlank()) {
            "saf:$authority|${decode(docId)}"
        } else {
            uriString
        }
    }

    fun sameTree(a: String, b: String): Boolean =
        canonicalKeyFromUriString(a) == canonicalKeyFromUriString(b)

    internal fun parseTreeDocumentId(uriString: String): String? {
        val marker = "/tree/"
        val idx = uriString.indexOf(marker)
        if (idx < 0) return null
        val after = uriString.substring(idx + marker.length)
        val end = after.indexOf('/').let { if (it < 0) after.length else it }
        val raw = after.substring(0, end)
        if (raw.isBlank()) return null
        return decode(raw)
    }

    private fun authorityOf(uriString: String): String? {
        if (!uriString.startsWith("content://")) return null
        val rest = uriString.removePrefix("content://")
        return rest.substringBefore('/').takeIf { it.isNotBlank() }
    }

    private fun decode(value: String): String =
        runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrDefault(value)
}
