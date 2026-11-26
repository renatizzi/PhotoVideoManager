package com.renatizzi.photovideomanager.data.hub

/**
 * Nomi dei 3 file dello script Setup e riconoscimento varianti MTP/Windows.
 *
 * Due naming reali osservati:
 * - `CONFIGURA (1).cmd` — numero **prima** dell'estensione (Android MediaStore tipico)
 * - `CONFIGURA.cmd (1)` — numero **dopo** il nome completo (Esplora file / MTP Windows)
 *
 * Se manca uno dei due pattern, sul telefono restano i duplicati.
 */
object SetupSetFileNames {

    val CANONICAL: List<String> = WindowsCatalogSetupScript.SETUP_SET_FILE_NAMES

    fun isCanonicalOrVariant(displayName: String, canonical: String): Boolean {
        val n = displayName.trim()
        if (n.isEmpty()) return false
        if (n.equals(canonical, ignoreCase = true)) return true
        val stem = canonical.substringBeforeLast('.', canonical)
        val ext = canonical.substringAfterLast('.', missingDelimiterValue = "")
        if (ext.isEmpty()) return false
        // stem (n).ext
        val beforeExt = Regex(
            "^${Regex.escape(stem)}\\s*\\(\\d+\\)\\.${Regex.escape(ext)}$",
            RegexOption.IGNORE_CASE,
        )
        if (beforeExt.matches(n)) return true
        // name.ext (n)  — pattern Windows MTP sul Download del telefono
        val afterFull = Regex(
            "^${Regex.escape(canonical)}\\s*\\(\\d+\\)$",
            RegexOption.IGNORE_CASE,
        )
        return afterFull.matches(n)
    }

    fun isAnySetupSetFile(displayName: String): Boolean =
        CANONICAL.any { isCanonicalOrVariant(displayName, it) }

    /**
     * Prefisso LIKE MediaStore: copre canonic, `stem (n).ext` e `stem.ext (n)`.
     * Es. CONFIGURA% matcha CONFIGURA.cmd, CONFIGURA (1).cmd, CONFIGURA.cmd (1).
     */
    fun mediaStoreLikePrefix(canonical: String): String {
        val stem = canonical.substringBeforeLast('.', canonical)
        return "$stem%"
    }
}
