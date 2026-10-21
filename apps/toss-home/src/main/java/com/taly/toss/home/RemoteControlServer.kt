package com.taly.toss.home

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.concurrent.Executors
import kotlin.random.Random

/** Small LAN-only control endpoint for TOSS Control. Protocol: TOSS1|PIN|COMMAND. */
class RemoteControlServer(context: Context, private val onCommand: (String) -> Result<String>) {
    val port = 47990
    val pin: String
    @Volatile private var running = false
    private var server: ServerSocket? = null
    private val executor = Executors.newSingleThreadExecutor()

    init {
        val prefs = context.getSharedPreferences("toss_remote", Context.MODE_PRIVATE)
        pin = prefs.getString("pin", null) ?: Random.nextInt(100000, 1000000).toString().also {
            prefs.edit().putString("pin", it).apply()
        }
    }

    fun start() {
        if (running) return
        running = true
        executor.execute {
            try {
                server = ServerSocket().apply { reuseAddress = true; bind(InetSocketAddress(port)) }
                while (running) {
                    val socket = server?.accept() ?: break
                    handle(socket)
                }
            } catch (_: Exception) {
                if (running) running = false
            }
        }
    }

    fun stop() {
        running = false
        runCatching { server?.close() }
        executor.shutdownNow()
    }

    fun address(): String = runCatching {
        Collections.list(NetworkInterface.getNetworkInterfaces())
            .flatMap { Collections.list(it.inetAddresses) }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { !it.isLoopbackAddress && (it.isSiteLocalAddress || it.isLinkLocalAddress) }
            ?.hostAddress ?: "sem rede"
    }.getOrDefault("sem rede")

    private fun handle(socket: Socket) = socket.use { s ->
        s.soTimeout = 3000
        val remote = s.inetAddress
        val out = PrintWriter(s.getOutputStream(), true)
        if (!(remote.isSiteLocalAddress || remote.isLinkLocalAddress || remote.isLoopbackAddress)) {
            out.println("ERR|LAN_ONLY"); return
        }
        val line = BufferedReader(InputStreamReader(s.getInputStream())).readLine()?.take(256).orEmpty()
        val parts = line.split('|', limit = 3)
        if (parts.size != 3 || parts[0] != "TOSS1") { out.println("ERR|PROTOCOL"); return }
        if (parts[1] != pin) { out.println("ERR|PIN"); return }
        val result = onCommand(parts[2].trim().uppercase())
        result.fold({ out.println("OK|$it") }, { out.println("ERR|${it.message ?: "COMMAND"}") })
    }
}
