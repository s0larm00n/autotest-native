package com.epam.drill.auto.test.agent

import platform.posix.*
import platform.posix.WSAEWOULDBLOCK
import platform.windows.*

fun createDirectory(path: String){
    mkdir(path)
}

fun connectSocket(host: String, port: String): Ulong {
    val address = resolve(host, port)
    return openSocket(address)
}

private fun openSocket(address: Address): ULong {
    val sfd = platform.posix.socket(address.aiFamily, address.aiSockType, address.aiProtocol)
    val result = platform.posix.connect(sfd, address.aiAddr, address.aiAddrLen)
    if (result != 0) {
        Logger.logError("Connection failed: $result\n")
        freeaddrinfo(address.ptr[0])
        close(sfd.convert())
        error("Failed to establish connection")
    } else {
        Logger.logInfo("Websocket connected")
    }
    return sfd
}

private fun resolve(host: String, port: String): Address = memScoped {
    val hints = alloc<addrinfo>()
    memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
    hints.ai_family = platform.posix.AF_UNSPEC
    hints.ai_socktype = platform.posix.SOCK_STREAM
    hints.ai_flags = AI_PASSIVE
    val addressPtr = allocArray<LPADDRINFOVar>(1)
    val result = platform.windows.getaddrinfo(host, port, hints.ptr, addressPtr)
    if (result != 0) Logger.logError("Failed to resolve address: $host:$port")
    Address(addressPtr)
}


private class Address(val ptr: CArrayPointer<LPADDRINFOVar>) {
    val aiFamily: Int = ptr.pointed.pointed?.ai_family ?: 0
    val aiSockType: Int = ptr.pointed.pointed?.ai_socktype ?: 0
    val aiProtocol: Int = ptr.pointed.pointed?.ai_protocol ?: 0
    val aiAddrLen: Int = ptr.pointed.pointed?.ai_addrlen?.toInt() ?: 0
    val aiAddr = ptr.pointed.pointed?.ai_addr
}

fun getSocketError(): Int {
    val rc = platform.windows.WSAGetLastError()
    if (rc == WSAEWOULDBLOCK) return EAGAIN
    if (rc == platform.windows.WSAEINPROGRESS) return EINPROGRESS
    if (rc == platform.windows.WSAEISCONN || rc == platform.windows.WSAEALREADY) return EISCONN
    return rc
}

fun sendMessage(sfd: ULong, request: String, requestLength: Int): ssize_t =
    send(sfd, request, requestLength, 0)
