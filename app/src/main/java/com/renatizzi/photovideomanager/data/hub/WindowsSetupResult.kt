package com.renatizzi.photovideomanager.data.hub

/**
 * Esito script Windows (`MediaManager_setup_result.json`), senza password.
 * Usato al Passo 5 per User (`NOMEPC\utente`) e Host (IP).
 */
data class WindowsSetupResult(
    val status: String,
    val computerName: String,
    val userName: String,
    val userFormatHint: String,
    val shareName: String,
    val folderPath: String,
    val ipv4: List<String>,
) {
    val primaryIpv4: String
        get() = ipv4.firstOrNull().orEmpty()

    val isCompleted: Boolean
        get() = status.equals("completed", ignoreCase = true)
}

object WindowsSetupResultParser {
    fun parse(json: String): WindowsSetupResult? {
        if (json.isBlank()) return null
        return runCatching {
            val status = stringField(json, "status") ?: return null
            val ipv4 = stringArrayField(json, "ipv4")
            WindowsSetupResult(
                status = status,
                computerName = stringField(json, "computerName").orEmpty(),
                userName = stringField(json, "userName")
                    ?.ifBlank { null }
                    ?: WindowsCatalogSetupScript.DEFAULT_USER,
                userFormatHint = stringField(json, "userFormatHint").orEmpty(),
                shareName = stringField(json, "shareName")
                    ?.ifBlank { null }
                    ?: WindowsCatalogSetupScript.DEFAULT_SHARE,
                folderPath = stringField(json, "folderPath").orEmpty(),
                ipv4 = ipv4,
            )
        }.getOrNull()
    }

    /** Preferisce userFormatHint; altrimenti computerName\user. */
    fun resolvedUser(result: WindowsSetupResult): String {
        val hint = result.userFormatHint.trim()
        if (hint.isNotEmpty()) return hint
        val pc = result.computerName.trim()
        val user = result.userName.trim().ifBlank { WindowsCatalogSetupScript.DEFAULT_USER }
        return if (pc.isNotEmpty()) {
            "$pc\\$user"
        } else {
            WindowsCatalogSetupScript.cifsUserCopyText(user)
        }
    }

    private fun stringField(json: String, name: String): String? {
        val re = Regex("\"${Regex.escape(name)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        val match = re.find(json) ?: return null
        return unescapeJson(match.groupValues[1])
    }

    private fun stringArrayField(json: String, name: String): List<String> {
        val re = Regex("\"${Regex.escape(name)}\"\\s*:\\s*\\[(.*?)]", RegexOption.DOT_MATCHES_ALL)
        val body = re.find(json)?.groupValues?.get(1) ?: return emptyList()
        return Regex("\"((?:\\\\.|[^\"\\\\])*)\"")
            .findAll(body)
            .map { unescapeJson(it.groupValues[1]).trim() }
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun unescapeJson(raw: String): String {
        val out = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                val n = raw[i + 1]
                when (n) {
                    '\\', '"', '/' -> {
                        out.append(n)
                        i += 2
                    }
                    'n' -> {
                        out.append('\n')
                        i += 2
                    }
                    't' -> {
                        out.append('\t')
                        i += 2
                    }
                    'u' -> {
                        if (i + 5 < raw.length) {
                            val hex = raw.substring(i + 2, i + 6)
                            out.append(hex.toInt(16).toChar())
                            i += 6
                        } else {
                            out.append(n)
                            i += 2
                        }
                    }
                    else -> {
                        // Keep unknown escapes as literal backslash + char (e.g. \M).
                        out.append('\\')
                        out.append(n)
                        i += 2
                    }
                }
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }
}
