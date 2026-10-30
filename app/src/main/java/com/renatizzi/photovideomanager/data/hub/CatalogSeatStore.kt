package com.renatizzi.photovideomanager.data.hub

import android.content.Context
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.hub.HubDiffEngine
import org.json.JSONObject
import java.util.UUID

class CatalogSeatStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun deviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val id = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        return id
    }

    fun seat(): CatalogSeat {
        val kind = runCatching {
            CatalogSeatKind.valueOf(prefs.getString(KEY_KIND, CatalogSeatKind.NONE.name)!!)
        }.getOrDefault(CatalogSeatKind.NONE)
        val label = prefs.getString(KEY_LABEL, "").orEmpty()
        // Migrazione: se manca catalogName, riusa displayLabel se sembra un nome corto, altrimenti default.
        val storedName = prefs.getString(KEY_CATALOG_NAME, null)
        val catalogName = when {
            !storedName.isNullOrBlank() -> storedName
            label.isNotBlank() && !label.contains('\\') && !label.contains('/') && label.length <= 48 -> label
            else -> CatalogSeat.DEFAULT_CATALOG_NAME
        }
        return CatalogSeat(
            kind = kind,
            treeUri = prefs.getString(KEY_URI, null),
            displayLabel = label,
            catalogName = catalogName,
            deviceModel = prefs.getString(KEY_DEVICE_MODEL, "").orEmpty(),
            memoryLabel = prefs.getString(KEY_MEMORY, "").orEmpty(),
            configuredAtEpochMs = prefs.getLong(KEY_CONFIGURED_AT, 0L),
        )
    }

    fun saveSeat(seat: CatalogSeat) {
        prefs.edit()
            .putString(KEY_KIND, seat.kind.name)
            .putString(KEY_URI, seat.treeUri)
            .putString(KEY_LABEL, seat.displayLabel)
            .putString(KEY_CATALOG_NAME, seat.catalogName)
            .putString(KEY_DEVICE_MODEL, seat.deviceModel)
            .putString(KEY_MEMORY, seat.memoryLabel)
            .putLong(KEY_CONFIGURED_AT, seat.configuredAtEpochMs)
            .apply()
    }

    fun clearSeat() {
        saveSeat(CatalogSeat(kind = CatalogSeatKind.NONE))
    }

    fun lastSynced(): Map<String, HubDiffEngine.SyncStamp> {
        val raw = prefs.getString(KEY_SYNC, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val id = keys.next()
                    val entry = obj.getJSONObject(id)
                    put(
                        id,
                        HubDiffEngine.SyncStamp(
                            updatedAtEpochMs = entry.getLong("at"),
                            updatedByDeviceId = entry.getString("by"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun putLastSynced(stamps: Map<String, HubDiffEngine.SyncStamp>) {
        val obj = JSONObject()
        stamps.forEach { (id, stamp) ->
            obj.put(
                id,
                JSONObject()
                    .put("at", stamp.updatedAtEpochMs)
                    .put("by", stamp.updatedByDeviceId),
            )
        }
        prefs.edit().putString(KEY_SYNC, obj.toString()).apply()
    }

    companion object {
        const val PREFS = "pvm_catalog_seat"
        private const val KEY_KIND = "seat_kind"
        private const val KEY_URI = "seat_uri"
        private const val KEY_LABEL = "seat_label"
        private const val KEY_CATALOG_NAME = "seat_catalog_name"
        private const val KEY_DEVICE_MODEL = "seat_device_model"
        private const val KEY_MEMORY = "seat_memory"
        private const val KEY_CONFIGURED_AT = "seat_configured_at"
        private const val KEY_DEVICE_ID = "hub_device_id"
        private const val KEY_SYNC = "hub_last_synced"
    }
}
