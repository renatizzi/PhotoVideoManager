package com.renatizzi.photovideomanager.data.hub

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.hub.HubPackage
import com.renatizzi.photovideomanager.domain.hub.HubPackageCodec
import java.io.File
import java.nio.charset.Charset

/**
 * Lettura e scrittura del fascicolo. La lettura non crea e non modifica il file.
 * La scrittura è considerata riuscita solo dopo una rilettura uguale al documento preparato.
 * Su SAF la sostituzione non è atomica: se la rilettura fallisce si tenta di rimettere l'originale
 * e, se non si riesce, il fascicolo resta non verificato.
 */
class HubPackageIo(
    private val context: Context,
    private val localHubRoot: File,
) {
    fun read(seat: CatalogSeat): HubPackage? = when (val outcome = readOutcome(seat)) {
        is HubReadOutcome.Present -> outcome.pkg
        is HubReadOutcome.Missing -> null
        is HubReadOutcome.Invalid -> throw IllegalStateException(outcome.message)
        is HubReadOutcome.Unreachable -> null
    }

    fun readOutcome(seat: CatalogSeat): HubReadOutcome {
        val text = try {
            readRaw(seat)
        } catch (e: Exception) {
            return HubReadOutcome.Unreachable(e.message ?: "Sede non raggiungibile")
        } ?: return HubReadOutcome.Missing
        return runCatching { HubPackageCodec.parse(text) }
            .fold(
                onSuccess = { HubReadOutcome.Present(it) },
                onFailure = { HubReadOutcome.Invalid(it.message ?: "Fascicolo non leggibile") },
            )
    }

    fun writeVerified(seat: CatalogSeat, pkg: HubPackage): HubWriteReport {
        val json = HubPackageCodec.toJsonString(pkg)
        return when (seat.kind) {
            CatalogSeatKind.NONE -> HubWriteReport(false, false, false, "Sede non configurata")
            CatalogSeatKind.LOCAL_DEVICE -> HubFileWrite.writeVerified(localFile(), json, backupFile())
            CatalogSeatKind.PC_NOTEBOOK,
            CatalogSeatKind.CLOUD_DRIVE,
            CatalogSeatKind.NETWORK_NAS,
            -> writeSaf(seat, json)
        }
    }

    fun verifiedBackupText(): String? =
        backupFile().takeIf { it.isFile }?.readText(Charsets.UTF_8)

    private fun writeSaf(seat: CatalogSeat, json: String): HubWriteReport {
        val original = readRaw(seat)
        if (original != null) {
            val saved = runCatching {
                backupFile().parentFile?.mkdirs()
                backupFile().writeText(original, Charsets.UTF_8)
                backupFile().readText(Charsets.UTF_8) == original
            }.getOrDefault(false)
            if (!saved) {
                return HubWriteReport(false, false, false, "Copia originale non verificata: fascicolo non sostituito")
            }
        }
        val doc = hubDocument(seat, createIfMissing = true)
            ?: return HubWriteReport(false, false, false, "Impossibile creare il fascicolo nella sede")
        val wrote = writeDocument(doc, json)
        if (!wrote) {
            return finishFailedWrite(doc, original, seat)
        }
        val back = readRaw(seat)
        if (back != json) {
            return finishFailedWrite(doc, original, seat)
        }
        return HubWriteReport(true, false, false, "Verificato")
    }

    /**
     * SAF non garantisce una sostituzione atomica. Se la rilettura non coincide,
     * si rimette l'originale verificato. Un file nuovo e non verificato si elimina, se il provider lo consente.
     * Altrimenti il fascicolo resta non verificato e le scritture successive sono bloccate.
     */
    private fun finishFailedWrite(doc: DocumentFile, original: String?, seat: CatalogSeat): HubWriteReport {
        if (original != null) {
            val restored = writeDocument(doc, original) && readRaw(seat) == original
            return HubWriteReport(
                verified = false,
                restored = restored,
                unverified = !restored,
                message = if (restored) {
                    "Scrittura non riuscita: fascicolo originale ripristinato"
                } else {
                    "Fascicolo non verificato"
                },
            )
        }
        val removed = runCatching { doc.delete() }.getOrDefault(false)
        return HubWriteReport(
            verified = false,
            restored = false,
            unverified = !removed,
            message = if (removed) {
                "Scrittura non verificata: il file nuovo è stato rimosso"
            } else {
                "Fascicolo non verificato"
            },
        )
    }

    private fun writeDocument(doc: DocumentFile, json: String): Boolean {
        return runCatching {
            context.contentResolver.openOutputStream(doc.uri, "wt")?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: return false
            true
        }.getOrDefault(false)
    }

    private fun readRaw(seat: CatalogSeat): String? = when (seat.kind) {
        CatalogSeatKind.NONE -> null
        CatalogSeatKind.LOCAL_DEVICE -> {
            val file = localFile()
            if (!file.isFile) null else file.readText(Charsets.UTF_8)
        }
        CatalogSeatKind.PC_NOTEBOOK,
        CatalogSeatKind.CLOUD_DRIVE,
        CatalogSeatKind.NETWORK_NAS,
        -> {
            val doc = hubDocument(seat, createIfMissing = false) ?: return null
            context.contentResolver.openInputStream(doc.uri)?.use {
                it.readBytes().toString(Charset.forName("UTF-8"))
            }
        }
    }

    private fun localFile(): File =
        File(File(localHubRoot, HubPackage.RELATIVE_DIR), HubPackage.FILE_NAME)

    private fun backupFile(): File =
        File(localHubRoot, "catalog_hub.verified.bak")

    private fun hubDocument(seat: CatalogSeat, createIfMissing: Boolean): DocumentFile? {
        val uri = seat.treeUri?.let(Uri::parse) ?: return null
        val root = DocumentFile.fromTreeUri(context, uri) ?: return null
        var dir = root.findFile(HubPackage.RELATIVE_DIR)
        if (dir == null || !dir.isDirectory) {
            if (!createIfMissing) return null
            dir = root.createDirectory(HubPackage.RELATIVE_DIR) ?: return null
        }
        var file = dir.findFile(HubPackage.FILE_NAME)
        if (file == null || !file.isFile) {
            if (!createIfMissing) return null
            file = dir.createFile("application/json", HubPackage.FILE_NAME) ?: return null
        }
        return file
    }
}

sealed class HubReadOutcome {
    data class Present(val pkg: HubPackage) : HubReadOutcome()
    data object Missing : HubReadOutcome()
    data class Invalid(val message: String) : HubReadOutcome()
    data class Unreachable(val message: String) : HubReadOutcome()
}
