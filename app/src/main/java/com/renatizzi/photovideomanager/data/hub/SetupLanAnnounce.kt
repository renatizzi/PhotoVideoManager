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
 * - l’app cerca in HTTP sulla rete locale e ascolta UDP;
 * - non si ferma al primo pacchetto parziale: serve un IPv4 reale + utente.
 */
object SetupLanAnnounce {
    const val UDP_PORT = 38455
    const val HTTP_PORT = 38456
    const val MAGIC = "PVMSETUP1"
    const val DISCOVER_MS = 60_000L
    private const val RECV_TIMEOUT_MS = 800
    private const val HTTP_CONNECT_MS = 400
    private const val HTTP_READ_MS = 400

    private val IPV4_RE = Regex("""^(?:\d{1,3}\.){3}\d{1,3}$""")

    data class Payload(
        val token: String,
        val hostIp: String,
        val userQualified: String,
        val shareName: String,
        val computerName: String,
    ) {
        /** Host da mettere in CIFS: solo IPv4; mai il solo nome PC. */
        val hostForCifs: String
            get() = hostIp.takeIf { isIpv4(it) }.orEmpty()

        val hasIpv4: Boolean get() = isIpv4(hostIp)

        val hasUser: Boolean
            get() = userQualified.isNotBlank() || computerName.isNotBlank()

        /** Pronto per compilare il form CIFS senza altri tentativi. */
        val isComplete: Boolean get() = hasIpv4 && hasUser
    }

    fun isIpv4(value: String): Boolean {
        val t = value.trim()
        if (!IPV4_RE.matches(t)) return false
        return t.split('.').all { part ->
            val n = part.toIntOrNull() ?: return false
            n in 0..255
        }
    }

    fun parsePayload(raw: String): Payload? {
        val text = raw.trim()
        if (!text.contains(MAGIC)) return null
        val token = field(text, "t") ?: return null
        if (token.isBlank()) return null
        var ip = field(text, "ip").orEmpty().trim()
        if (!isIpv4(ip)) {
            ip = field(text, "ips").orEmpty()
                .split(',', ' ', ';')
                .map { it.trim() }
                .firstOrNull { isIpv4(it) }
                .orEmpty()
        }
        val user = field(text, "user").orEmpty()
        val pc = field(text, "pc").orEmpty()
        if (!isIpv4(ip) && user.isBlank() && pc.isBlank()) return null
        return Payload(
            token = token,
            hostIp = ip,
            userQualified = user,
            shareName = field(text, "share").orEmpty(),
            computerName = pc,
        )
    }

    fun mergePayloads(current: Payload?, incoming: Payload): Payload {
        if (current == null) return incoming
        return Payload(
            token = incoming.token.ifBlank { current.token },
            hostIp = when {
                isIpv4(incoming.hostIp) -> incoming.hostIp
                isIpv4(current.hostIp) -> current.hostIp
                else -> incoming.hostIp.ifBlank { current.hostIp }
            },
            userQualified = incoming.userQualified.ifBlank { current.userQualified },
            shareName = incoming.shareName.ifBlank { current.shareName },
            computerName = incoming.computerName.ifBlank { current.computerName },
        )
    }

    /**
     * Cerca il payload con [expectedToken] via HTTP (scansione rete) e UDP in parallelo.
     * Restituisce solo quando c’è IPv4 + utente, oppure il miglior parziale a scadenza.
     */
    fun discover(
        context: Context,
        expectedToken: String,
        cancelled: AtomicBoolean = AtomicBoolean(false),
        discoverMs: Long = DISCOVER_MS,
    ): Payload? {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val lock = wifi?.createMulticastLock("pvm-setup-announce")
        lock?.setReferenceCounted(false)
        lock?.acquire()
        try {
            return discoverNetwork(
                expectedToken = expectedToken,
                cancelled = cancelled,
                discoverMs = discoverMs,
                candidateHosts = null,
            )
        } finally {
            if (lock?.isHeld == true) lock.release()
        }
    }

    /**
     * Variante testabile senza Context. Se [candidateHosts] è null, usa le subnet locali.
     */
    fun discoverNetwork(
        expectedToken: String,
        cancelled: AtomicBoolean = AtomicBoolean(false),
        discoverMs: Long = DISCOVER_MS,
        candidateHosts: List<String>? = null,
    ): Payload? {
        val want = expectedToken.trim()
        if (want.isEmpty()) return null
        val found = AtomicReference<Payload?>(null)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val udpFuture: Future<*> = pool.submit {
                listenUdp(want, cancelled, discoverMs, found)
            }
            val httpFuture: Future<*> = pool.submit {
                scanHttp(want, cancelled, discoverMs, found, candidateHosts)
            }
            val deadline = System.currentTimeMillis() + discoverMs
            while (System.currentTimeMillis() < deadline && !cancelled.get()) {
                found.get()?.takeIf { it.isComplete }?.let { return it }
                if (udpFuture.isDone && httpFuture.isDone) break
                try {
                    Thread.sleep(50)
                } catch (_: InterruptedException) {
                    break
                }
            }
            cancelled.set(true)
            return found.get()?.takeIf { it.isComplete } ?: found.get()
        } finally {
            cancelled.set(true)
            pool.shutdownNow()
            runCatching { pool.awaitTermination(1, TimeUnit.SECONDS) }
        }
    }

    private fun offer(found: AtomicReference<Payload?>, incoming: Payload) {
        while (true) {
            val cur = found.get()
            val merged = mergePayloads(cur, incoming)
            if (found.compareAndSet(cur, merged)) return
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
                    found.get()?.isComplete != true
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
                        var payload = parsePayload(raw) ?: continue
                        if (payload.token != want) continue
                        // Sorgente UDP = IP del PC anche se il JSON ha ip vuoto.
                        if (!payload.hasIpv4) {
                            val src = packet.address?.hostAddress.orEmpty()
                            val v4 = src.substringBefore('%')
                            if (isIpv4(v4) && !v4.startsWith("127.")) {
                                payload = payload.copy(hostIp = v4)
                            } else if (isIpv4(v4)) {
                                // loopback ammesso in simulazione / stesso host
                                payload = payload.copy(hostIp = v4)
                            }
                        }
                        offer(found, payload)
                        if (found.get()?.isComplete == true) return
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
        candidateHosts: List<String>?,
    ) {
        val hosts = candidateHosts ?: localIpv4Candidates()
        if (hosts.isEmpty()) return
        val deadline = System.currentTimeMillis() + discoverMs
        val workers = Executors.newFixedThreadPool(64)
        try {
            val tasks = hosts.map { ip ->
                workers.submit {
                    if (cancelled.get() || found.get()?.isComplete == true) return@submit
                    if (System.currentTimeMillis() > deadline) return@submit
                    val payload = fetchHttpPayload(ip, want) ?: return@submit
                    offer(found, payload)
                }
            }
            while (System.currentTimeMillis() < deadline &&
                !cancelled.get() &&
                found.get()?.isComplete != true
            ) {
                if (tasks.all { it.isDone }) break
                try {
                    Thread.sleep(40)
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
                // L’IP raggiunto in TCP è la fonte di verità per Host CIFS.
                if (isIpv4(ip)) {
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

    fun localIpv4Candidates(): List<String> {
        val bases = linkedSetOf<String>()
        val self = linkedSetOf<String>()
        try {
            val en = NetworkInterface.getNetworkInterfaces() ?: return emptyList()
            for (nif in Collections.list(en)) {
                if (!nif.isUp || nif.isLoopback) continue
                for (addr in Collections.list(nif.inetAddresses)) {
                    if (addr !is Inet4Address || addr.isLoopbackAddress) continue
                    val host = addr.hostAddress ?: continue
                    val parts = host.split('.')
                    if (parts.size != 4) continue
                    if (!isIpv4(host)) continue
                    self.add(host)
                    bases.add("${parts[0]}.${parts[1]}.${parts[2]}.")
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        // Priorità: indirizzi locali noti, poi .1 (router spesso vicino), poi resto subnet.
        val out = LinkedHashSet<String>()
        out.addAll(self)
        for (base in bases) {
            out.add("${base}1")
        }
        for (base in bases) {
            for (i in 1..254) out.add("$base$i")
        }
        return out.toList()
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
