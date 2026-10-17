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
                            trashed = entry.optBoolean("trashed", false),
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
                    .put("by", stamp.updatedByDeviceId)
                    .put("trashed", stamp.trashed),
            )
        }
        prefs.edit().putString(KEY_SYNC, obj.toString()).apply()
    }

    fun adoptedCatalogId(): String? = prefs.getString(KEY_CATALOG_ID, null)?.trim()?.takeIf { it.isNotEmpty() }

    fun setAdoptedCatalogId(catalogId: String?) {
        prefs.edit().apply {
            val clean = catalogId?.trim().orEmpty()
            if (clean.isEmpty()) remove(KEY_CATALOG_ID) else putString(KEY_CATALOG_ID, clean)
        }.apply()
    }

    fun isHubUnverified(): Boolean = prefs.getBoolean(KEY_UNVERIFIED, false)

    fun setHubUnverified(unverified: Boolean) {
        prefs.edit().putBoolean(KEY_UNVERIFIED, unverified).apply()
    }

    fun sourceKeyForLocation(locationId: String): String? =
        prefs.getString(keyLocation(locationId), null)?.trim()?.takeIf { it.isNotEmpty() }

    fun rememberSourceKey(locationId: String, sourceKey: String) {
        prefs.edit().putString(keyLocation(locationId), sourceKey).apply()
    }

    fun locationForSourceKey(sourceKey: String): String? =
        prefs.getString(keySource(sourceKey), null)?.trim()?.takeIf { it.isNotEmpty() }

    fun rememberSourceLink(sourceKey: String, locationId: String) {
        prefs.edit().putString(keySource(sourceKey), locationId).apply()
    }

    fun linkedSourceKeys(): Set<String> {
        return prefs.all.keys
            .filter { it.startsWith(PREFIX_SOURCE) }
            .map { it.removePrefix(PREFIX_SOURCE) }
            .toSet()
    }

    private fun keyLocation(locationId: String) = "$PREFIX_LOCATION$locationId"

    private fun keySource(sourceKey: String) = "$PREFIX_SOURCE$sourceKey"

    companion object {
        private const val PREFS = "pvm_catalog_seat"
        private const val KEY_KIND = "seat_kind"
        private const val KEY_URI = "seat_uri"
        private const val KEY_LABEL = "seat_label"
        private const val KEY_CONFIGURED_AT = "seat_configured_at"
        private const val KEY_DEVICE_ID = "hub_device_id"
        private const val KEY_SYNC = "hub_last_synced"
        private const val KEY_CATALOG_ID = "adopted_catalog_id"
        private const val KEY_UNVERIFIED = "hub_unverified"
        private const val PREFIX_LOCATION = "src_key."
        private const val PREFIX_SOURCE = "src_link."
    }
}
