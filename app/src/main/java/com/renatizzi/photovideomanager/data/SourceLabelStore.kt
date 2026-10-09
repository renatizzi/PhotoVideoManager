package com.renatizzi.photovideomanager.data

import android.content.Context

/**
 * Alias utente per dispositivo e singole sorgenti (senza migrazione Room).
 */
class SourceLabelStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun deviceAlias(): String? = prefs.getString(KEY_DEVICE, null)?.trim()?.takeIf { it.isNotEmpty() }

    fun setDeviceAlias(alias: String?) {
        val clean = alias?.trim().orEmpty()
        prefs.edit().apply {
            if (clean.isEmpty()) remove(KEY_DEVICE) else putString(KEY_DEVICE, clean)
        }.apply()
    }

    fun sourceAlias(locationId: String): String? =
        prefs.getString(keySource(locationId), null)?.trim()?.takeIf { it.isNotEmpty() }

    fun setSourceAlias(locationId: String, alias: String?) {
        val clean = alias?.trim().orEmpty()
        prefs.edit().apply {
            if (clean.isEmpty()) remove(keySource(locationId)) else putString(keySource(locationId), clean)
        }.apply()
    }

    private fun keySource(locationId: String) = "source.$locationId"

    companion object {
        private const val PREFS = "pvm_source_labels"
        private const val KEY_DEVICE = "device_alias"
    }
}
