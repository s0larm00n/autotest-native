package com.epam.drill.auto.test.agent

import kotlinx.cinterop.*
import platform.posix.*

const val INFO = "INFO"
const val ERROR = "ERROR"

//TODO: remove default debug logging

object Logger {
    private var debugMode: Boolean = true
    private var terminalOutput: Boolean = false
    private var loggerDirPath: String = "logs"
    private const val bufferSize = 128
    private const val loggerFileName = "main.txt"
    private var loggerFilePath = ""

    init {
        loggerFilePath = "$loggerDirPath/$loggerFileName"
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
