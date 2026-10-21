package com.taly.toss.home

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.Executors

/**
 * Lightweight same-LAN HTTP endpoint for the iPhone/iPad TOSS remote.
 *
 * The web UI is served by the projector itself, so it does not depend on cloud hosting and
 * browser commands remain same-origin. This is intentionally a small Phase-1 bridge; in Phase 2
 * it should move out of the Home process into the dedicated TOSS Remote Service.
 */
class PwaRemoteServer(
    private val context: Context,
    private val remote: RemoteControlServer,
    private val onCommand: (String) -> Result<String>
) {
    val port = 47991
    @Volatile private var running = false
    private var server: ServerSocket? = null
    private val acceptExecutor = Executors.newSingleThreadExecutor()
    private val clientExecutor = Executors.newCachedThreadPool()

    fun start() {
        if (running) return
        running = true
        acceptExecutor.execute {
            try {
                server = ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress(port))
                }
                while (running) {
                    val socket = server?.accept() ?: break
                    clientExecutor.execute { handle(socket) }
                }
            } catch (_: Exception) {
                if (running) running = false
            }
        }
    }

    fun stop() {
        running = false
        runCatching { server?.close() }
        acceptExecutor.shutdownNow()
        clientExecutor.shutdownNow()
    }

    fun url(): String = "http://${remote.address()}:$port"

    private fun handle(socket: Socket) = socket.use { s ->
        s.soTimeout = 3500
        val remoteAddress = s.inetAddress
        if (!(remoteAddress.isSiteLocalAddress || remoteAddress.isLinkLocalAddress || remoteAddress.isLoopbackAddress)) {
            writeResponse(s, 403, "text/plain; charset=utf-8", "LAN only")
            return
        }

        val input = BufferedInputStream(s.getInputStream())
        val requestLine = readLine(input)?.take(1024).orEmpty()
        val parts = requestLine.split(' ')
        if (parts.size < 2) {
            writeResponse(s, 400, "text/plain; charset=utf-8", "Bad request")
            return
        }
        val method = parts[0].uppercase()
        val rawTarget = parts[1]
        val headers = linkedMapOf<String, String>()
        while (true) {
            val line = readLine(input) ?: break
            if (line.isEmpty()) break
            val colon = line.indexOf(':')
            if (colon > 0) headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
        }

        if (method == "POST" && rawTarget.substringBefore('?') == "/api/command") {
            val length = headers["content-length"]?.toIntOrNull()?.coerceIn(0, 512) ?: 0
            val body = ByteArray(length)
            var offset = 0
            while (offset < length) {
                val read = input.read(body, offset, length - offset)
                if (read <= 0) break
                offset += read
            }
            val params = String(body, 0, offset, Charsets.UTF_8)
                .split('&')
                .mapNotNull {
                    val pair = it.split('=', limit = 2)
                    if (pair.size == 2) URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair[1], "UTF-8") else null
                }.toMap()
            val pin = params["pin"].orEmpty()
            val command = params["command"].orEmpty().trim().uppercase().take(64)
            if (pin != remote.pin) {
                writeJson(s, 401, false, "PIN incorreto")
                return
            }
            if (command.isBlank()) {
                writeJson(s, 400, false, "Comando vazio")
                return
            }
            val result = onCommand(command)
            result.fold(
                { writeJson(s, 200, true, it) },
                { writeJson(s, 400, false, it.message ?: "Falha no comando") }
            )
            return
        }

        if (method != "GET") {
            writeResponse(s, 405, "text/plain; charset=utf-8", "Method not allowed")
            return
        }

        when (rawTarget.substringBefore('?')) {
            "/", "/index.html" -> serveAsset(s, "toss-remote/index.html", "text/html; charset=utf-8")
            "/manifest.webmanifest" -> serveAsset(s, "toss-remote/manifest.webmanifest", "application/manifest+json; charset=utf-8")
            "/icon.svg" -> serveAsset(s, "toss-remote/icon.svg", "image/svg+xml; charset=utf-8")
            else -> writeResponse(s, 404, "text/plain; charset=utf-8", "Not found")
        }
    }

    private fun serveAsset(socket: Socket, path: String, contentType: String) {
        val bytes = runCatching { context.assets.open(path).use { it.readBytes() } }.getOrNull()
        if (bytes == null) writeResponse(socket, 404, "text/plain; charset=utf-8", "Not found")
        else writeResponse(socket, 200, contentType, bytes)
    }

    private fun writeJson(socket: Socket, status: Int, ok: Boolean, message: String) {
        val escaped = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        writeResponse(socket, status, "application/json; charset=utf-8", "{\"ok\":$ok,\"message\":\"$escaped\"}")
    }

    private fun writeResponse(socket: Socket, status: Int, contentType: String, text: String) =
        writeResponse(socket, status, contentType, text.toByteArray(Charsets.UTF_8))

    private fun writeResponse(socket: Socket, status: Int, contentType: String, bytes: ByteArray) {
        val reason = when (status) {
            200 -> "OK"; 400 -> "Bad Request"; 401 -> "Unauthorized"; 403 -> "Forbidden"
            404 -> "Not Found"; 405 -> "Method Not Allowed"; else -> "Error"
        }
        val out = BufferedOutputStream(socket.getOutputStream())
        val headers = "HTTP/1.1 $status $reason\r\n" +
            "Content-Type: $contentType\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Cache-Control: no-store\r\n" +
            "Connection: close\r\n\r\n"
        out.write(headers.toByteArray(Charsets.US_ASCII))
        out.write(bytes)
        out.flush()
    }

    private fun readLine(input: BufferedInputStream): String? {
        val bytes = ArrayList<Byte>(128)
        while (bytes.size < 4096) {
            val value = input.read()
            if (value == -1) return if (bytes.isEmpty()) null else bytes.toByteArray().toString(Charsets.UTF_8)
            if (value == '\n'.code) break
            if (value != '\r'.code) bytes.add(value.toByte())
        }
        return bytes.toByteArray().toString(Charsets.UTF_8)
    }
}
