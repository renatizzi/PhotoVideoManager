package com.renatizzi.photovideomanager.data.hub

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simulazioni end-to-end su localhost: PC announcer (TCP+UDP) → discover app.
 * Ripetute più volte: il recupero IP+User deve riuscire al primo colpo, non a tentativi.
 */
class SetupLanAnnounceSimulationTest {

    @Test
    fun httpDiscover_fillsIpv4AndUser_firstShot() {
        val token = "simhttp" + System.nanoTime().toString(16).takeLast(8)
        val body =
            """{"v":1,"m":"PVMSETUP1","t":"$token","ip":"","user":"DESKTOP-E5C1L4E\\MediaManager","share":"MediaManagerCatalog","pc":"DESKTOP-E5C1L4E","ips":""}"""
        val server = startHttpAnnouncer(body)
        try {
            val got = SetupLanAnnounce.discoverNetwork(
                expectedToken = token,
                discoverMs = 8_000L,
                candidateHosts = listOf("127.0.0.1"),
            )
            assertNotNull("HTTP discover must find payload", got)
            assertTrue("must be complete", got!!.isComplete)
            assertEquals("127.0.0.1", got.hostIp)
            assertEquals("DESKTOP-E5C1L4E\\MediaManager", got.userQualified)
            assertEquals("127.0.0.1", got.hostForCifs)
        } finally {
            server.close()
        }
    }

    @Test
    fun udpDiscover_usesPacketSourceIp_whenJsonIpEmpty() {
        val token = "simudp" + System.nanoTime().toString(16).takeLast(8)
        val body =
            """{"v":1,"m":"PVMSETUP1","t":"$token","ip":"","user":"DESKTOP-E5C1L4E\\MediaManager","share":"MediaManagerCatalog","pc":"DESKTOP-E5C1L4E"}"""
        val stop = AtomicBoolean(false)
        val sender = startUdpBlast(body, stop)
        try {
            val got = SetupLanAnnounce.discoverNetwork(
                expectedToken = token,
                discoverMs = 8_000L,
                candidateHosts = emptyList(), // solo UDP
            )
            assertNotNull("UDP discover must find payload", got)
            assertTrue(got!!.hasUser)
            assertTrue(
                "UDP source must supply IPv4 (got hostIp=${got.hostIp})",
                got.hasIpv4,
            )
            assertTrue(got.isComplete)
            assertEquals("DESKTOP-E5C1L4E\\MediaManager", got.userQualified)
        } finally {
            stop.set(true)
            sender.shutdownNow()
        }
    }

    @Test
    fun merge_doesNotStopOnUserOnly_untilIpv4Arrives() {
        val userOnly = SetupLanAnnounce.parsePayload(
            """{"v":1,"m":"PVMSETUP1","t":"tok","ip":"","user":"PC\\MediaManager","share":"S","pc":"PC"}""",
        )!!
        assertTrue(userOnly.hasUser)
        assertTrue(!userOnly.hasIpv4)
        assertTrue(!userOnly.isComplete)
        assertEquals("", userOnly.hostForCifs)

        val withIp = SetupLanAnnounce.parsePayload(
            """{"v":1,"m":"PVMSETUP1","t":"tok","ip":"192.168.68.68","user":"","share":"S","pc":"PC"}""",
        )!!
        val merged = SetupLanAnnounce.mergePayloads(userOnly, withIp)
        assertTrue(merged.isComplete)
        assertEquals("192.168.68.68", merged.hostForCifs)
        assertEquals("PC\\MediaManager", merged.userQualified)
    }

    @Test
    fun stress_httpDiscover_succeedsEveryTime() {
        val token = "stress" + System.nanoTime().toString(16).takeLast(8)
        val body =
            """{"v":1,"m":"PVMSETUP1","t":"$token","ip":"10.0.0.9","user":"DESKTOP-E5C1L4E\\MediaManager","share":"MediaManagerCatalog","pc":"DESKTOP-E5C1L4E","ips":"10.0.0.9,192.168.68.68"}"""
        val server = startHttpAnnouncer(body)
        val ok = AtomicInteger(0)
        try {
            repeat(5) {
                val got = SetupLanAnnounce.discoverNetwork(
                    expectedToken = token,
                    discoverMs = 6_000L,
                    candidateHosts = listOf("127.0.0.1"),
                )
                if (got?.isComplete == true &&
                    got.hostIp == "127.0.0.1" &&
                    got.userQualified.contains("MediaManager")
                ) {
                    ok.incrementAndGet()
                }
            }
            assertEquals("all 5 HTTP discoveries must succeed with IP+User", 5, ok.get())
        } finally {
            server.close()
        }
    }

    @Test
    fun parse_ipsField_fallbackWhenIpEmpty() {
        val p = SetupLanAnnounce.parsePayload(
            """{"v":1,"m":"PVMSETUP1","t":"t1","ip":"","user":"A\\B","share":"S","pc":"A","ips":"192.168.68.68,10.0.0.1"}""",
        )
        assertNotNull(p)
        assertEquals("192.168.68.68", p!!.hostIp)
        assertTrue(p.isComplete)
    }

    private fun startHttpAnnouncer(body: String): AutoCloseable {
        val server = ServerSocket()
        server.reuseAddress = true
        server.bind(InetSocketAddress(InetAddress.getByName("0.0.0.0"), SetupLanAnnounce.HTTP_PORT))
        val pool = Executors.newSingleThreadExecutor()
        val stop = AtomicBoolean(false)
        pool.execute {
            while (!stop.get() && !server.isClosed) {
                try {
                    server.soTimeout = 500
                    val client = try {
                        server.accept()
                    } catch (_: Exception) {
                        continue
                    }
                    client.use { c ->
                        c.getInputStream().read(ByteArray(2048))
                        val bytes = body.toByteArray(StandardCharsets.UTF_8)
                        val hdr =
                            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
                        val out = c.getOutputStream()
                        out.write(hdr.toByteArray(StandardCharsets.US_ASCII))
                        out.write(bytes)
                        out.flush()
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }
        }
        return AutoCloseable {
            stop.set(true)
            runCatching { server.close() }
            pool.shutdownNow()
            pool.awaitTermination(2, TimeUnit.SECONDS)
        }
    }

    private fun startUdpBlast(body: String, stop: AtomicBoolean): java.util.concurrent.ExecutorService {
        val pool = Executors.newSingleThreadExecutor()
        val data = body.toByteArray(StandardCharsets.UTF_8)
        pool.execute {
            DatagramSocket().use { sock ->
                sock.broadcast = true
                val targets = listOf(
                    InetSocketAddress("127.0.0.1", SetupLanAnnounce.UDP_PORT),
                    InetSocketAddress("255.255.255.255", SetupLanAnnounce.UDP_PORT),
                )
                while (!stop.get()) {
                    for (ep in targets) {
                        try {
                            sock.send(DatagramPacket(data, data.size, ep))
                        } catch (_: Exception) {
                            // ignore
                        }
                    }
                    try {
                        Thread.sleep(200)
                    } catch (_: InterruptedException) {
                        return@execute
                    }
                }
            }
        }
        return pool
    }
}
