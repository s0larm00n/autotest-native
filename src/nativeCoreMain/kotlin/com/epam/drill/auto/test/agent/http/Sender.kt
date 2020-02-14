package com.epam.drill.auto.test.agent.http

import com.epam.drill.auto.test.agent.*
import kotlinx.cinterop.*
import platform.posix.*

@ThreadLocal
object Sender {

    fun post(
        host: String,
        port: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
        responseBufferSize: Int = 4096
    ): HttpResponse {
        val request = HttpRequest(host, port, path, body, "POST")
        headers.forEach { (key, value) ->
            request.addHeader(key, value)
        }
        return httpRequest(
            host,
            port,
            request.build(),
            responseBufferSize
        )
    }

    private fun httpRequest(
        host: String,
        port: String,
        request: String,
        responseBufferSize: Int = 4096
    ): HttpResponse = memScoped{
        val sfd = connect(host, port)
        mainLogger.debug { "Websocket connected" }
        val requestLength = request.length
        mainLogger.debug { "Attempting to send request of length $requestLength" }
        val written = send(sfd.convert(), request.cstr, requestLength.convert(), 0)
        mainLogger.debug { "Wrote $written of $requestLength expected; error: ${getSocketError()}" }
        val buffer = " ".repeat(responseBufferSize).cstr.getPointer(memScope)
        val read = recv(sfd.convert(), buffer, responseBufferSize.convert(), 0)
        mainLogger.debug { "Read $read of $responseBufferSize possible" }
        val result = buffer.toKString()
        close(sfd.convert())
        mainLogger.debug { "Closed socket connection" }
        return HttpResponse(result)
    }

    private fun connect(host: String, port: String): ULong =
        socket(AF_INET, SOCK_STREAM, IPPROTO_TCP).also { socketfd ->
            connect(socketfd, resolveAddress(host, port), sockaddr_in.size.convert())
        }

}
