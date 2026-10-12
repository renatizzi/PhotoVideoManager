package com.renatizzi.photovideomanager.data.hub

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.hub.HubCard
import com.renatizzi.photovideomanager.domain.hub.HubPackage
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

class HubPackageIo(
    private val context: Context,
    private val localHubRoot: File,
) {
    fun read(seat: CatalogSeat): HubPackage? {
        val text = readRaw(seat) ?: return null
        return parse(text)
    }

    fun write(seat: CatalogSeat, pkg: HubPackage): Result<Unit> = runCatching {
        val json = toJson(pkg).toString(2)
        writeRaw(seat, json)
    }

    fun probeWritable(seat: CatalogSeat): Result<Unit> = runCatching {
        val probe = HubPackage(
            seatId = seat.treeUri ?: "local",
            updatedAtEpochMs = System.currentTimeMillis(),
            updatedByDeviceId = "probe",
            cards = emptyList(),
        )
        val existing = read(seat)
        write(seat, existing ?: probe).getOrThrow()
        if (existing == null && seat.kind == CatalogSeatKind.LOCAL_DEVICE) {
            // ok — created empty hub
        }
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

    private fun writeRaw(seat: CatalogSeat, json: String) {
        when (seat.kind) {
            CatalogSeatKind.NONE -> error("Sede non configurata")
            CatalogSeatKind.LOCAL_DEVICE -> {
                val file = localFile()
                file.parentFile?.mkdirs()
                file.writeText(json, Charsets.UTF_8)
            }
            CatalogSeatKind.PC_NOTEBOOK,
            CatalogSeatKind.CLOUD_DRIVE,
            CatalogSeatKind.NETWORK_NAS,
            -> {
                val doc = hubDocument(seat, createIfMissing = true)
                    ?: error("Impossibile creare il fascicolo nella sede")
                context.contentResolver.openOutputStream(doc.uri, "wt")?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("Scrittura sede non disponibile")
            }
        }
    }

    private fun localFile(): File =
        File(File(localHubRoot, HubPackage.RELATIVE_DIR), HubPackage.FILE_NAME)

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

    private fun parse(text: String): HubPackage {
        val obj = JSONObject(text)
        val cardsJson = obj.optJSONArray("cards") ?: JSONArray()
        val cards = buildList {
            for (i in 0 until cardsJson.length()) {
                val c = cardsJson.getJSONObject(i)
                add(
                    HubCard(
                        id = c.getString("id"),
                        kind = c.getString("kind"),
                        displayTitle = if (c.isNull("displayTitle")) {
                            null
                        } else {
                            c.optString("displayTitle").takeIf { it.isNotEmpty() }
                        },
                        updatedAtEpochMs = c.getLong("updatedAtEpochMs"),
                        updatedByDeviceId = c.getString("updatedByDeviceId"),
                        trashed = c.optBoolean("trashed", false),
                        sha256 = if (c.isNull("sha256")) {
                            null
                        } else {
                            c.optString("sha256").takeIf { it.isNotEmpty() }
                        },
                    ),
                )
            }
        }
        return HubPackage(
            formatVersion = obj.optInt("formatVersion", HubPackage.FORMAT_VERSION),
            seatId = obj.optString("seatId", ""),
            updatedAtEpochMs = obj.optLong("updatedAtEpochMs", 0L),
            updatedByDeviceId = obj.optString("updatedByDeviceId", ""),
            cards = cards,
        )
    }

    private fun toJson(pkg: HubPackage): JSONObject {
        val cards = JSONArray()
        pkg.cards.forEach { c ->
            cards.put(
                JSONObject()
                    .put("id", c.id)
                    .put("kind", c.kind)
                    .put("displayTitle", c.displayTitle)
                    .put("updatedAtEpochMs", c.updatedAtEpochMs)
                    .put("updatedByDeviceId", c.updatedByDeviceId)
                    .put("trashed", c.trashed)
                    .put("sha256", c.sha256),
            )
        }
        return JSONObject()
            .put("formatVersion", pkg.formatVersion)
            .put("seatId", pkg.seatId)
            .put("updatedAtEpochMs", pkg.updatedAtEpochMs)
            .put("updatedByDeviceId", pkg.updatedByDeviceId)
            .put("cards", cards)
    }
}
