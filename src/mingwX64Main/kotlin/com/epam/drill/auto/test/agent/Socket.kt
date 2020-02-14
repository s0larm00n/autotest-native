package com.epam.drill.auto.test.agent

import kotlinx.cinterop.*
import platform.posix.*

fun resolveAddress(host: String, port: String) = memScoped {
    val addr = allocArray<platform.windows.LPADDRINFOVar>(1)
    val alloc = alloc<platform.windows.addrinfo>()
    alloc.ai_family = AF_INET
    alloc.ai_socktype = SOCK_STREAM
    alloc.ai_protocol = IPPROTO_TCP
    platform.windows.getaddrinfo(host, port, alloc.ptr, addr)
    val info = addr[0]!!.pointed
    val aiAddr: CPointer<sockaddr> = info.ai_addr!!
    aiAddr as CValuesRef<sockaddr>
}

fun getSocketError(): Int {
    val rc = platform.windows.WSAGetLastError()
    if (rc == WSAEWOULDBLOCK) return EAGAIN
    if (rc == platform.windows.WSAEINPROGRESS) return EINPROGRESS
    if (rc == platform.windows.WSAEISCONN || rc == platform.windows.WSAEALREADY) return EISCONN
    return rc
}
