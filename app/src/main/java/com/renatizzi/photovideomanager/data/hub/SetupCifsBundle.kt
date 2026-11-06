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
            password.isNotBlank()

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
        const val BUNDLE_FILE_NAME = "MediaManager_cifs_bundle.json"
        const val MAGIC = SetupLanAnnounce.MAGIC

        fun fromPayload(p: SetupLanAnnounce.Payload): SetupCifsBundle = SetupCifsBundle(
            token = p.token,
            hostIp = p.hostForCifs,
            userQualified = when {
                p.userQualified.isNotBlank() -> p.userQualified
                p.computerName.isNotBlank() ->
                    WindowsCatalogSetupScript.cifsUserCopyText(
                        WindowsCatalogSetupScript.DEFAULT_USER,
                        p.computerName,
                    )
                else -> ""
            },
            password = p.password,
            shareName = p.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
            computerName = p.computerName,
            port = p.port.ifBlank { DEFAULT_PORT },
            folder = p.folder.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
            connectionName = p.connectionName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
        )

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
