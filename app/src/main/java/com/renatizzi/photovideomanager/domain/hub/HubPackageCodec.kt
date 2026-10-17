package com.renatizzi.photovideomanager.domain.hub

import org.json.JSONArray
import org.json.JSONObject

/**
 * Lettura e scrittura JSON del fascicolo.
 * Un catalogId assente resta assente: la lettura non ne genera uno.
 */
object HubPackageCodec {
    fun parse(text: String): HubPackage {
        val obj = JSONObject(text)
        val cardsJson = obj.optJSONArray("cards") ?: JSONArray()
        val cards = buildList {
            for (i in 0 until cardsJson.length()) {
                val c = cardsJson.getJSONObject(i)
                add(
                    HubCard(
                        id = c.getString("id"),
                        kind = c.getString("kind"),
                        displayTitle = c.optNullableString("displayTitle"),
                        updatedAtEpochMs = c.getLong("updatedAtEpochMs"),
                        updatedByDeviceId = c.getString("updatedByDeviceId"),
                        trashed = c.optBoolean("trashed", false),
                        sha256 = c.optNullableString("sha256"),
                    ),
                )
            }
        }
        val sourcesJson = obj.optJSONArray("sources") ?: JSONArray()
        val sources = buildList {
            for (i in 0 until sourcesJson.length()) {
                val s = sourcesJson.getJSONObject(i)
                add(
                    HubSourceDescriptor(
                        sourceKey = s.getString("sourceKey"),
                        displayName = s.optString("displayName"),
                        provider = s.optString("provider"),
                        humanPath = s.optString("humanPath"),
                        documentId = s.optNullableString("documentId"),
                        shareable = s.optBoolean("shareable", false),
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
            catalogId = obj.optNullableString("catalogId"),
            displayLabel = obj.optNullableString("displayLabel"),
            sources = sources,
        )
    }

    fun toJson(pkg: HubPackage): JSONObject {
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
        val obj = JSONObject()
            .put("formatVersion", pkg.formatVersion)
            .put("seatId", pkg.seatId)
            .put("updatedAtEpochMs", pkg.updatedAtEpochMs)
            .put("updatedByDeviceId", pkg.updatedByDeviceId)
            .put("cards", cards)
        pkg.catalogId.blankToNull()?.let { obj.put("catalogId", it) }
        pkg.displayLabel.blankToNull()?.let { obj.put("displayLabel", it) }
        if (pkg.sources.isNotEmpty()) {
            val sources = JSONArray()
            pkg.sources.forEach { s ->
                val source = JSONObject()
                    .put("sourceKey", s.sourceKey)
                    .put("displayName", s.displayName)
                    .put("provider", s.provider)
                    .put("humanPath", s.humanPath)
                    .put("shareable", s.shareable)
                s.documentId.blankToNull()?.let { source.put("documentId", it) }
                sources.put(source)
            }
            obj.put("sources", sources)
        }
        return obj
    }

    fun toJsonString(pkg: HubPackage): String = toJson(pkg).toString(2)

    /** Il JSON non deve trasportare permessi o segreti. */
    fun containsForbiddenKeys(json: String): Boolean {
        val lowered = json.lowercase()
        return forbiddenFragments.any { lowered.contains(it) }
    }

    private val forbiddenFragments = listOf(
        "content://",
        "\"uri\"",
        "password",
        "credential",
        "persistable",
    )

    private fun JSONObject.optNullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).blankToNull()
    }
}
