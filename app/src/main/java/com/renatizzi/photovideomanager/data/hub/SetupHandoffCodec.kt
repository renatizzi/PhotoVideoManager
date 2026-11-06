package com.renatizzi.photovideomanager.data.hub

import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Canale durevole PC → app per Host/User (sostituisce il LAN announce come primario).
 *
 * Wire: `pvmsetup:2;<base64url(JSON UTF-8)>`
 * Password **mai** nel payload (resta solo in app / Copia).
 *
 * JSON costruito/letto senza `org.json` così i unit test JVM non dipendono da mock Android.
 */
object SetupHandoffCodec {
    const val SCHEME = "pvmsetup"
    const val VERSION = 2
    const val MAGIC = SetupLanAnnounce.MAGIC
    const val URI_PREFIX = "$SCHEME:$VERSION;"
    /** Validità QR da «Tutto pronto». */
    val DEFAULT_TTL_MS: Long = TimeUnit.MINUTES.toMillis(45)

    data class Handoff(
        val token: String,
        val hostIp: String,
        val userQualified: String,
        val shareName: String,
        val computerName: String,
        val expiresAtEpochSec: Long,
        val version: Int = VERSION,
    ) {
        fun toLanPayload(): SetupLanAnnounce.Payload = SetupLanAnnounce.Payload(
            token = token,
            hostIp = hostIp,
            userQualified = userQualified,
            shareName = shareName,
            computerName = computerName,
        )

        val isComplete: Boolean
            get() = SetupLanAnnounce.isIpv4(hostIp) &&
                (userQualified.isNotBlank() || computerName.isNotBlank())
    }

    sealed class DecodeError(val message: String) {
        class Format(message: String) : DecodeError(message)
        class Token(message: String) : DecodeError(message)
        class Expired(message: String) : DecodeError(message)
        class Incomplete(message: String) : DecodeError(message)
    }

    data class DecodeResult(
        val handoff: Handoff? = null,
        val error: DecodeError? = null,
    ) {
        val isOk: Boolean get() = handoff != null && error == null
    }

    fun encodeUri(handoff: Handoff): String {
        val json = buildString {
            append('{')
            append("\"v\":").append(handoff.version).append(',')
            append("\"m\":\"").append(escapeJson(MAGIC)).append("\",")
            append("\"t\":\"").append(escapeJson(handoff.token)).append("\",")
            append("\"ip\":\"").append(escapeJson(handoff.hostIp)).append("\",")
            append("\"user\":\"").append(escapeJson(handoff.userQualified)).append("\",")
            append("\"share\":\"").append(escapeJson(handoff.shareName)).append("\",")
            append("\"pc\":\"").append(escapeJson(handoff.computerName)).append("\",")
            append("\"exp\":").append(handoff.expiresAtEpochSec)
            append('}')
        }
        require(!json.contains("password", ignoreCase = true)) {
            "handoff JSON must not contain password"
        }
        return URI_PREFIX + base64UrlEncode(json.toByteArray(StandardCharsets.UTF_8))
    }

    fun encodeUri(
        token: String,
        hostIp: String,
        userQualified: String,
        shareName: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
        computerName: String = "",
        nowEpochMs: Long = System.currentTimeMillis(),
        ttlMs: Long = DEFAULT_TTL_MS,
    ): String = encodeUri(
        Handoff(
            token = token.trim(),
            hostIp = hostIp.trim(),
            userQualified = userQualified.trim(),
            shareName = shareName.trim().ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
            computerName = computerName.trim(),
            expiresAtEpochSec = TimeUnit.MILLISECONDS.toSeconds(nowEpochMs + ttlMs),
        ),
    )

    /**
     * Accetta URI `pvmsetup:2;…`, JSON grezzo PVMSETUP1, o testo che lo contiene.
     */
    fun decode(
        raw: String,
        expectedToken: String,
        nowEpochMs: Long = System.currentTimeMillis(),
    ): DecodeResult {
        val want = expectedToken.trim()
        if (want.isEmpty()) {
            return DecodeResult(error = DecodeError.Token("Sessione setup senza token: ripeti Preparazione script"))
        }
        val text = raw.trim()
        if (text.isEmpty()) {
            return DecodeResult(error = DecodeError.Format("Codice vuoto"))
        }

        val jsonText = extractJson(text)
            ?: return DecodeResult(error = DecodeError.Format("Codice non riconosciuto (atteso $URI_PREFIX…)"))

        if (!jsonText.contains(MAGIC)) {
            return DecodeResult(error = DecodeError.Format("Codice non MediaManager"))
        }
        if (jsonText.contains("\"password\"", ignoreCase = true) ||
            jsonText.contains("PlainPassword", ignoreCase = true)
        ) {
            return DecodeResult(error = DecodeError.Format("Codice non valido (contiene password)"))
        }

        val token = jsonStringField(jsonText, "t").orEmpty().trim()
        if (token.isEmpty()) {
            return DecodeResult(error = DecodeError.Token("Token assente nel codice"))
        }
        if (token != want) {
            return DecodeResult(
                error = DecodeError.Token(
                    "Questo codice non è di questa sessione: riesegui CONFIGURA generato da quest’app",
                ),
            )
        }

        val exp = jsonLongField(jsonText, "exp") ?: 0L
        if (exp > 0L) {
            val nowSec = TimeUnit.MILLISECONDS.toSeconds(nowEpochMs)
            if (nowSec > exp) {
                return DecodeResult(
                    error = DecodeError.Expired(
                        "Codice scaduto: lascia aperta «Tutto pronto» e genera di nuovo CONFIGURA, oppure inserisci IP e User a mano",
                    ),
                )
            }
        }

        var ip = jsonStringField(jsonText, "ip").orEmpty().trim()
        if (!SetupLanAnnounce.isIpv4(ip)) {
            ip = jsonStringField(jsonText, "ips").orEmpty()
                .split(',', ' ', ';')
                .map { it.trim() }
                .firstOrNull { SetupLanAnnounce.isIpv4(it) }
                .orEmpty()
        }
        val user = jsonStringField(jsonText, "user").orEmpty().trim()
        val pc = jsonStringField(jsonText, "pc").orEmpty().trim()
        val share = jsonStringField(jsonText, "share").orEmpty().trim()
            .ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
        val version = jsonLongField(jsonText, "v")?.toInt() ?: VERSION

        if (!SetupLanAnnounce.isIpv4(ip)) {
            return DecodeResult(
                error = DecodeError.Incomplete("Nel codice manca un IPv4 valido (Host)"),
            )
        }
        if (user.isBlank() && pc.isBlank()) {
            return DecodeResult(
                error = DecodeError.Incomplete("Nel codice manca l’utente (COMPUTERNAME\\MyMedia)"),
            )
        }
        val qualified = when {
            user.contains('\\') -> user
            user.isNotBlank() && pc.isNotBlank() -> "$pc\\$user"
            pc.isNotBlank() -> "$pc\\${WindowsCatalogSetupScript.DEFAULT_USER}"
            else -> user
        }

        return DecodeResult(
            handoff = Handoff(
                token = token,
                hostIp = ip,
                userQualified = qualified,
                shareName = share,
                computerName = pc,
                expiresAtEpochSec = exp,
                version = version,
            ),
        )
    }

    private fun extractJson(text: String): String? {
        val trimmed = text.trim()
        val uriIdx = trimmed.indexOf(URI_PREFIX, ignoreCase = true)
        if (uriIdx >= 0) {
            val b64 = trimmed.substring(uriIdx + URI_PREFIX.length)
                .trim()
                .takeWhile { !it.isWhitespace() }
            val decoded = base64UrlDecode(b64) ?: return null
            return decoded.toString(StandardCharsets.UTF_8)
        }
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1)
        }
        return null
    }

    private fun escapeJson(value: String): String = buildString(value.length) {
        for (c in value) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }

    /** Stesso stile di [SetupLanAnnounce]: campo stringa JSON `"key":"value"`. */
    private fun jsonStringField(json: String, key: String): String? {
        val marker = "\"$key\""
        val i = json.indexOf(marker)
        if (i < 0) return null
        var p = i + marker.length
        while (p < json.length && json[p].isWhitespace()) p++
        if (p >= json.length || json[p] != ':') return null
        p++
        while (p < json.length && json[p].isWhitespace()) p++
        if (p >= json.length || json[p] != '"') return null
        p++
        val out = StringBuilder()
        while (p < json.length) {
            val c = json[p]
            if (c == '\\' && p + 1 < json.length) {
                out.append(json[p + 1])
                p += 2
                continue
            }
            if (c == '"') break
            out.append(c)
            p++
        }
        return out.toString()
    }

    private fun jsonLongField(json: String, key: String): Long? {
        val marker = "\"$key\""
        val i = json.indexOf(marker)
        if (i < 0) return null
        var p = i + marker.length
        while (p < json.length && json[p].isWhitespace()) p++
        if (p >= json.length || json[p] != ':') return null
        p++
        while (p < json.length && json[p].isWhitespace()) p++
        val start = p
        while (p < json.length && (json[p].isDigit() || json[p] == '-')) p++
        if (start == p) return null
        return json.substring(start, p).toLongOrNull()
    }

    fun base64UrlEncode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun base64UrlDecode(text: String): ByteArray? = try {
        Base64.getUrlDecoder().decode(text)
    } catch (_: Exception) {
        null
    }
}
