package com.renatizzi.photovideomanager.data.hub

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Annuncio breve in rete locale dopo «Tutto pronto»: il PC invia IP + User
 * legati al token generato dall’app. Nessuna copia file sul telefono.
 */
object SetupLanAnnounce {
    const val PORT = 38455
    const val MAGIC = "PVMSETUP1"
    /** Durata ascolto lato app (ms). */
    const val LISTEN_MS = 60_000L
    private const val RECV_TIMEOUT_MS = 1_500

    data class Payload(
        val token: String,
        val hostIp: String,
        val userQualified: String,
        val shareName: String,
        val computerName: String,
    )

    fun parsePayload(raw: String): Payload? {
        val text = raw.trim()
        if (!text.contains(MAGIC)) return null
        val token = field(text, "t") ?: return null
        val ip = field(text, "ip").orEmpty()
        val user = field(text, "user").orEmpty()
        if (token.isBlank() || (ip.isBlank() && user.isBlank())) return null
        return Payload(
            token = token,
            hostIp = ip,
            userQualified = user,
            shareName = field(text, "share").orEmpty(),
            computerName = field(text, "pc").orEmpty(),
        )
    }

    /**
     * Ascolta UDP fino a [LISTEN_MS] o finché [cancelled].
     * Restituisce il primo payload con token uguale a [expectedToken].
     */
    fun listenForToken(
        expectedToken: String,
        cancelled: AtomicBoolean = AtomicBoolean(false),
        listenMs: Long = LISTEN_MS,
    ): Payload? {
        val want = expectedToken.trim()
        if (want.isEmpty()) return null
        val deadline = System.currentTimeMillis() + listenMs
        DatagramSocket(null).use { socket ->
            socket.reuseAddress = true
            socket.broadcast = true
            socket.bind(InetSocketAddress(PORT))
            socket.soTimeout = RECV_TIMEOUT_MS
            val buf = ByteArray(2048)
            while (System.currentTimeMillis() < deadline && !cancelled.get()) {
                try {
                    val packet = DatagramPacket(buf, buf.size)
                    socket.receive(packet)
                    val raw = String(packet.data, packet.offset, packet.length, StandardCharsets.UTF_8)
                    val payload = parsePayload(raw) ?: continue
                    if (payload.token == want) return payload
                } catch (_: SocketTimeoutException) {
                    // retry until deadline
                } catch (_: Exception) {
                    return null
                }
            }
        }
        return null
    }

    private fun field(json: String, name: String): String? {
        val re = Regex("\"${Regex.escape(name)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        val match = re.find(json) ?: return null
        return unescape(match.groupValues[1])
    }

    private fun unescape(raw: String): String {
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
                    else -> {
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
