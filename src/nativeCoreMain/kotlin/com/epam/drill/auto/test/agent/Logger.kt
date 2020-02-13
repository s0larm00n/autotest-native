package com.epam.drill.auto.test.agent

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.native.concurrent.*

const val INFO = "INFO"
const val ERROR = "ERROR"

class Logger {
    private val bufferSize = 128
    private val loggerFileName = "main.txt"
    var debugMode: Boolean = true
    var terminalOutput: Boolean = false
    var loggerDirPath: String = ""

    val loggerFilePath: String
        get() = "$loggerDirPath/$loggerFileName"

    fun init() {
        createDirectory(loggerDirPath)
        val fd = fopen(loggerFilePath, "w") ?: error("Failed to open logger dir")
        fclose(fd)
    }

    private fun openFile(): CPointer<FILE>? {
        return fopen(loggerFilePath, "a+") ?: error("Open logger output file failed")
    }

    fun logInfo(message: String) {
        if (debugMode) log(
            message,
            INFO
        )
    }

    fun logError(message: String, exception: Exception? = null) {
        log(
            message,
            ERROR
        )
        if (exception != null) throw exception
    }

    private fun log(message: String, mode: String) {
        if (terminalOutput) {
            println("[$mode] $message\n")
        } else memScoped {
            val outputFile = openFile()
            "[$mode] $message\n".chunked(bufferSize).forEach { chunk ->
                fprintf(outputFile, chunk)
            }
            fclose(outputFile)
        }
    }

}

fun logInfo(message: String) = log { logger.logInfo(message) }

fun logError(message: String) = log { logger.logError(message) }

inline fun <reified T> log(noinline what: Logger.() -> T) =
    loggerWorker.execute(TransferMode.UNSAFE, { what }) {
        it(logger)
    }.result

@SharedImmutable
val loggerWorker = Worker.start(true)

@ThreadLocal
val logger = Logger()