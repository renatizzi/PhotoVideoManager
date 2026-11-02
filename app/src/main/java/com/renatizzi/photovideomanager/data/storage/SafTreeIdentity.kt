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
        // Preferisci documentId se presente (sottocartella tree/…/document/…),
        // altrimenti treeDocumentId (root OpenDocumentTree).
        val docId = runCatching { DocumentsContract.getDocumentId(treeUri) }.getOrNull()
            ?: runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: parseDocumentId(treeUri.toString())
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
        val docId = parseDocumentId(uriString)
            ?: parseTreeDocumentId(uriString)
        return if (!docId.isNullOrBlank() && !authority.isNullOrBlank()) {
            "saf:$authority|${decode(docId)}"
        } else {
            uriString
        }
    }

    fun sameTree(a: String, b: String): Boolean =
        canonicalKeyFromUriString(a) == canonicalKeyFromUriString(b)

    /**
     * Normalizza un URI documento/albero in tree URI sul documentId più specifico
     * (utile per confronti; non usare come root adapter se manca il permesso sul figlio).
     */
    fun normalizeToTreeUri(uri: Uri): Uri {
        val authority = uri.authority ?: return uri
        val docId = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
            ?: runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            ?: parseDocumentId(uri.toString())
            ?: parseTreeDocumentId(uri.toString())
            ?: return uri
        return DocumentsContract.buildTreeDocumentUri(authority, docId)
    }

    fun normalizeToTreeUriString(uriString: String): String =
        runCatching { normalizeToTreeUri(Uri.parse(uriString)).toString() }
            .getOrDefault(uriString)

    /**
     * Tree URI da passare a [SafTreeStorageAdapter]: se l’opaqueLocator è
     * `…/tree/<parent>/document/<child>` (sottocartella espansa), usa il tree
     * **genitore** (su cui c’è takePersistable). Altrimenti normalizza l’URI.
     */
    fun adapterTreeUri(uri: Uri): Uri {
        val raw = uri.toString()
        if (raw.contains("/tree/") && raw.contains("/document/")) {
            val authority = uri.authority ?: return uri
            val parentTreeId = parseTreeDocumentId(raw) ?: return normalizeToTreeUri(uri)
            return DocumentsContract.buildTreeDocumentUri(authority, parentTreeId)
        }
        return normalizeToTreeUri(uri)
    }

    /**
     * True se il volume SAF non è `primary:` (memoria Secondaria → «SD» in UI).
     */
    fun isSecondaryVolume(uriString: String): Boolean {
        val docId = parseTreeDocumentId(uriString)
            ?: parseDocumentId(uriString)
            ?: return false
        val decoded = decode(docId)
        return decoded.isNotBlank() && !decoded.startsWith("primary:", ignoreCase = true)
    }

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

    /** DocumentId da URI `…/document/<id>` (anche sotto tree). */
    internal fun parseDocumentId(uriString: String): String? {
        val marker = "/document/"
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
