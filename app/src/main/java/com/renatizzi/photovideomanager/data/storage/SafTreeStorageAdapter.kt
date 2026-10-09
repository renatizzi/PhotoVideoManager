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
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
            val result = mutableListOf<StorageEntry>()
            runCatching {
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                    ),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(idIdx) ?: continue
                        val name = cursor.getString(nameIdx) ?: docId
                        val mime = cursor.getString(mimeIdx)
                        val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        result.add(
                            StorageEntry(
                                opaqueLocator = childUri.toString(),
                                displayName = name,
                                isDirectory = mime == DocumentsContract.Document.MIME_TYPE_DIR,
                                mimeType = mime,
                            ),
                        )
                    }
                }
            }
            result
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
