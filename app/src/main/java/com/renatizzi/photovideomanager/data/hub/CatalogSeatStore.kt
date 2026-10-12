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
        return CatalogSeat(
            kind = kind,
            treeUri = prefs.getString(KEY_URI, null),
            displayLabel = prefs.getString(KEY_LABEL, "").orEmpty(),
            configuredAtEpochMs = prefs.getLong(KEY_CONFIGURED_AT, 0L),
        )
    }

    fun saveSeat(seat: CatalogSeat) {
        prefs.edit()
            .putString(KEY_KIND, seat.kind.name)
            .putString(KEY_URI, seat.treeUri)
            .putString(KEY_LABEL, seat.displayLabel)
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
        private const val PREFS = "pvm_catalog_seat"
        private const val KEY_KIND = "seat_kind"
        private const val KEY_URI = "seat_uri"
        private const val KEY_LABEL = "seat_label"
        private const val KEY_CONFIGURED_AT = "seat_configured_at"
        private const val KEY_DEVICE_ID = "hub_device_id"
        private const val KEY_SYNC = "hub_last_synced"
    }
}
