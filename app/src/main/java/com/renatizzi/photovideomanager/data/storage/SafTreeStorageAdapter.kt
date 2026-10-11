package com.renatizzi.photovideomanager.data.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.renatizzi.photovideomanager.domain.model.Availability
import com.renatizzi.photovideomanager.domain.model.StorageCapability
import com.renatizzi.photovideomanager.domain.port.MissingCapabilityException
import com.renatizzi.photovideomanager.domain.port.StorageAdapter
import com.renatizzi.photovideomanager.domain.port.StorageEntry
import com.renatizzi.photovideomanager.domain.port.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Adapter SAF Document Tree.
 *
 * Listing figli: sempre via [DocumentsContract.buildChildDocumentsUriUsingTree].
 * Non usare [DocumentFile.fromSingleUri] + [DocumentFile.listFiles] sulle sottocartelle:
 * produce SingleDocumentFile che lancia UnsupportedOperationException (media in
 * DCIM/Camera non venivano censiti).
 */
class SafTreeStorageAdapter(
    private val context: Context,
    private val treeUri: Uri,
) : StorageAdapter {
    override val adapterId: String = "saf-tree"

    private val caps = setOf(
        StorageCapability.LIST,
        StorageCapability.READ,
        StorageCapability.WRITE,
        StorageCapability.CREATE_DIRECTORY,
        StorageCapability.DELETE,
        StorageCapability.SEQUENTIAL_READ,
        StorageCapability.METADATA,
        StorageCapability.FINGERPRINT_STREAM,
        StorageCapability.AVAILABILITY,
    )

    override fun capabilities(): Set<StorageCapability> = caps

    override suspend fun availability(): Availability = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri)
        when {
            root == null -> Availability.UNKNOWN
            root.exists() && root.canRead() -> Availability.AVAILABLE
            else -> Availability.UNAVAILABLE
        }
    }

    override suspend fun listChildren(opaqueLocator: String): List<StorageEntry> =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.LIST)
            val parentDocId = documentIdOf(opaqueLocator) ?: return@withContext emptyList()
            val fromContract = queryChildrenViaContract(parentDocId)
            // Sempre unire DocumentFile: su NAS il cursor può omettere cartelle (es. «famiglia»)
            // anche se ne restituisce altre (es. «archivio condiviso»).
            val fromDocFile = listChildrenViaDocumentFile(opaqueLocator, parentDocId)
            mergeChildren(fromContract, fromDocFile)
        }

    private fun queryChildrenViaContract(parentDocId: String): List<StorageEntry> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
        )
        val result = mutableListOf<StorageEntry>()
        // Paginazione (API 26+): alcuni provider rete limitano la prima pagina.
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            var offset = 0
            val pageSize = 256
            val seenKeys = LinkedHashSet<String>()
            while (offset < 10_000) {
                val extras = android.os.Bundle().apply {
                    putInt(android.content.ContentResolver.QUERY_ARG_OFFSET, offset)
                    putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, pageSize)
                }
                val page = mutableListOf<StorageEntry>()
                runCatching {
                    context.contentResolver.query(childrenUri, projection, extras, null)?.use { cursor ->
                        readChildrenCursor(cursor, page)
                    }
                }
                if (page.isEmpty()) break
                var newInPage = 0
                for (entry in page) {
                    val key = entry.displayName.trim().lowercase() + "|" + entry.isDirectory + "|" +
                        entry.opaqueLocator
                    if (seenKeys.add(key)) {
                        result.add(entry)
                        newInPage++
                    }
                }
                // Provider che ignorano OFFSET: stessa pagina ripetuta → stop.
                if (newInPage == 0) break
                if (page.size < pageSize) break
                offset += pageSize
            }
            if (result.isNotEmpty()) return result
        }
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                readChildrenCursor(cursor, result)
            }
        }
        return result
    }

    private fun readChildrenCursor(
        cursor: android.database.Cursor,
        out: MutableList<StorageEntry>,
    ) {
        val idIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val nameIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
        val sizeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
        while (cursor.moveToNext()) {
            val docId = cursor.getString(idIdx) ?: continue
            val name = cursor.getString(nameIdx) ?: docId
            val mime = cursor.getString(mimeIdx)
            val size = if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else null
            val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            out.add(
                StorageEntry(
                    opaqueLocator = childUri.toString(),
                    displayName = name,
                    isDirectory = isDirectoryEntry(name, mime, size),
                    mimeType = mime,
                    byteSize = size,
                ),
            )
        }
    }

    /**
     * Alcuni provider NAS omettono MIME_TYPE_DIR sulle cartelle: senza questo
     * indizio non si scende in sottocartelle (es. «famiglia»).
     */
    private fun isDirectoryEntry(displayName: String, mime: String?, byteSize: Long?): Boolean {
        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) return true
        if (!mime.isNullOrBlank()) return false
        val name = displayName.trim()
        if (name.isEmpty() || name.startsWith('.')) return false
        // Nome con estensione media tipica → file, non cartella.
        val ext = name.substringAfterLast('.', missingDelimiterValue = "")
        if (ext.isNotEmpty() && ext.length in 2..5 && ext.all { it.isLetterOrDigit() }) {
            return false
        }
        // Cartella sospetta: niente MIME, niente size (o zero).
        return byteSize == null || byteSize == 0L
    }

    private fun mergeChildren(
        primary: List<StorageEntry>,
        secondary: List<StorageEntry>,
    ): List<StorageEntry> {
        if (secondary.isEmpty()) return primary
        if (primary.isEmpty()) return secondary
        val byKey = LinkedHashMap<String, StorageEntry>()
        fun keyOf(e: StorageEntry): String =
            e.displayName.trim().lowercase() + "|" + e.isDirectory
        primary.forEach { byKey[keyOf(it)] = it }
        secondary.forEach { e ->
            val k = keyOf(e)
            if (!byKey.containsKey(k)) byKey[k] = e
        }
        return byKey.values.toList()
    }

    /**
     * Fallback listing (utile su NAS/SMB se il cursor DocumentsContract è vuoto).
     * Root: [DocumentFile.fromTreeUri]; sottocartelle: fromSingleUri (se supportato).
     */
    private fun listChildrenViaDocumentFile(
        opaqueLocator: String,
        parentDocId: String,
    ): List<StorageEntry> {
        val isRoot = opaqueLocator.isBlank() || opaqueLocator == treeUri.toString()
        val parentFile = if (isRoot) {
            DocumentFile.fromTreeUri(context, treeUri)
        } else {
            val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId)
            // Nested: fromSingleUri può non supportare listFiles → lista vuota (ok, resta il contract).
            DocumentFile.fromSingleUri(context, docUri)
        } ?: return emptyList()
        return runCatching {
            parentFile.listFiles().map { child ->
                val name = child.name ?: child.uri.lastPathSegment.orEmpty()
                val mime = child.type
                val size = if (child.isFile) child.length().takeIf { it >= 0 } else null
                StorageEntry(
                    opaqueLocator = child.uri.toString(),
                    displayName = name,
                    isDirectory = child.isDirectory || isDirectoryEntry(name, mime, size),
                    mimeType = mime,
                    byteSize = size,
                )
            }
        }.getOrDefault(emptyList())
    }

    override suspend fun openRead(opaqueLocator: String): InputStream =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.READ)
            val uri = documentUriOf(opaqueLocator)
            context.contentResolver.openInputStream(uri)
                ?: error("Impossibile aprire in lettura: $opaqueLocator")
        }

    override suspend fun readMetadata(opaqueLocator: String): StorageMetadata? =
        withContext(Dispatchers.IO) {
            requireCapability(StorageCapability.METADATA)
            val uri = documentUriOf(opaqueLocator)
            context.contentResolver.query(
                uri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@withContext null
                val sizeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                StorageMetadata(
                    byteSize = if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) cursor.getLong(sizeIdx) else null,
                    mimeType = if (mimeIdx >= 0) cursor.getString(mimeIdx) else null,
                    lastModifiedEpochMs = if (modIdx >= 0 && !cursor.isNull(modIdx)) {
                        cursor.getLong(modIdx)
                    } else {
                        null
                    },
                )
            }
        }

    override suspend fun writeCopy(
        parentOpaqueLocator: String,
        fileName: String,
        source: InputStream,
    ): String = withContext(Dispatchers.IO) {
        requireCapability(StorageCapability.WRITE)
        val parentDocId = documentIdOf(parentOpaqueLocator)
            ?: DocumentsContract.getTreeDocumentId(treeUri)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId)
        val parent = DocumentFile.fromTreeUri(context, treeUri)?.let { root ->
            // Prefer creating under resolved parent via DocumentsContract when possible.
            DocumentFile.fromSingleUri(context, parentUri) ?: root
        } ?: error("Parent SAF non disponibile")

        // createFile on SingleDocumentFile may fail for nested dirs — use DocumentsContract.createDocument
        val safeName = fileName.replace(Regex("[\\/]+"), "_")
        val createdUri = DocumentsContract.createDocument(
            context.contentResolver,
            parentUri,
            "application/octet-stream",
            safeName,
        ) ?: run {
            // Fallback root create if parent create fails
            val root = DocumentFile.fromTreeUri(context, treeUri)
                ?: error("Root SAF non disponibile")
            val created = root.createFile("application/octet-stream", safeName)
                ?: error("Impossibile creare file SAF")
            created.uri
        }
        context.contentResolver.openOutputStream(createdUri)?.use { output: OutputStream ->
            source.use { input -> input.copyTo(output) }
        } ?: error("Impossibile aprire output SAF")
        createdUri.toString()
    }

    override suspend fun delete(opaqueLocator: String): Unit = withContext(Dispatchers.IO) {
        requireCapability(StorageCapability.DELETE)
        val uri = documentUriOf(opaqueLocator)
        val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
        require(deleted) { "Impossibile eliminare documento SAF: $opaqueLocator" }
    }

    private fun requireCapability(capability: StorageCapability) {
        if (capability !in caps) {
            throw MissingCapabilityException(capability, adapterId)
        }
    }

    private fun documentIdOf(opaqueLocator: String): String? {
        if (opaqueLocator.isBlank() || opaqueLocator == treeUri.toString()) {
            return runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
        }
        val uri = Uri.parse(opaqueLocator)
        return runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
            ?: runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
    }

    private fun documentUriOf(opaqueLocator: String): Uri {
        if (opaqueLocator.isBlank() || opaqueLocator == treeUri.toString()) {
            val treeId = DocumentsContract.getTreeDocumentId(treeUri)
            return DocumentsContract.buildDocumentUriUsingTree(treeUri, treeId)
        }
        val raw = Uri.parse(opaqueLocator)
        val docId = documentIdOf(opaqueLocator)
        return if (docId != null) {
            DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        } else {
            raw
        }
    }
}
