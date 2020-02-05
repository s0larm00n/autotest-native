package com.epam.drill.auto.test.agent

import platform.posix.*

fun createDirectory(path: String){
    mkdir(path, 511)
}
