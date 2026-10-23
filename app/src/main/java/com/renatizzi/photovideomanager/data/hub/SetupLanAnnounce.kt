package com.renatizzi.photovideomanager.data.hub

import android.content.Context
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Recupero IP/User dopo «Tutto pronto»:
 * - il PC espone i dati in HTTP (porta [HTTP_PORT]) e invia anche UDP;
 * - l’app cerca in HTTP sulla rete locale e ascolta UDP.
 */
object SetupLanAnnounce {
    const val UDP_PORT = 38455
    const val HTTP_PORT = 38456
    const val MAGIC = "PVMSETUP1"
    const val DISCOVER_MS = 45_000L
    private const val RECV_TIMEOUT_MS = 1_000
    private const val HTTP_CONNECT_MS = 350
    private const val HTTP_READ_MS = 350

    data class Payload(
        val token: String,
        val hostIp: String,
        val userQualified: String,
        val shareName: String,
        val computerName: String,
    ) {
        /** Host da mettere in CIFS: IP se c’è, altrimenti nome PC. */
        val hostForCifs: String
            get() = hostIp.ifBlank { computerName }.trim()
    }

    fun parsePayload(raw: String): Payload? {
        val text = raw.trim()
        if (!text.contains(MAGIC)) return null
        val token = field(text, "t") ?: return null
        val ip = field(text, "ip").orEmpty()
        val user = field(text, "user").orEmpty()
        val pc = field(text, "pc").orEmpty()
        if (token.isBlank()) return null
        if (ip.isBlank() && user.isBlank() && pc.isBlank()) return null
        return Payload(
            token = token,
            hostIp = ip,
            userQualified = user,
            shareName = field(text, "share").orEmpty(),
            computerName = pc,
        )
    }

    /**
     * Cerca il payload con [expectedToken] via HTTP (scansione rete) e UDP in parallelo.
     */
    fun discover(
        context: Context,
        expectedToken: String,
        cancelled: AtomicBoolean = AtomicBoolean(false),
        discoverMs: Long = DISCOVER_MS,
    ): Payload? {
        val want = expectedToken.trim()
        if (want.isEmpty()) return null
        val found = AtomicReference<Payload?>(null)
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val lock = wifi?.createMulticastLock("pvm-setup-announce")
        lock?.setReferenceCounted(false)
        lock?.acquire()
        val pool = Executors.newFixedThreadPool(2)
        try {
            val udpFuture: Future<*> = pool.submit {
                listenUdp(want, cancelled, discoverMs, found)
            }
            val httpFuture: Future<*> = pool.submit {
                scanHttp(want, cancelled, discoverMs, found)
            }
            val deadline = System.currentTimeMillis() + discoverMs
            while (System.currentTimeMillis() < deadline && !cancelled.get()) {
                found.get()?.let { return it }
                if (udpFuture.isDone && httpFuture.isDone) break
                try {
                    Thread.sleep(100)
                } catch (_: InterruptedException) {
                    break
                }
            }
            cancelled.set(true)
            return found.get()
        } finally {
            cancelled.set(true)
            pool.shutdownNow()
            runCatching { pool.awaitTermination(1, TimeUnit.SECONDS) }
            if (lock?.isHeld == true) lock.release()
        }
    }

    private fun listenUdp(
        want: String,
        cancelled: AtomicBoolean,
        discoverMs: Long,
        found: AtomicReference<Payload?>,
    ) {
        val deadline = System.currentTimeMillis() + discoverMs
        try {
            DatagramSocket(null).use { socket ->
                socket.reuseAddress = true
                socket.broadcast = true
                socket.bind(InetSocketAddress(UDP_PORT))
                socket.soTimeout = RECV_TIMEOUT_MS
                val buf = ByteArray(4096)
                while (System.currentTimeMillis() < deadline &&
                    !cancelled.get() &&
                    found.get() == null
                ) {
                    try {
                        val packet = DatagramPacket(buf, buf.size)
                        socket.receive(packet)
                        val raw = String(
                            packet.data,
                            packet.offset,
                            packet.length,
                            StandardCharsets.UTF_8,
                        )
                        val payload = parsePayload(raw) ?: continue
                        if (payload.token == want) {
                            found.compareAndSet(null, payload)
                            return
                        }
                    } catch (_: SocketTimeoutException) {
                        // continue
                    }
                }
            }
        } catch (_: Exception) {
            // UDP non disponibile su questo dispositivo/rete
        }
    }

    private fun scanHttp(
        want: String,
        cancelled: AtomicBoolean,
        discoverMs: Long,
        found: AtomicReference<Payload?>,
    ) {
        val hosts = localIpv4Candidates()
        if (hosts.isEmpty()) return
        val deadline = System.currentTimeMillis() + discoverMs
        val workers = Executors.newFixedThreadPool(48)
        try {
            val tasks = hosts.map { ip ->
                workers.submit {
                    if (cancelled.get() || found.get() != null) return@submit
                    if (System.currentTimeMillis() > deadline) return@submit
                    val payload = fetchHttpPayload(ip, want) ?: return@submit
                    found.compareAndSet(null, payload)
                }
            }
            while (System.currentTimeMillis() < deadline &&
                !cancelled.get() &&
                found.get() == null
            ) {
                if (tasks.all { it.isDone }) break
                try {
                    Thread.sleep(50)
                } catch (_: InterruptedException) {
                    break
                }
            }
        } finally {
            workers.shutdownNow()
        }
    }

    private fun fetchHttpPayload(ip: String, want: String): Payload? {
        return try {
            val url = URL("http://$ip:$HTTP_PORT/")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = HTTP_CONNECT_MS
                readTimeout = HTTP_READ_MS
                requestMethod = "GET"
                instanceFollowRedirects = false
            }
            try {
                if (conn.responseCode != 200) return null
                val raw = conn.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
                val payload = parsePayload(raw) ?: return null
                if (payload.token != want) return null
                // Se il PC non ha messo l’IP nel JSON, usa l’IP raggiunto.
                if (payload.hostIp.isBlank()) {
                    payload.copy(hostIp = ip)
                } else {
                    payload
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun localIpv4Candidates(): List<String> {
        val bases = linkedSetOf<String>()
        try {
            val en = NetworkInterface.getNetworkInterfaces() ?: return emptyList()
            for (nif in Collections.list(en)) {
                if (!nif.isUp || nif.isLoopback) continue
                for (addr in Collections.list(nif.inetAddresses)) {
                    if (addr !is Inet4Address || addr.isLoopbackAddress) continue
                    val host = addr.hostAddress ?: continue
                    val parts = host.split('.')
                    if (parts.size != 4) continue
                    bases.add("${parts[0]}.${parts[1]}.${parts[2]}.")
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        val out = ArrayList<String>(bases.size * 254)
        for (base in bases) {
            for (i in 1..254) out.add("$base$i")
        }
        return out
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
