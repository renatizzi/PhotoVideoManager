package com.renatizzi.photovideomanager.data.hub

/**
 * Bundle completo post «Tutto pronto» per compilare CIFS via Copia.
 * Include **password** (a differenza del QR-handoff bocciato).
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
) {
    val isComplete: Boolean
        get() = SetupLanAnnounce.isIpv4(hostIp) &&
            userQualified.isNotBlank() &&
            userQualified.contains('\\') &&
            password.isNotBlank() &&
            folder.isNotBlank() &&
            connectionName.isNotBlank()

    fun toLanPayload(): SetupLanAnnounce.Payload = SetupLanAnnounce.Payload(
        token = token,
        hostIp = hostIp,
        userQualified = userQualified,
        shareName = shareName,
        computerName = computerName,
        password = password,
        port = port,
        folder = folder,
        connectionName = connectionName,
    )

    companion object {
        const val DEFAULT_PORT = "445"
        /**
         * Nome fisso del bundle da inviare/importare (BL-BT-01).
         * Evita file omonimi per-token sul Desktop (BL-BT-02 overwrite).
         */
        const val BUNDLE_FILE_NAME = "DATI_CIFS.json"
        /** Prefisso legacy (pre-DATI_CIFS); ancora accettato in lettura. */
        const val LEGACY_BUNDLE_FILE_PREFIX = "MediaManager_cifs_bundle"
        /** Alias legacy del prefisso (import / MediaStore LIKE). */
        const val BUNDLE_FILE_PREFIX = LEGACY_BUNDLE_FILE_PREFIX
        /** Nome legacy esatto (pre-0.16.72); ancora accettato in lettura. */
        const val LEGACY_BUNDLE_FILE_NAME = "MediaManager_cifs_bundle.json"
        const val STORAGE_VALUE = "SMB2,3 (jCIFS NG)"
        const val MAGIC = SetupLanAnnounce.MAGIC

        /**
         * Nome file bundle per la sessione: fisso [BUNDLE_FILE_NAME].
         * Il token resta validato nel JSON, non nel nome file.
         */
        @Suppress("UNUSED_PARAMETER")
        fun bundleFileNameForToken(setupToken: String): String = BUNDLE_FILE_NAME

        fun isBundleFileName(name: String): Boolean {
            val n = name.trim()
            if (n.equals(BUNDLE_FILE_NAME, ignoreCase = true)) return true
            // MTP: "DATI_CIFS (1).json" oppure "DATI_CIFS.json (1)" (Windows Esplora).
            if (Regex("""^DATI_CIFS \(\d+\)\.json$""", RegexOption.IGNORE_CASE).matches(n)) {
                return true
            }
            if (Regex("""^DATI_CIFS\.json \(\d+\)$""", RegexOption.IGNORE_CASE).matches(n)) {
                return true
            }
            if (n.equals(LEGACY_BUNDLE_FILE_NAME, ignoreCase = true)) return true
            if (n.contains("(") && n.contains(")")) return false
            return n.startsWith(LEGACY_BUNDLE_FILE_PREFIX, ignoreCase = true) &&
                n.endsWith(".json", ignoreCase = true)
        }

        /** True se il JSON è il result senza password (non importabile come bundle). */
        fun looksLikeSetupResultNotBundle(raw: String): Boolean {
            val t = raw.trim()
            if (t.contains(MAGIC)) return false
            return t.contains("\"ipv4\"") && t.contains("\"status\"")
        }

        fun fromPayload(p: SetupLanAnnounce.Payload): SetupCifsBundle {
            val share = p.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
            return SetupCifsBundle(
                token = p.token,
                hostIp = p.hostForCifs,
                userQualified = when {
                    p.userQualified.isNotBlank() -> p.userQualified
                    p.computerName.isNotBlank() ->
                        WindowsCatalogSetupScript.cifsUserCopyText(share, p.computerName)
                    else -> ""
                },
                password = p.password,
                shareName = share,
                computerName = p.computerName,
                port = p.port.ifBlank { DEFAULT_PORT },
                folder = p.folder.ifBlank { share },
                connectionName = p.connectionName.ifBlank { share },
            )
        }

        /**
         * Parse JSON announce / file USB. Password obbligatoria per [isComplete].
         */
        fun parse(raw: String, expectedToken: String? = null): SetupCifsBundle? {
            val payload = SetupLanAnnounce.parsePayload(raw) ?: return null
            if (!expectedToken.isNullOrBlank() && payload.token != expectedToken) return null
            return fromPayload(payload)
        }

        /** JSON compatto per script / announce (ASCII). */
        fun toJsonCompact(bundle: SetupCifsBundle): String {
            fun esc(s: String): String = buildString {
                for (c in s) {
                    when (c) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        else -> append(c)
                    }
                }
            }
            return buildString {
                append('{')
                append("\"v\":3,")
                append("\"m\":\"").append(MAGIC).append("\",")
                append("\"t\":\"").append(esc(bundle.token)).append("\",")
                append("\"ip\":\"").append(esc(bundle.hostIp)).append("\",")
                append("\"user\":\"").append(esc(bundle.userQualified)).append("\",")
                append("\"password\":\"").append(esc(bundle.password)).append("\",")
                append("\"share\":\"").append(esc(bundle.shareName)).append("\",")
                append("\"pc\":\"").append(esc(bundle.computerName)).append("\",")
                append("\"port\":\"").append(esc(bundle.port)).append("\",")
                append("\"folder\":\"").append(esc(bundle.folder)).append("\",")
                append("\"name\":\"").append(esc(bundle.connectionName)).append("\"")
                append('}')
            }
        }
    }
}
