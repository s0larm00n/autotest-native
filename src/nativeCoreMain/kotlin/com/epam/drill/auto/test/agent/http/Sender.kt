package com.epam.drill.auto.test.agent.http

import com.epam.drill.auto.test.agent.Logger
import com.epam.drill.auto.test.agent.connectSocket
import com.epam.drill.auto.test.agent.getSocketError
import com.epam.drill.auto.test.agent.sendMessage
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
    ): HttpResponse = memScoped {
        val sfd = connectSocket(host, port)

        val requestLength = request.length
        Logger.logInfo("Attempting to send request of length $requestLength")
        val written = sendMessage(sfd, request, requestLength)
        Logger.logInfo("Wrote $written of $requestLength expected; error: ${getSocketError()}")
        val buffer = " ".repeat(responseBufferSize).cstr.getPointer(this)
        val read = recv(sfd.convert(), buffer, responseBufferSize.convert(), 0)
        Logger.logInfo("Read $read of $responseBufferSize possible")

        val result = buffer.toKString()
        close(sfd.convert())
        Logger.logInfo("Closed socket connection")

        return HttpResponse(result)
    }




}
