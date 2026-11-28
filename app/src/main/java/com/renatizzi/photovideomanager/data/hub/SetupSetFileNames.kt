package com.renatizzi.photovideomanager.data.hub

/**
 * Nomi dei 3 file dello script Setup e riconoscimento varianti Windows/MTP
 * `stem (n).ext` (numero **prima** dell'estensione).
 *
 * Usato da export MediaStore e da test: se qui non matcha, sul telefono restano `(1)/(2)`.
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
        val pat = Regex(
            "^${Regex.escape(stem)}\\s*\\(\\d+\\)\\.${Regex.escape(ext)}$",
            RegexOption.IGNORE_CASE,
        )
        return pat.matches(n)
    }

    fun isAnySetupSetFile(displayName: String): Boolean =
        CANONICAL.any { isCanonicalOrVariant(displayName, it) }

    /** Prefisso LIKE sicuro per query MediaStore (stem + eventuale spazio/`(`). */
    fun mediaStoreLikePrefix(canonical: String): String {
        val stem = canonical.substringBeforeLast('.', canonical)
        return "$stem%"
    }
}
