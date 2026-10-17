package com.renatizzi.photovideomanager.data.hub

import java.io.File

/**
 * Esito di una scrittura del fascicolo. [verified] è vero solo dopo rilettura uguale ai byte attesi.
 * Non promette atomicità sul provider SAF: lì il chiamante usa la stessa sequenza e, se fallisce, non tocca Room.
 */
data class HubWriteReport(
    val verified: Boolean,
    val restored: Boolean,
    val unverified: Boolean,
    val message: String,
)

object HubFileWrite {
    fun writeVerified(target: File, json: String, backup: File): HubWriteReport {
        val original = if (target.isFile) runCatching { target.readText(Charsets.UTF_8) }.getOrNull() else null
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, target.name + ".tmp")
        return try {
            tmp.writeText(json, Charsets.UTF_8)
            if (tmp.readText(Charsets.UTF_8) != json) {
                tmp.delete()
                return HubWriteReport(
                    verified = false,
                    restored = true,
                    unverified = false,
                    message = "Il file temporaneo non coincide con quanto scritto",
                )
            }
            if (original != null) {
                backup.parentFile?.mkdirs()
                backup.writeText(original, Charsets.UTF_8)
                if (backup.readText(Charsets.UTF_8) != original) {
                    tmp.delete()
                    return HubWriteReport(
                        verified = false,
                        restored = false,
                        unverified = false,
                        message = "Copia originale non verificata: fascicolo non sostituito",
                    )
                }
            }
            if (!tmp.renameTo(target)) {
                target.writeText(json, Charsets.UTF_8)
                tmp.delete()
            }
            val finalText = target.readText(Charsets.UTF_8)
            if (finalText != json) {
                val restored = restore(target, original)
                if (restored) {
                    HubWriteReport(
                        verified = false,
                        restored = true,
                        unverified = false,
                        message = "Rilettura diversa: fascicolo originale ripristinato",
                    )
                } else if (original == null && target.delete()) {
                    HubWriteReport(
                        verified = false,
                        restored = false,
                        unverified = false,
                        message = "Scrittura non verificata: il file nuovo è stato rimosso",
                    )
                } else {
                    HubWriteReport(
                        verified = false,
                        restored = false,
                        unverified = true,
                        message = "Fascicolo non verificato",
                    )
                }
            } else {
                HubWriteReport(verified = true, restored = false, unverified = false, message = "Verificato")
            }
        } catch (e: Exception) {
            tmp.delete()
            val restored = restore(target, original)
            HubWriteReport(
                verified = false,
                restored = restored,
                unverified = !restored && original != null,
                message = e.message ?: "Scrittura non riuscita",
            )
        }
    }

    private fun restore(target: File, original: String?): Boolean {
        if (original == null) return false
        return runCatching {
            target.writeText(original, Charsets.UTF_8)
            target.readText(Charsets.UTF_8) == original
        }.getOrDefault(false)
    }
}
