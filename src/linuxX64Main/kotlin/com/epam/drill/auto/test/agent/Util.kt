package com.epam.drill.auto.test.agent

import kotlinx.cinterop.*
import platform.posix.*

fun createDirectory(path: String) {
    mkdir(path, 511)
}

fun connectSocket(host: String, port: String): ULong = memScoped {
    val address = resolve(host, port)
    return openSocket(address)
}

private fun MemScope.openSocket(address: Address): ULong {
    val sfd = socket(address.aiFamily, address.aiSockType, address.aiProtocol)
    val result = connect(sfd, address.aiAddr, address.aiAddrLen.convert())
    if (result != 0) {
        Logger.logError("Connection failed: $result\n")
        freeaddrinfo(address.ptr?.getPointer(this)?.pointed?.value)
        close(sfd.convert())
        error("Failed to establish connection")
    } else {
        Logger.logInfo("Websocket connected")
    }
    return sfd.convert()
}

private fun MemScope.resolve(host: String, port: String): Address {
    val hints = alloc<addrinfo>()
    memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
    hints.ai_family = platform.posix.AF_UNSPEC
    hints.ai_socktype = platform.posix.SOCK_STREAM
    hints.ai_flags = AI_PASSIVE
    val addressPtr = allocArray<addrinfo>(1)
    val ref = cValuesOf(addressPtr).getPointer(this)
    val result = getaddrinfo(host, port, hints.ptr, ref)
    if (result != 0) Logger.logError("Failed to resolve address: $host:$port")
    return Address(ref, this)
}


private class Address(val ptr: CValuesRef<CPointerVar<addrinfo>>?, memScope: MemScope) {
    val raw = ptr?.getPointer(memScope)?.pointed?.pointed
    val aiFamily: Int = raw?.ai_family ?: 0
    val aiSockType: Int = raw?.ai_socktype ?: 0
    val aiProtocol: Int = raw?.ai_protocol ?: 0
    val aiAddrLen: Int = raw?.ai_addrlen?.toInt() ?: 0
    val aiAddr = raw?.ai_addr
}

fun getSocketError(): Int {
    return errno
}

fun sendMessage(sfd: ULong, request: String, requestLength: Int): ssize_t {
    val reqRef = request.cstr
    return send(sfd.convert(), reqRef, requestLength.convert(), 0)
}
