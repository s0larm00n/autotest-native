package com.epam.drill.auto.test.agent

import kotlinx.cinterop.*
import platform.posix.*

fun resolveAddress(host: String, port: Int) = memScoped {
    val ip = IP.fromHost(host)
    val addr = allocArray<sockaddr_in>(1)
    addr.set(ip, port)
    @Suppress("UNCHECKED_CAST")
    addr as CValuesRef<sockaddr>
}

fun getSocketError(): Int {
    return errno
}
