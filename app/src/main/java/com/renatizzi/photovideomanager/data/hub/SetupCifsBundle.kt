package com.renatizzi.photovideomanager.data.hub

/**
 * Bundle completo post «Tutto pronto» per la scheda CIFS (solo Copia).
 * Contratto: [docs/LOCK_SETUP_BT_BUNDLE_CIFS.md].
 */
data class SetupCifsBundle(
    val token: String,
    val hostIp: String,
    val userQualified: String,
    val password: String,
    val shareName: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    val computerName: String = "",
    val port: String = DEFAULT_PORT,
    val folder: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    val connectionName: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    val storage: String = STORAGE_VALUE,
) {
    /** Gate duro LOCK: niente scheda Copia parziale. */
    val isComplete: Boolean
        get() {
            val portOk = port.trim() == DEFAULT_PORT || port.trim().toIntOrNull() == 445
            val storageOk = storage.isBlank() ||
                storage.trim().equals(STORAGE_VALUE, ignoreCase = true) ||
                storage.contains("SMB2", ignoreCase = true)
            return isIpv4(hostIp) &&
                userQualified.isNotBlank() &&
                userQualified.contains('\\') &&
                password.isNotBlank() &&
                portOk &&
                folder.isNotBlank() &&
                connectionName.isNotBlank() &&
                storageOk
        }

    companion object {
        const val DEFAULT_PORT = "445"
        const val BUNDLE_FILE_NAME = "MediaManager_cifs_bundle.json"
        const val STORAGE_VALUE = "SMB2,3 (jCIFS NG)"
        private val IPV4 =
            Regex("""^(?:(?:25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(?:25[0-5]|2[0-4]\d|[01]?\d\d?)$""")

        fun isIpv4(value: String): Boolean = IPV4.matches(value.trim())

        fun newSessionToken(): String =
            java.util.UUID.randomUUID().toString().replace("-", "")

        /**
         * Parse JSON bundle (chiavi lunghe PowerShell o compatte).
         * [expectedToken] se non vuoto deve combaciare.
         */
        fun parse(raw: String, expectedToken: String? = null): SetupCifsBundle? {
            val text = raw.trim().removePrefix("\uFEFF")
            if (text.isEmpty() || !text.startsWith("{")) return null
            val token = extractString(text, "token", "t").orEmpty()
            if (!expectedToken.isNullOrBlank() && token != expectedToken) return null
            val host = extractString(text, "hostIp", "ip").orEmpty()
            val user = extractString(text, "userQualified", "user").orEmpty()
            val password = extractString(text, "password", "pwd").orEmpty()
            val share = extractString(text, "shareName", "share")
                ?.ifBlank { null }
                ?: WindowsCatalogSetupScript.DEFAULT_SHARE
            val pc = extractString(text, "computerName", "pc").orEmpty()
            val port = extractString(text, "port")?.ifBlank { null } ?: DEFAULT_PORT
            val folder = extractString(text, "folder")?.ifBlank { null } ?: share
            val name = extractString(text, "connectionName", "name")?.ifBlank { null } ?: share
            val storage = extractString(text, "storage")?.ifBlank { null } ?: STORAGE_VALUE
            return SetupCifsBundle(
                token = token,
                hostIp = host.trim(),
                userQualified = user.trim(),
                password = password,
                shareName = share.trim(),
                computerName = pc.trim(),
                port = port.trim(),
                folder = folder.trim(),
                connectionName = name.trim(),
                storage = storage.trim(),
            )
        }

        /** JSON ASCII compatto (test / dry-run). */
        fun toJsonCompact(bundle: SetupCifsBundle): String {
            fun esc(s: String): String = buildString {
                for (c in s) {
                    when (c) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\n', '\r', '\t' -> { /* drop control */ }
                        else -> if (c.code in 32..126) append(c)
                    }
                }
            }
            return buildString {
                append('{')
                append("\"hostIp\":\"").append(esc(bundle.hostIp)).append("\",")
                append("\"userQualified\":\"").append(esc(bundle.userQualified)).append("\",")
                append("\"password\":\"").append(esc(bundle.password)).append("\",")
                append("\"port\":\"").append(esc(bundle.port)).append("\",")
                append("\"connectionName\":\"").append(esc(bundle.connectionName)).append("\",")
                append("\"folder\":\"").append(esc(bundle.folder)).append("\",")
                append("\"shareName\":\"").append(esc(bundle.shareName)).append("\",")
                append("\"computerName\":\"").append(esc(bundle.computerName)).append("\",")
                append("\"storage\":\"").append(esc(bundle.storage)).append("\",")
                append("\"token\":\"").append(esc(bundle.token)).append('"')
                append('}')
            }
        }

        private fun extractString(json: String, vararg keys: String): String? {
            for (key in keys) {
                val re = Regex(
                    """"$key"\s*:\s*"((?:\\.|[^"\\])*)"""",
                    RegexOption.IGNORE_CASE,
                )
                val m = re.find(json) ?: continue
                return unescape(m.groupValues[1])
            }
            // port numerico senza virgolette
            for (key in keys) {
                val re = Regex(""""$key"\s*:\s*(\d+)""", RegexOption.IGNORE_CASE)
                val m = re.find(json) ?: continue
                return m.groupValues[1]
            }
            return null
        }

        private fun unescape(s: String): String = buildString {
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '\\' && i + 1 < s.length) {
                    when (s[i + 1]) {
                        '\\' -> append('\\')
                        '"' -> append('"')
                        'n' -> append('\n')
                        'r' -> append('\r')
                        't' -> append('\t')
                        'u' -> {
                            if (i + 5 < s.length) {
                                val hex = s.substring(i + 2, i + 6)
                                append(hex.toIntOrNull(16)?.toChar() ?: '?')
                                i += 6
                                continue
                            }
                            append('u')
                        }
                        else -> append(s[i + 1])
                    }
                    i += 2
                } else {
                    append(c)
                    i++
                }
            }
        }
    }
}
